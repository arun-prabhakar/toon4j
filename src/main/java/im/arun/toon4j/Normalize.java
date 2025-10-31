package im.arun.toon4j;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

public final class Normalize {
    private Normalize() {}

    // Get converter instance (DSL-JSON)
    private static final PojoConverterFactory.PojoConverter converter = PojoConverterFactory.getInstance();

    public static Object normalizeValue(Object value) {
        if (value == null) {
            return null;
        }

        // Fast path: Most common types first (reduces average instanceof checks)

        // String (most common primitive)
        if (value instanceof String) {
            return value;
        }

        // Boolean (common primitive)
        if (value instanceof Boolean) {
            return value;
        }

        // BigInteger (must be before generic Number check since BigInteger extends Number)
        if (value instanceof BigInteger) {
            BigInteger bi = (BigInteger) value;
            // Try to convert to Long if within range
            if (bi.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) >= 0
                && bi.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0) {
                return bi.longValue();
            }
            // Otherwise return as string
            return bi.toString();
        }

        // BigDecimal (must be before generic Number check since BigDecimal extends Number)
        if (value instanceof BigDecimal) {
            BigDecimal bd = (BigDecimal) value;
            // Convert whole numbers to long for cleaner output
            if (bd.scale() <= 0 && bd.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) <= 0
                && bd.compareTo(BigDecimal.valueOf(Long.MIN_VALUE)) >= 0) {
                return bd.longValue();
            }
            return bd.doubleValue();
        }

        // Numbers (generic, after BigInteger and BigDecimal)
        if (value instanceof Number) {
            return normalizeNumber((Number) value);
        }

        // Map (common for objects)
        if (value instanceof Map) {
            Map<String, Object> normalized = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                String key = String.valueOf(entry.getKey());
                normalized.put(key, normalizeValue(entry.getValue()));
            }
            return normalized;
        }

        // Collections (common for arrays)
        if (value instanceof Collection) {
            List<Object> list = new ArrayList<>();
            for (Object item : (Collection<?>) value) {
                list.add(normalizeValue(item));
            }
            return list;
        }

        // Primitive arrays
        if (value.getClass().isArray()) {
            return normalizePrimitiveArray(value);
        }

        // Date/Time types (less common, but important)
        if (value instanceof Instant) {
            return ((Instant) value).toString();
        }
        if (value instanceof Date) {
            return ((Date) value).toInstant().toString();
        }
        if (value instanceof ZonedDateTime) {
            return ((ZonedDateTime) value).format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
        }
        if (value instanceof OffsetDateTime) {
            return ((OffsetDateTime) value).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        if (value instanceof LocalDate) {
            return ((LocalDate) value).format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        if (value instanceof LocalTime) {
            return ((LocalTime) value).format(DateTimeFormatter.ISO_LOCAL_TIME);
        }

        // Optional - unwrap or return null
        if (value instanceof Optional) {
            Optional<?> opt = (Optional<?>) value;
            return opt.map(Normalize::normalizeValue).orElse(null);
        }

        // Stream - materialize to list
        if (value instanceof Stream) {
            Stream<?> stream = (Stream<?>) value;
            List<Object> list = new ArrayList<>();
            stream.forEach(item -> list.add(normalizeValue(item)));
            return list;
        }

        // POJOs - convert to Map using DSL-JSON converter (check last, as it's expensive)
        if (converter.isPojo(value)) {
            Map<String, Object> map = converter.toMap(value);
            return normalizeValue(map);  // Recursively normalize the converted map
        }

        // Unsupported types return null
        return null;
    }

    private static Object normalizeNumber(Number number) {
        if (number instanceof Float || number instanceof Double) {
            double d = number.doubleValue();

            // Handle NaN and Infinity
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                return null;
            }

            // Canonicalize -0 to 0
            if (d == 0.0 && Double.doubleToRawLongBits(d) == Double.doubleToRawLongBits(-0.0)) {
                return 0;
            }

            // Convert whole numbers to long for cleaner output
            if (d == Math.floor(d) && !Double.isInfinite(d)
                && d <= Long.MAX_VALUE && d >= Long.MIN_VALUE) {
                long longVal = (long) d;
                // Verify no precision loss
                if (longVal == d) {
                    return longVal;
                }
            }

            return d;
        }

        return number;
    }

    /**
     * Normalizes primitive arrays to List.
     * Handles all Java primitive array types efficiently.
     */
    private static Object normalizePrimitiveArray(Object array) {
        List<Object> list = new ArrayList<>();

        if (array instanceof int[]) {
            for (int val : (int[]) array) {
                list.add(val);
            }
        } else if (array instanceof long[]) {
            for (long val : (long[]) array) {
                list.add(val);
            }
        } else if (array instanceof double[]) {
            for (double val : (double[]) array) {
                list.add(Double.isFinite(val) ? normalizeNumber(val) : null);
            }
        } else if (array instanceof float[]) {
            for (float val : (float[]) array) {
                list.add(Float.isFinite(val) ? normalizeNumber(val) : null);
            }
        } else if (array instanceof boolean[]) {
            for (boolean val : (boolean[]) array) {
                list.add(val);
            }
        } else if (array instanceof byte[]) {
            for (byte val : (byte[]) array) {
                list.add((int) val);
            }
        } else if (array instanceof short[]) {
            for (short val : (short[]) array) {
                list.add((int) val);
            }
        } else if (array instanceof char[]) {
            for (char val : (char[]) array) {
                list.add(String.valueOf(val));
            }
        } else if (array instanceof Object[]) {
            for (Object val : (Object[]) array) {
                list.add(normalizeValue(val));
            }
        }

        return list;
    }

    // Type guards
    public static boolean isJsonPrimitive(Object value) {
        return value == null
            || value instanceof String
            || value instanceof Number
            || value instanceof Boolean;
    }

    public static boolean isJsonArray(Object value) {
        return value instanceof List;
    }

    public static boolean isJsonObject(Object value) {
        return value instanceof Map;
    }

    public static boolean isArrayOfPrimitives(List<?> list) {
        for (Object item : list) {
            if (!isJsonPrimitive(item)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isArrayOfArrays(List<?> list) {
        for (Object item : list) {
            if (!isJsonArray(item)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isArrayOfObjects(List<?> list) {
        for (Object item : list) {
            if (!isJsonObject(item)) {
                return false;
            }
        }
        return true;
    }
}
