package im.arun.toon4j;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-performance Map to POJO deserializer using optimized reflection.
 * Supports JavaBeans (setters), public fields, and Java records.
 * Uses cached metadata for fast deserialization.
 * Internal use only.
 */
final class PojoDeserializer {
    private PojoDeserializer() {}

    // Cache for class metadata
    private static final ConcurrentHashMap<Class<?>, ClassMetadata> metadataCache = new ConcurrentHashMap<>();

    /**
     * Metadata about how to deserialize a class.
     */
    private static class ClassMetadata {
        final Class<?> targetClass;
        final Constructor<?> noArgConstructor;
        final Constructor<?> canonicalConstructor; // For records
        final Map<String, Setter> setters;
        final boolean isRecord;

        ClassMetadata(Class<?> targetClass) {
            this.targetClass = targetClass;
            this.isRecord = isRecord(targetClass);
            this.canonicalConstructor = isRecord ? findCanonicalConstructor(targetClass) : null;
            this.noArgConstructor = isRecord ? null : findNoArgConstructor(targetClass);
            this.setters = isRecord ? Collections.emptyMap() : buildSetters(targetClass);
        }
    }

    /**
     * Represents a setter (method or field).
     */
    private static abstract class Setter {
        final String propertyName;
        final Class<?> propertyType;
        final Type genericType;

        Setter(String propertyName, Class<?> propertyType, Type genericType) {
            this.propertyName = propertyName;
            this.propertyType = propertyType;
            this.genericType = genericType;
        }

        abstract void setValue(Object target, Object value) throws Exception;
    }

    private static class MethodSetter extends Setter {
        final Method method;

        MethodSetter(String propertyName, Method method, Class<?> propertyType, Type genericType) {
            super(propertyName, propertyType, genericType);
            this.method = method;
            method.setAccessible(true);
        }

        @Override
        void setValue(Object target, Object value) throws Exception {
            method.invoke(target, value);
        }
    }

    private static class FieldSetter extends Setter {
        final Field field;

        FieldSetter(String propertyName, Field field) {
            super(propertyName, field.getType(), field.getGenericType());
            this.field = field;
            field.setAccessible(true);
        }

        @Override
        void setValue(Object target, Object value) throws Exception {
            field.set(target, value);
        }
    }

    /**
     * Deserialize a Map to a POJO of the specified type.
     *
     * @param map the map containing property values
     * @param targetClass the target POJO class
     * @param <T> the type of the POJO
     * @return the deserialized POJO instance
     */
    @SuppressWarnings("unchecked")
    static <T> T fromMap(Map<String, Object> map, Class<T> targetClass) {
        if (map == null) {
            return null;
        }

        if (targetClass == null) {
            throw new IllegalArgumentException("Target class cannot be null");
        }

        // Get or compute cached metadata
        ClassMetadata metadata = metadataCache.computeIfAbsent(targetClass, ClassMetadata::new);

        try {
            if (metadata.isRecord) {
                return (T) deserializeRecord(map, metadata);
            } else {
                return (T) deserializeBean(map, metadata);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize to " + targetClass.getName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Deserialize a value to the target type.
     * Handles primitives, nested POJOs, and collections.
     */
    @SuppressWarnings("unchecked")
    static Object deserializeValue(Object value, Class<?> targetType) {
        return deserializeValue(value, targetType, targetType);
    }

    /**
     * Deserialize a value to the target type with generic type information.
     * Handles primitives, nested POJOs, and collections with generic parameters.
     */
    @SuppressWarnings("unchecked")
    static Object deserializeValue(Object value, Class<?> targetType, Type genericType) {
        if (value == null) {
            return null;
        }

        // Handle collections with generic types FIRST (before type check)
        // This is important because we need to convert List<Map> to List<POJO>
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            if (targetType.isArray()) {
                return convertToArray(list, targetType.getComponentType());
            } else if (Collection.class.isAssignableFrom(targetType)) {
                // Extract generic type parameter for collections
                Class<?> elementType = extractElementType(genericType);

                // Determine if we need conversion:
                // 1. Collection type doesn't match (e.g., List → Set)
                boolean needsCollectionConversion = !targetType.isInstance(value);

                // 2. Elements need conversion (e.g., Map → POJO)
                boolean needsElementConversion = elementType != Object.class;

                if (needsCollectionConversion || needsElementConversion) {
                    return convertToCollection(list, targetType, elementType);
                }

                // Direct assignment works - no conversion needed
                return value;
            }
        }

        // Direct assignment if types match (but not for collections handled above)
        if (targetType.isInstance(value)) {
            return value;
        }

        // Handle primitive type conversions
        if (targetType.isPrimitive() || isWrapperType(targetType)) {
            return convertPrimitive(value, targetType);
        }

        // Handle String
        if (targetType == String.class) {
            return value.toString();
        }

        // Handle enum
        if (targetType.isEnum()) {
            return convertToEnum(value, (Class<? extends Enum>) targetType);
        }

        // Handle nested POJO
        if (value instanceof Map) {
            return fromMap((Map<String, Object>) value, targetType);
        }

        // Handle BigDecimal, BigInteger
        if (targetType == BigDecimal.class) {
            if (value instanceof Number) {
                return BigDecimal.valueOf(((Number) value).doubleValue());
            }
            return new BigDecimal(value.toString());
        }
        if (targetType == BigInteger.class) {
            if (value instanceof Number) {
                return BigInteger.valueOf(((Number) value).longValue());
            }
            return new BigInteger(value.toString());
        }

        // Fallback: return as-is
        return value;
    }

    /**
     * Extract the element type from a parameterized collection type.
     * For example, List<Employee> returns Employee.class
     */
    private static Class<?> extractElementType(Type genericType) {
        if (genericType instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) genericType;
            Type[] typeArgs = paramType.getActualTypeArguments();
            if (typeArgs.length > 0) {
                Type typeArg = typeArgs[0];
                if (typeArg instanceof Class) {
                    return (Class<?>) typeArg;
                } else if (typeArg instanceof ParameterizedType) {
                    // Handle nested generics like List<List<String>>
                    return (Class<?>) ((ParameterizedType) typeArg).getRawType();
                }
            }
        } else if (genericType instanceof Class) {
            // If it's just a raw List without generics, return Object
            return Object.class;
        }
        return Object.class; // Fallback to Object if no generic info
    }

    private static Object deserializeRecord(Map<String, Object> map, ClassMetadata metadata) throws Exception {
        Constructor<?> constructor = metadata.canonicalConstructor;
        if (constructor == null) {
            throw new IllegalStateException("No canonical constructor found for record: " + metadata.targetClass.getName());
        }

        Parameter[] params = constructor.getParameters();
        Object[] args = new Object[params.length];

        for (int i = 0; i < params.length; i++) {
            String paramName = params[i].getName();
            Object value = map.get(paramName);
            args[i] = deserializeValue(value, params[i].getType());
        }

        return constructor.newInstance(args);
    }

    private static Object deserializeBean(Map<String, Object> map, ClassMetadata metadata) throws Exception {
        // Create instance
        Object instance;
        if (metadata.noArgConstructor != null) {
            instance = metadata.noArgConstructor.newInstance();
        } else {
            throw new IllegalStateException("No no-arg constructor found for class: " + metadata.targetClass.getName());
        }

        // Set properties
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String propertyName = entry.getKey();
            Object value = entry.getValue();

            Setter setter = metadata.setters.get(propertyName);
            if (setter != null) {
                Object convertedValue = deserializeValue(value, setter.propertyType, setter.genericType);
                setter.setValue(instance, convertedValue);
            }
        }

        return instance;
    }

    private static Constructor<?> findNoArgConstructor(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static Constructor<?> findCanonicalConstructor(Class<?> recordClass) {
        try {
            // Get record components
            RecordComponent[] components = recordClass.getRecordComponents();
            Class<?>[] paramTypes = new Class<?>[components.length];
            for (int i = 0; i < components.length; i++) {
                paramTypes[i] = components[i].getType();
            }

            Constructor<?> constructor = recordClass.getDeclaredConstructor(paramTypes);
            constructor.setAccessible(true);
            return constructor;
        } catch (Exception e) {
            return null;
        }
    }

    private static Map<String, Setter> buildSetters(Class<?> clazz) {
        Map<String, Setter> setters = new LinkedHashMap<>();

        // Find setter methods
        try {
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (method.getParameterCount() != 1) continue;
                if (Modifier.isStatic(method.getModifiers())) continue;

                String name = method.getName();
                if (name.startsWith("set") && name.length() > 3) {
                    String propertyName = decapitalize(name.substring(3));
                    Class<?> propertyType = method.getParameterTypes()[0];
                    Type genericType = method.getGenericParameterTypes()[0];
                    setters.put(propertyName, new MethodSetter(propertyName, method, propertyType, genericType));
                }
            }
        } catch (Exception e) {
            // Method introspection failed (e.g., security restrictions).
            // Fall through to field-based deserialization.
        }

        // If no setters found, use fields
        if (setters.isEmpty()) {
            Class<?> current = clazz;
            while (current != null && current != Object.class) {
                Field[] fields = current.getDeclaredFields();
                for (Field field : fields) {
                    if (Modifier.isStatic(field.getModifiers())) continue;
                    if (Modifier.isFinal(field.getModifiers())) continue;
                    setters.put(field.getName(), new FieldSetter(field.getName(), field));
                }
                current = current.getSuperclass();
            }
        }

        return setters;
    }

    private static String decapitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        if (s.length() > 1 && Character.isUpperCase(s.charAt(0)) && Character.isUpperCase(s.charAt(1))) {
            return s;
        }
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    private static boolean isRecord(Class<?> clazz) {
        try {
            return clazz.isRecord();
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isWrapperType(Class<?> clazz) {
        return clazz == Boolean.class ||
               clazz == Character.class ||
               clazz == Byte.class ||
               clazz == Short.class ||
               clazz == Integer.class ||
               clazz == Long.class ||
               clazz == Float.class ||
               clazz == Double.class;
    }

    private static Object convertPrimitive(Object value, Class<?> targetType) {
        if (value instanceof Number) {
            Number num = (Number) value;
            if (targetType == int.class || targetType == Integer.class) return num.intValue();
            if (targetType == long.class || targetType == Long.class) return num.longValue();
            if (targetType == double.class || targetType == Double.class) return num.doubleValue();
            if (targetType == float.class || targetType == Float.class) return num.floatValue();
            if (targetType == short.class || targetType == Short.class) return num.shortValue();
            if (targetType == byte.class || targetType == Byte.class) return num.byteValue();
        }

        if (targetType == boolean.class || targetType == Boolean.class) {
            if (value instanceof Boolean) return value;
            return Boolean.parseBoolean(value.toString());
        }

        if (targetType == char.class || targetType == Character.class) {
            String str = value.toString();
            return str.isEmpty() ? '\0' : str.charAt(0);
        }

        return value;
    }

    @SuppressWarnings("unchecked")
    private static <E extends Enum<E>> E convertToEnum(Object value, Class<E> enumType) {
        if (value instanceof String) {
            return Enum.valueOf(enumType, (String) value);
        }
        return Enum.valueOf(enumType, value.toString());
    }

    private static Object convertToArray(List<?> list, Class<?> componentType) {
        Object array = Array.newInstance(componentType, list.size());
        for (int i = 0; i < list.size(); i++) {
            Object value = deserializeValue(list.get(i), componentType);
            Array.set(array, i, value);
        }
        return array;
    }

    @SuppressWarnings("unchecked")
    private static Object convertToCollection(List<?> list, Class<?> collectionType, Class<?> elementType) {
        Collection<Object> collection;

        if (collectionType == List.class || collectionType == Collection.class || collectionType == ArrayList.class) {
            collection = new ArrayList<>(list.size());
        } else if (collectionType == Set.class || collectionType == HashSet.class) {
            collection = new HashSet<>(list.size());
        } else if (collectionType == LinkedHashSet.class) {
            collection = new LinkedHashSet<>(list.size());
        } else {
            // Try to instantiate the collection type
            try {
                collection = (Collection<Object>) collectionType.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                // Fallback to ArrayList
                collection = new ArrayList<>(list.size());
            }
        }

        // Convert each element to the target element type
        for (Object item : list) {
            Object converted = deserializeValue(item, elementType);
            collection.add(converted);
        }

        return collection;
    }
}
