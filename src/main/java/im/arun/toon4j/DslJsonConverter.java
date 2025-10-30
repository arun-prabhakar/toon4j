package im.arun.toon4j;

import com.dslplatform.json.*;
import com.dslplatform.json.runtime.Settings;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-performance POJO to Map converter using DSL-JSON.
 * ~2x faster than Jackson, ~4x faster than Gson.
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
     * Convert a POJO to a Map using DSL-JSON.
     * Strategy: DSL-JSON serialization (very fast) + simple JSON parser (still faster overall).
     */
    @SuppressWarnings("unchecked")
    static Map<String, Object> toMap(Object pojo) {
        if (pojo == null) {
            return null;
        }

        try {
            // Serialize POJO to JSON string using DSL-JSON (this is the fast part)
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            dslJson.serialize(pojo, os);
            String json = os.toString(StandardCharsets.UTF_8.name());

            // Parse JSON string to Map using simple parser
            return (Map<String, Object>) parseJson(json);
        } catch (Exception e) {
            // Fallback: reflectively map POJO fields/getters
            // This avoids forcing users to add annotations/serializers for simple cases.
            return reflectPojoToMap(pojo);
        }
    }

    // Reflection fallback for POJO -> Map when DSL-JSON can't serialize the class
    private static Map<String, Object> reflectPojoToMap(Object pojo) {
        Class<?> cls = pojo.getClass();

        // Get or compute cached accessors
        List<Accessor> accessors = reflectionCache.computeIfAbsent(cls, DslJsonConverter::buildAccessors);

        Map<String, Object> out = new LinkedHashMap<>(accessors.size());
        for (Accessor accessor : accessors) {
            try {
                Object value = accessor.getValue(pojo);
                out.put(accessor.name, value);
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
                accessors.add(new MethodAccessor(entry.getKey(), entry.getValue()));
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
     * Simple JSON parser that converts to Map/List structures.
     * Much simpler and more reliable than using DSL-JSON's complex reader API.
     */
    private static Object parseJson(String json) {
        return new SimpleJsonParser(json).parse();
    }

    /**
     * Simple JSON parser for Map/List structures.
     * Not a full JSON parser, but sufficient for our needs.
     */
    private static class SimpleJsonParser {
        private final String json;
        private int pos = 0;

        SimpleJsonParser(String json) {
            this.json = json;
        }

        Object parse() {
            skipWhitespace();
            return parseValue();
        }

        private Object parseValue() {
            skipWhitespace();
            char c = current();

            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"') return parseString();
            if (c == 't') return parseTrue();
            if (c == 'f') return parseFalse();
            if (c == 'n') return parseNull();
            if (c == '-' || Character.isDigit(c)) return parseNumber();

            throw new RuntimeException("Unexpected character: " + c);
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            consume('{');
            skipWhitespace();

            if (current() == '}') {
                consume('}');
                return map;
            }

            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                consume(':');
                skipWhitespace();
                Object value = parseValue();
                map.put(key, value);

                skipWhitespace();
                if (current() == '}') {
                    consume('}');
                    break;
                }
                consume(',');
            }

            return map;
        }

        private List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            consume('[');
            skipWhitespace();

            if (current() == ']') {
                consume(']');
                return list;
            }

            while (true) {
                skipWhitespace();
                list.add(parseValue());
                skipWhitespace();

                if (current() == ']') {
                    consume(']');
                    break;
                }
                consume(',');
            }

            return list;
        }

        private String parseString() {
            consume('"');
            StringBuilder sb = new StringBuilder();

            while (current() != '"') {
                if (current() == '\\') {
                    pos++;
                    char escaped = current();
                    switch (escaped) {
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'b': sb.append('\b'); break;
                        case 'f': sb.append('\f'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        case 'u': // Unicode escape
                            pos++;
                            String hex = json.substring(pos, pos + 4);
                            sb.append((char) Integer.parseInt(hex, 16));
                            pos += 3; // +3 because we'll ++pos at the end
                            break;
                        default:
                            sb.append(escaped);
                    }
                } else {
                    sb.append(current());
                }
                pos++;
            }

            consume('"');
            return sb.toString();
        }

        private Number parseNumber() {
            int start = pos;
            if (current() == '-') pos++;

            while (pos < json.length() && (Character.isDigit(current()) ||
                   current() == '.' || current() == 'e' || current() == 'E' ||
                   current() == '+' || current() == '-')) {
                pos++;
            }

            String num = json.substring(start, pos);
            if (num.contains(".") || num.contains("e") || num.contains("E")) {
                return Double.parseDouble(num);
            } else {
                long val = Long.parseLong(num);
                if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) {
                    return (int) val;
                }
                return val;
            }
        }

        private Boolean parseTrue() {
            consume('t'); consume('r'); consume('u'); consume('e');
            return true;
        }

        private Boolean parseFalse() {
            consume('f'); consume('a'); consume('l'); consume('s'); consume('e');
            return false;
        }

        private Object parseNull() {
            consume('n'); consume('u'); consume('l'); consume('l');
            return null;
        }

        private void skipWhitespace() {
            while (pos < json.length() && Character.isWhitespace(current())) {
                pos++;
            }
        }

        private char current() {
            return json.charAt(pos);
        }

        private void consume(char expected) {
            if (current() != expected) {
                throw new RuntimeException("Expected '" + expected + "' but got '" + current() + "'");
            }
            pos++;
        }
    }

    /**
     * Get the DslJson instance (for future extensions).
     */
    static DslJson<Object> getDslJson() {
        return dslJson;
    }
}
