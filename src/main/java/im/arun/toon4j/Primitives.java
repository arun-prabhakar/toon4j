package im.arun.toon4j;

import java.util.List;
import java.util.regex.Pattern;

import static im.arun.toon4j.Constants.*;

public final class Primitives {
    private Primitives() {}

    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^-?\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?$");
    private static final Pattern LEADING_ZERO_PATTERN = Pattern.compile("^0\\d+$");
    private static final Pattern UNQUOTED_KEY_PATTERN = Pattern.compile("^[A-Z_][\\w.]*$", Pattern.CASE_INSENSITIVE);

    // Encode a primitive value
    public static String encodePrimitive(Object value, String delimiter) {
        if (value == null) {
            return NULL_LITERAL;
        }

        if (value instanceof Boolean) {
            return value.toString();
        }

        if (value instanceof Number) {
            return value.toString();
        }

        if (value instanceof String) {
            return encodeStringLiteral((String) value, delimiter);
        }

        return NULL_LITERAL;
    }

    // Encode a string literal with quoting if needed
    public static String encodeStringLiteral(String value, String delimiter) {
        if (isSafeUnquoted(value, delimiter)) {
            return value;
        }
        return DOUBLE_QUOTE + escapeString(value) + DOUBLE_QUOTE;
    }

    // Escape special characters in a string
    // Optimized: Use pooled StringBuilder and charAt() to avoid array allocation
    public static String escapeString(String value) {
        // Use pooled StringBuilder for hot path
        StringBuilder result = ObjectPool.getStringBuilder();
        result.ensureCapacity(value.length() + (value.length() / 10));

        // Use charAt() instead of toCharArray() to avoid array allocation
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\':
                    result.append("\\\\");
                    break;
                case '"':
                    result.append("\\\"");
                    break;
                case '\n':
                    result.append("\\n");
                    break;
                case '\r':
                    result.append("\\r");
                    break;
                case '\t':
                    result.append("\\t");
                    break;
                default:
                    if (c < 32) {
                        result.append(String.format("\\u%04x", (int) c));
                    } else {
                        result.append(c);
                    }
            }
        }
        String escaped = result.toString();
        ObjectPool.returnStringBuilder(result);
        return escaped;
    }

    // Check if a string can be safely used unquoted
    // Optimized: Single pass through the string instead of multiple contains() and regex calls
    public static boolean isSafeUnquoted(String value, String delimiter) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        // Check for padding with whitespace
        if (isPaddedWithWhitespace(value)) {
            return false;
        }

        // Check for boolean/null literals
        if (value.equals(TRUE_LITERAL) || value.equals(FALSE_LITERAL) || value.equals(NULL_LITERAL)) {
            return false;
        }

        // Check for numeric-like strings
        if (isNumericLike(value)) {
            return false;
        }

        // Check for list marker at start
        if (value.startsWith(LIST_ITEM_MARKER)) {
            return false;
        }

        // Single pass through string checking all special characters
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            // Check for structural characters
            if (c == ':' || c == '"' || c == '\\' ||
                c == '[' || c == ']' || c == '{' || c == '}') {
                return false;
            }

            // Check for control characters
            if (c == '\n' || c == '\r' || c == '\t' || c < 32) {
                return false;
            }
        }

        // Check for active delimiter (optimized for single-char delimiters)
        if (delimiter.length() == 1) {
            return value.indexOf(delimiter.charAt(0)) == -1;
        } else {
            return !value.contains(delimiter);
        }
    }

    private static boolean isNumericLike(String value) {
        return NUMERIC_PATTERN.matcher(value).matches() || LEADING_ZERO_PATTERN.matcher(value).matches();
    }

    private static boolean isPaddedWithWhitespace(String value) {
        return !value.equals(value.trim());
    }

    // Encode a key (object key or table header field)
    public static String encodeKey(String key) {
        if (isValidUnquotedKey(key)) {
            return key;
        }
        return DOUBLE_QUOTE + escapeString(key) + DOUBLE_QUOTE;
    }

    private static boolean isValidUnquotedKey(String key) {
        return UNQUOTED_KEY_PATTERN.matcher(key).matches();
    }

    // Join encoded values with delimiter
    // Optimized: Use pooled StringBuilder to avoid allocation
    public static String joinEncodedValues(List<?> values, String delimiter) {
        if (values.isEmpty()) {
            return "";
        }

        // Use pooled StringBuilder for hot path
        StringBuilder result = ObjectPool.getStringBuilder();
        int estimatedSize = values.size() * (10 + delimiter.length());
        result.ensureCapacity(estimatedSize);

        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                result.append(delimiter);
            }
            result.append(encodePrimitive(values.get(i), delimiter));
        }
        String joined = result.toString();
        ObjectPool.returnStringBuilder(result);
        return joined;
    }

    // Format an array header
    public static String formatHeader(int length, String key, List<String> fields, String delimiter, boolean lengthMarker) {
        StringBuilder header = new StringBuilder();

        if (key != null && !key.isEmpty()) {
            header.append(encodeKey(key));
        }

        // Add length with optional marker
        header.append("[");
        if (lengthMarker) {
            header.append("#");
        }
        header.append(length);

        // Add delimiter marker if not default (comma)
        if (!delimiter.equals(COMMA)) {
            header.append(delimiter);
        }

        header.append("]");

        // Add field list if provided
        if (fields != null && !fields.isEmpty()) {
            header.append("{");
            for (int i = 0; i < fields.size(); i++) {
                if (i > 0) {
                    header.append(delimiter);
                }
                header.append(encodeKey(fields.get(i)));
            }
            header.append("}");
        }

        header.append(":");

        return header.toString();
    }
}
