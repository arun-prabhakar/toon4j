package im.arun.toon4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static im.arun.toon4j.core.Constants.*;

/**
 * Parser for TOON syntax elements (array headers, primitives, keys, values).
 */
final class ToonParser {
    private ToonParser() {}

    // Patterns for type detection
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^-?\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?$");
    private static final Pattern LEADING_ZERO_PATTERN = Pattern.compile("^0\\d+$");

    /**
     * Parse a primitive token (number, boolean, null, or string).
     */
    static Object parsePrimitive(String token) {
        String trimmed = token.trim();

        // Empty token -> empty string
        if (trimmed.isEmpty()) {
            return "";
        }

        // Quoted string
        if (trimmed.startsWith(DOUBLE_QUOTE)) {
            return parseStringLiteral(trimmed);
        }

        // Boolean/null literals
        if (trimmed.equals(TRUE_LITERAL)) return true;
        if (trimmed.equals(FALSE_LITERAL)) return false;
        if (trimmed.equals(NULL_LITERAL)) return null;

        // Numeric literal
        if (isNumericLiteral(trimmed)) {
            return parseNumber(trimmed);
        }

        // Unquoted string
        return trimmed;
    }

    /**
     * Parse a quoted string literal with escape sequences.
     */
    static String parseStringLiteral(String token) {
        if (!token.startsWith(DOUBLE_QUOTE)) {
            throw new IllegalArgumentException("String literal must start with double quote");
        }

        int closingQuote = findClosingQuote(token, 0);
        if (closingQuote == -1) {
            throw new IllegalArgumentException("Unclosed string literal: " + token);
        }

        // Extract content between quotes and unescape
        String content = token.substring(1, closingQuote);
        return unescapeString(content);
    }

    /**
     * Find the closing quote, accounting for escape sequences.
     */
    static int findClosingQuote(String str, int start) {
        if (start >= str.length() || str.charAt(start) != '"') {
            return -1;
        }

        for (int i = start + 1; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '\\' && i + 1 < str.length()) {
                i++; // Skip escaped character
                continue;
            }
            if (c == '"') {
                return i;
            }
        }
        return -1;
    }

    /**
     * Unescape a string (convert \n, \t, \\, \" to actual characters).
     */
    static String unescapeString(String str) {
        StringBuilder result = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '\\' && i + 1 < str.length()) {
                char next = str.charAt(i + 1);
                switch (next) {
                    case 'n':
                        result.append('\n');
                        i++;
                        break;
                    case 't':
                        result.append('\t');
                        i++;
                        break;
                    case 'r':
                        result.append('\r');
                        i++;
                        break;
                    case '\\':
                        result.append('\\');
                        i++;
                        break;
                    case '"':
                        result.append('"');
                        i++;
                        break;
                    default:
                        result.append(c);
                }
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * Check if a token is a numeric literal.
     */
    static boolean isNumericLiteral(String token) {
        if (LEADING_ZERO_PATTERN.matcher(token).matches()) {
            return false; // "007" is not a valid number
        }
        return NUMERIC_PATTERN.matcher(token).matches();
    }

    /**
     * Parse a number from string.
     */
    static Number parseNumber(String token) {
        if (token.contains(".") || token.contains("e") || token.contains("E")) {
            return Double.parseDouble(token);
        }
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException e) {
            return Long.parseLong(token);
        }
    }

    /**
     * Parse delimited values (comma/pipe/tab separated).
     * Respects quotes and escape sequences.
     */
    static List<String> parseDelimitedValues(String input, String delimiter) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            // Handle escape sequences in quotes
            if (c == '\\' && i + 1 < input.length() && inQuotes) {
                current.append(c).append(input.charAt(i + 1));
                i++;
                continue;
            }

            // Toggle quote state
            if (c == '"') {
                inQuotes = !inQuotes;
                current.append(c);
                continue;
            }

            // Split on delimiter if not quoted
            if (!inQuotes && matchesDelimiter(input, i, delimiter)) {
                values.add(current.toString().trim());
                current.setLength(0);
                i += delimiter.length() - 1; // Skip delimiter
                continue;
            }

            current.append(c);
        }

        // Add last value
        if (current.length() > 0 || !values.isEmpty()) {
            values.add(current.toString().trim());
        }

        return values;
    }

    /**
     * Check if string matches delimiter at position.
     */
    private static boolean matchesDelimiter(String str, int pos, String delimiter) {
        if (pos + delimiter.length() > str.length()) {
            return false;
        }
        return str.startsWith(delimiter, pos);
    }

    /**
     * Parse an array header line.
     * Format: key[N]{fields}: or [N]: or key[N]:
     */
    static ArrayHeader parseArrayHeader(String line) {
        // Find bracket segment [N] or [N\t] or [N|]
        int bracketStart = line.indexOf('[');
        if (bracketStart == -1) {
            return null;
        }

        int bracketEnd = line.indexOf(']', bracketStart);
        if (bracketEnd == -1) {
            return null;
        }

        // Extract key before bracket
        String key = bracketStart > 0 ? line.substring(0, bracketStart).trim() : null;
        if (key != null && key.isEmpty()) {
            key = null;
        }

        // Parse bracket content
        String bracketContent = line.substring(bracketStart + 1, bracketEnd);
        boolean hasLengthMarker = bracketContent.startsWith("#");
        if (hasLengthMarker) {
            bracketContent = bracketContent.substring(1);
        }

        // Extract delimiter suffix
        String delimiter = COMMA;
        int length;
        if (bracketContent.endsWith("\t")) {
            delimiter = "\t";
            length = Integer.parseInt(bracketContent.substring(0, bracketContent.length() - 1));
        } else if (bracketContent.endsWith("|")) {
            delimiter = "|";
            length = Integer.parseInt(bracketContent.substring(0, bracketContent.length() - 1));
        } else {
            length = Integer.parseInt(bracketContent);
        }

        // Check for field segment {field1,field2}
        List<String> fields = null;
        int afterBracket = bracketEnd + 1;
        if (afterBracket < line.length() && line.charAt(afterBracket) == '{') {
            int fieldEnd = line.indexOf('}', afterBracket);
            if (fieldEnd != -1) {
                String fieldContent = line.substring(afterBracket + 1, fieldEnd);
                fields = parseDelimitedValues(fieldContent, delimiter);
                afterBracket = fieldEnd + 1;
            }
        }

        // Find colon
        int colonPos = line.indexOf(':', afterBracket);
        if (colonPos == -1) {
            return null;
        }

        // Extract inline values after colon
        String inlineValues = null;
        if (colonPos + 1 < line.length()) {
            inlineValues = line.substring(colonPos + 1).trim();
            if (inlineValues.isEmpty()) {
                inlineValues = null;
            }
        }

        return new ArrayHeader(key, length, delimiter, fields, hasLengthMarker, inlineValues);
    }

    /**
     * Check if line contains a key-value pair (has unquoted colon).
     */
    static boolean isKeyValueLine(String content) {
        return findUnquotedColon(content) != -1;
    }

    /**
     * Find the position of the first unquoted colon.
     */
    static int findUnquotedColon(String content) {
        boolean inQuotes = false;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '\\' && i + 1 < content.length() && inQuotes) {
                i++; // Skip escaped character
                continue;
            }
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ':' && !inQuotes) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Parse a key from the start of content (handles quoted and unquoted keys).
     */
    static String parseKey(String content) {
        content = content.trim();
        if (content.startsWith(DOUBLE_QUOTE)) {
            int closingQuote = findClosingQuote(content, 0);
            if (closingQuote == -1) {
                throw new IllegalArgumentException("Unclosed quoted key");
            }
            return unescapeString(content.substring(1, closingQuote));
        } else {
            int colonPos = findUnquotedColon(content);
            if (colonPos == -1) {
                throw new IllegalArgumentException("Missing colon after key");
            }
            return content.substring(0, colonPos).trim();
        }
    }

    /**
     * Check if content starts with array header after hyphen.
     * Used for list item detection: "- [2]: a,b"
     */
    static boolean isArrayHeaderAfterHyphen(String content) {
        return content.startsWith("[") || (content.contains("[") && content.contains("]:"));
    }

    /**
     * Check if content is an object first field after hyphen.
     * Used for list item detection: "- id: 1"
     */
    static boolean isObjectFirstFieldAfterHyphen(String content) {
        return isKeyValueLine(content);
    }

    /**
     * Array header information.
     */
    static class ArrayHeader {
        final String key;
        final int length;
        final String delimiter;
        final List<String> fields;
        final boolean hasLengthMarker;
        final String inlineValues;

        ArrayHeader(String key, int length, String delimiter, List<String> fields,
                   boolean hasLengthMarker, String inlineValues) {
            this.key = key;
            this.length = length;
            this.delimiter = delimiter;
            this.fields = fields;
            this.hasLengthMarker = hasLengthMarker;
            this.inlineValues = inlineValues;
        }

        boolean isTabular() {
            return fields != null && !fields.isEmpty();
        }
    }
}
