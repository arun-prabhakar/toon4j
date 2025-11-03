package im.arun.toon4j;

import java.util.*;

import static im.arun.toon4j.core.Constants.*;
import static im.arun.toon4j.ToonParser.*;

/**
 * Core decoding logic for TOON format.
 */
final class ToonDecoders {
    private ToonDecoders() {}

    /**
     * Decode value from lines (entry point).
     */
    static Object decodeValue(LineCursor cursor, DecodeOptions options) {
        if (cursor.atEnd()) {
            throw new IllegalArgumentException("Cannot decode empty input");
        }

        ParsedLine first = cursor.peek();

        // Check for root array: [N]: values (only if no key before bracket)
        ArrayHeader header = parseArrayHeader(first.content);
        if (header != null && header.key == null) {
            cursor.advance();
            return decodeArray(header, cursor, 0, options);
        }

        // Single primitive value
        if (cursor.size() == 1 && !isKeyValueLine(first.content)) {
            return parsePrimitive(first.content.trim());
        }

        // Default to object
        return decodeObject(cursor, 0, options);
    }

    /**
     * Decode an object from lines at given depth.
     */
    static Map<String, Object> decodeObject(LineCursor cursor, int baseDepth, DecodeOptions options) {
        Map<String, Object> obj = new LinkedHashMap<>();

        while (!cursor.atEnd()) {
            ParsedLine line = cursor.peek();
            if (line == null || line.depth < baseDepth) {
                break; // End of object
            }

            if (line.depth == baseDepth) {
                // Parse key-value pair
                decodeKeyValuePair(line, cursor, baseDepth, obj, options);
            } else {
                break; // Deeper level - will be handled by nested decode
            }
        }

        return obj;
    }

    /**
     * Decode a key-value pair and add to object.
     */
    private static void decodeKeyValuePair(ParsedLine line, LineCursor cursor, int baseDepth,
                                           Map<String, Object> obj, DecodeOptions options) {
        String content = line.content;

        cursor.advance();

        // Check for array header first (special case: key[N]: or key[N]{fields}:)
        ArrayHeader header = parseArrayHeader(content);
        if (header != null && header.key != null) {
            // This is an array value with a key
            Object value = decodeArray(header, cursor, baseDepth, options);
            obj.put(header.key, value);
            return;
        }

        // Normal key-value pair
        String key = parseKey(content);

        // Find colon position
        int colonPos = content.startsWith(DOUBLE_QUOTE)
            ? content.indexOf(':', findClosingQuote(content, 0) + 1)
            : findUnquotedColon(content);

        if (colonPos == -1) {
            throw new IllegalArgumentException("Line " + line.lineNumber + ": Missing colon after key");
        }

        // Extract value after colon
        String valueStr = content.substring(colonPos + 1).trim();

        // Determine value type
        Object value;

        // Check for nested object (next line is deeper)
        if (valueStr.isEmpty() && !cursor.atEnd() && cursor.peek().depth > baseDepth) {
            value = decodeObject(cursor, baseDepth + 1, options);
        }
        // Primitive value
        else {
            value = parsePrimitive(valueStr);
        }

        obj.put(key, value);
    }

    /**
     * Decode an array from header.
     */
    private static Object decodeArray(ArrayHeader header, LineCursor cursor, int baseDepth, DecodeOptions options) {
        // Inline array: [3]: a,b,c
        if (header.inlineValues != null) {
            return decodeInlineArray(header, options);
        }

        // Tabular array: [2]{sku,qty}: rows
        if (header.isTabular()) {
            return decodeTabularArray(header, cursor, baseDepth, options);
        }

        // List array: [2]: list items
        return decodeListArray(header, cursor, baseDepth, options);
    }

    /**
     * Decode inline array: [3]: a,b,c
     */
    private static List<Object> decodeInlineArray(ArrayHeader header, DecodeOptions options) {
        List<String> tokens = parseDelimitedValues(header.inlineValues, header.delimiter);
        List<Object> result = new ArrayList<>(tokens.size());

        for (String token : tokens) {
            result.add(parsePrimitive(token));
        }

        // Validate count in strict mode
        if (options.isStrict() && result.size() != header.length) {
            throw new IllegalArgumentException(
                String.format("Expected %d items, but got %d", header.length, result.size())
            );
        }

        return result;
    }

    /**
     * Decode tabular array: [2]{sku,qty}:
     *   A1,2
     *   B2,1
     */
    private static List<Map<String, Object>> decodeTabularArray(ArrayHeader header, LineCursor cursor,
                                                                 int baseDepth, DecodeOptions options) {
        List<Map<String, Object>> result = new ArrayList<>(header.length);
        List<String> fields = header.fields;

        while (!cursor.atEnd() && result.size() < header.length) {
            ParsedLine line = cursor.peek();

            // Check depth
            if (line.depth < baseDepth + 1) {
                break;
            }

            if (line.depth == baseDepth + 1) {
                // Parse row
                List<String> values = parseDelimitedValues(line.content, header.delimiter);

                if (values.size() != fields.size()) {
                    throw new IllegalArgumentException(
                        String.format("Line %d: Expected %d fields, got %d",
                            line.lineNumber, fields.size(), values.size())
                    );
                }

                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 0; i < fields.size(); i++) {
                    row.put(fields.get(i), parsePrimitive(values.get(i)));
                }

                result.add(row);
                cursor.advance();
            } else {
                break;
            }
        }

        // Validate count in strict mode
        if (options.isStrict() && result.size() != header.length) {
            throw new IllegalArgumentException(
                String.format("Expected %d rows, but got %d", header.length, result.size())
            );
        }

        return result;
    }

    /**
     * Decode list array: [2]:
     *   - item1
     *   - item2
     */
    private static List<Object> decodeListArray(ArrayHeader header, LineCursor cursor,
                                                int baseDepth, DecodeOptions options) {
        List<Object> result = new ArrayList<>(header.length);

        while (!cursor.atEnd() && result.size() < header.length) {
            ParsedLine line = cursor.peek();

            // Check depth
            if (line.depth < baseDepth + 1) {
                break;
            }

            if (line.depth == baseDepth + 1) {
                // Check for valid list item start
                boolean isListItem = line.content.startsWith(LIST_ITEM_PREFIX) || line.content.equals(LIST_ITEM_MARKER);
                if (!isListItem) {
                    throw new IllegalArgumentException(
                        "Line " + line.lineNumber + ": List item must start with '- ' or be a single '-'");
                }

                Object item = decodeListItem(cursor, baseDepth + 1, header.delimiter, options);
                result.add(item);
            } else {
                break;
            }
        }

        // Validate count in strict mode
        if (options.isStrict() && result.size() != header.length) {
            throw new IllegalArgumentException(
                String.format("Expected %d items, but got %d", header.length, result.size()));
        }

        return result;
    }

    /**
     * Decode a single list item: "- value" or "- key: value" or "- [2]: a,b"
     */
    private static Object decodeListItem(LineCursor cursor, int baseDepth, String delimiter, DecodeOptions options) {
        ParsedLine line = cursor.next();
        String content = line.content;

        // Case 1: Empty list item "-"
        if (content.equals(LIST_ITEM_MARKER)) {
            return new LinkedHashMap<String, Object>();
        }

        // At this point, it must be "- "
        String afterHyphen = content.substring(LIST_ITEM_PREFIX.length());

        // Case 2: Empty content after "- ", e.g. "- \n"
        if (afterHyphen.trim().isEmpty()) {
            return new LinkedHashMap<String, Object>();
        }

        // Check for nested array header: "- [2]: a,b"
        ArrayHeader arrayHeader = parseArrayHeader(afterHyphen);
        if (arrayHeader != null) {
            return decodeArray(arrayHeader, cursor, baseDepth, options);
        }

        // Check for object first field: "- id: 1"
        if (isObjectFirstFieldAfterHyphen(afterHyphen)) {
            return decodeObjectFromListItem(line, afterHyphen, cursor, baseDepth, options);
        }

        // Primitive value: "- 42"
        return parsePrimitive(afterHyphen);
    }

    /**
     * Decode object starting from list item line: "- key: value"
     */
    private static Map<String, Object> decodeObjectFromListItem(ParsedLine firstLine, String afterHyphen,
                                                                LineCursor cursor, int baseDepth, DecodeOptions options) {
        Map<String, Object> obj = new LinkedHashMap<>();

        // Parse first key-value from "- key: value"
        String key = parseKey(afterHyphen);
        int colonPos = afterHyphen.startsWith(DOUBLE_QUOTE)
            ? afterHyphen.indexOf(':', findClosingQuote(afterHyphen, 0) + 1)
            : findUnquotedColon(afterHyphen);

        String valueStr = afterHyphen.substring(colonPos + 1).trim();

        // Check if value is on next line (nested)
        if (valueStr.isEmpty() && !cursor.atEnd() && cursor.peek().depth > baseDepth) {
            obj.put(key, decodeObject(cursor, baseDepth + 1, options));
        } else {
            obj.put(key, parsePrimitive(valueStr));
        }

        // Parse remaining fields at depth baseDepth + 1 (indented relative to "- ")
        while (!cursor.atEnd()) {
            ParsedLine line = cursor.peek();
            if (line.depth < baseDepth + 1) {
                break;
            }
            if (line.depth == baseDepth + 1) {
                decodeKeyValuePair(line, cursor, baseDepth + 1, obj, options);
            } else {
                break;
            }
        }

        return obj;
    }
}
