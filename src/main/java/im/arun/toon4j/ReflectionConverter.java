package im.arun.toon4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-performance POJO to Map converter using optimized reflection.
 * Uses cached accessors for fast field/getter access.
 * Internal use only.
 */
final class ReflectionConverter {
    private ReflectionConverter() {}

    // Reflection cache for POJO to Map conversion
    private static final ConcurrentHashMap<Class<?>, List<Accessor>> reflectionCache = new ConcurrentHashMap<>();

    /**
     * Represents a cached field or method accessor for fast reflection.
     */
    private static abstract class Accessor {
        final String name;

        Accessor(String name) {
            this.name = name;
        }

        abstract Object getValue(Object target) throws Exception;
    }

    private static class FieldAccessor extends Accessor {
        final Field field;

        FieldAccessor(String name, Field field) {
            super(name);
            this.field = field;
            field.setAccessible(true);
        }

        @Override
        Object getValue(Object target) throws Exception {
            return field.get(target);
        }
    }

    private static class MethodAccessor extends Accessor {
        final Method method;

        MethodAccessor(String name, Method method) {
            super(name);
            this.method = method;
        }

        @Override
        Object getValue(Object target) throws Exception {
            return method.invoke(target);
        }
    }

    /**
     * Check if a value is a POJO that needs conversion.
     * Same logic as GsonConverter for consistency.
     */
    static boolean isPojo(Object value) {
        if (value == null) {
            return false;
        }

        // Already supported types - no conversion needed
        if (value instanceof String ||
            value instanceof Number ||
            value instanceof Boolean ||
            value instanceof Character ||
            value instanceof Map ||
            value instanceof Iterable ||
            value.getClass().isArray()) {
            return false;
        }

        // Check for java.time and java.util temporal types - already handled
        String className = value.getClass().getName();
        if (className.startsWith("java.time.") ||
            className.startsWith("java.util.Date") ||
            className.equals("java.time.Instant")) {
            return false;
        }

        // Check for Java standard library classes that shouldn't be serialized
        if (className.startsWith("java.") ||
            className.startsWith("javax.") ||
            className.startsWith("sun.")) {
            return false;
        }

        // If we get here, it's likely a user POJO
        return true;
    }

    /**
     * Convert a POJO to a Map using optimized reflection.
     * Strategy: Direct field/getter introspection with cached accessors (fastest for standard POJOs).
     * Avoids two-stage serialize/deserialize which is slower than direct reflection.
     */
    @SuppressWarnings("unchecked")
    static Map<String, Object> toMap(Object pojo) {
        if (pojo == null) {
            return null;
        }

        // Direct reflection is faster than serialize+deserialize round-trip
        // Accessors are cached per class, so overhead is minimal
        return reflectPojoToMap(pojo);
    }

    // Reflection fallback for POJO -> Map when DSL-JSON can't serialize the class
    private static Map<String, Object> reflectPojoToMap(Object pojo) {
        Class<?> cls = pojo.getClass();

        // Get or compute cached accessors
        List<Accessor> accessors = reflectionCache.computeIfAbsent(cls, ReflectionConverter::buildAccessors);

        Map<String, Object> out = new LinkedHashMap<>(accessors.size());
        for (Accessor accessor : accessors) {
            try {
                Object value = accessor.getValue(pojo);
                out.put(accessor.name, value);
            } catch (Exception e) {
                // Skip inaccessible or failing accessors (e.g., security restrictions,
                // lazy-loading proxies throwing exceptions). This is intentional to allow
                // partial serialization of POJOs with problematic fields.
            }
        }

        return out;
    }

    /**
     * Build the list of accessors for a given class (cached).
     */
    private static List<Accessor> buildAccessors(Class<?> cls) {
        List<Accessor> accessors = new ArrayList<>();

        // Java records: use record components as accessors
        try {
            if (cls.isRecord()) {
                var components = cls.getRecordComponents();
                for (var c : components) {
                    Method acc = c.getAccessor();
                    accessors.add(new MethodAccessor(c.getName(), acc));
                }
                return accessors;
            }
        } catch (Exception e) {
            // Record introspection failed (e.g., security manager restrictions).
            // Fall through to getter/field-based extraction.
        }

        // Public getters first (JavaBean style)
        Map<String, Method> getters = new LinkedHashMap<>();
        try {
            Method[] methods = cls.getMethods(); // includes inherited public
            for (Method m : methods) {
                if (m.getParameterCount() != 0) continue;
                if (Modifier.isStatic(m.getModifiers())) continue;
                String name = m.getName();
                if ("getClass".equals(name)) continue;

                String prop = null;
                if (name.startsWith("get") && name.length() > 3) {
                    prop = decapitalize(name.substring(3));
                } else if (name.startsWith("is") && name.length() > 2) {
                    Class<?> rt = m.getReturnType();
                    if (rt == boolean.class || rt == Boolean.class) {
                        prop = decapitalize(name.substring(2));
                    }
                }
                if (prop != null && !prop.isEmpty()) {
                    getters.putIfAbsent(prop, m);
                }
            }

            for (Map.Entry<String, Method> entry : getters.entrySet()) {
                accessors.add(new MethodAccessor(entry.getKey(), entry.getValue()));
            }
        } catch (Exception e) {
            // Method introspection failed (e.g., security restrictions).
            // Fall through to field-based extraction.
        }

        // If no getters found, fall back to declared fields
        if (accessors.isEmpty()) {
            Class<?> c = cls;
            while (c != null && c != Object.class) {
                Field[] fields = c.getDeclaredFields();
                for (Field f : fields) {
                    if (Modifier.isStatic(f.getModifiers())) continue;
                    accessors.add(new FieldAccessor(f.getName(), f));
                }
                c = c.getSuperclass();
            }
        }

        return accessors;
    }

    private static String decapitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        if (s.length() > 1 && Character.isUpperCase(s.charAt(0)) && Character.isUpperCase(s.charAt(1))) {
            return s; // e.g., URL -> URL
        }
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }
}
