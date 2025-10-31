package im.arun.toon4j;

import com.dslplatform.json.*;
import com.dslplatform.json.runtime.Settings;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-performance POJO to Map converter built for TOON.
 * Internal use only.
 */
final class DslJsonConverter {
    private DslJsonConverter() {}

    private static final DslJson<Object> dslJson = new DslJson<>(Settings.withRuntime().includeServiceLoader());
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
            try {
                field.setAccessible(true);
            } catch (Exception ignored) {
                // ignore - reflective access will fail gracefully later
            }
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
            value instanceof Enum<?> ||
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
     * Convert a POJO to a Map representation.
     * Strategy: cache fast reflection accessors and recursively materialize nested structures.
     */
    @SuppressWarnings("unchecked")
    static Map<String, Object> toMap(Object pojo) {
        if (pojo == null) {
            return null;
        }

        return reflectPojoToMap(pojo, new IdentityHashMap<>());
    }

    // Reflection path for POJO -> Map conversion
    private static Map<String, Object> reflectPojoToMap(Object pojo, IdentityHashMap<Object, Object> seen) {
        Class<?> cls = pojo.getClass();

        // Get or compute cached accessors
        List<Accessor> accessors = reflectionCache.computeIfAbsent(cls, DslJsonConverter::buildAccessors);

        Map<String, Object> out = new LinkedHashMap<>(accessors.size());
        Object existing = seen.putIfAbsent(pojo, out);
        if (existing instanceof Map) {
            // Already materialized - reuse it directly to break cycles
            return (Map<String, Object>) existing;
        }

        for (Accessor accessor : accessors) {
            try {
                Object value = accessor.getValue(pojo);
                out.put(accessor.name, normalize(value, seen));
            } catch (Throwable ignored) {
                // Skip this accessor if it fails
            }
        }

        return out;
    }

    /**
     * Build the list of accessors for a given class (cached).
     */
    private static List<Accessor> buildAccessors(Class<?> cls) {
        List<Accessor> accessors = new ArrayList<>();

        // Java records (if any)
        try {
            if (cls.isRecord()) {
                var components = cls.getRecordComponents();
                for (var c : components) {
                    Method acc = c.getAccessor();
                    try {
                        acc.setAccessible(true);
                    } catch (Exception ignored) {
                        // continue with best-effort reflective access
                    }
                    accessors.add(new MethodAccessor(c.getName(), acc));
                }
                return accessors;
            }
        } catch (Throwable ignored) {
            // Ignore and continue to getters/fields
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
                Method method = entry.getValue();
                try {
                    method.setAccessible(true);
                } catch (Exception ignored) {
                    // fall through - we'll attempt invocation regardless
                }
                accessors.add(new MethodAccessor(entry.getKey(), method));
            }
        } catch (Throwable ignored) {
            // Ignore and proceed to fields
        }

        // If no getters found, fall back to declared fields
        if (accessors.isEmpty()) {
            Class<?> c = cls;
            while (c != null && c != Object.class) {
                Field[] fields = c.getDeclaredFields();
                for (Field f : fields) {
                    if (Modifier.isStatic(f.getModifiers())) continue;
                    try {
                        f.setAccessible(true);
                    } catch (Exception ignored) {
                        // ignore - reflective access will fail gracefully later
                    }
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

    /**
     * Get the DslJson instance (for future extensions).
     */
    static DslJson<Object> getDslJson() {
        return dslJson;
    }

    private static Object normalize(Object value, IdentityHashMap<Object, Object> seen) {
        if (value == null) {
            return null;
        }

        if (value instanceof Map<?, ?> map) {
            Object cached = seen.get(value);
            if (cached instanceof Map) {
                return cached;
            }

            Map<String, Object> normalized = new LinkedHashMap<>(map.size());
            seen.put(value, normalized);
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object key = entry.getKey();
                String normalizedKey = key == null ? "null" : key.toString();
                normalized.put(normalizedKey, normalize(entry.getValue(), seen));
            }
            return normalized;
        }

        if (value instanceof Iterable<?> iterable) {
            Object cached = seen.get(value);
            if (cached instanceof List) {
                return cached;
            }

            List<Object> list = new ArrayList<>();
            seen.put(value, list);
            for (Object element : iterable) {
                list.add(normalize(element, seen));
            }
            return list;
        }

        Class<?> valueClass = value.getClass();
        if (valueClass.isArray()) {
            Object cached = seen.get(value);
            if (cached instanceof List) {
                return cached;
            }

            int length = Array.getLength(value);
            List<Object> list = new ArrayList<>(length);
            seen.put(value, list);
            for (int i = 0; i < length; i++) {
                list.add(normalize(Array.get(value, i), seen));
            }
            return list;
        }

        if (valueClass.isEnum()) {
            return ((Enum<?>) value).name();
        }

        if (isPojo(value)) {
            return reflectPojoToMap(value, seen);
        }

        return value;
    }
}
