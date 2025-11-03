package im.arun.toon4j;

import im.arun.toon4j.core.Constants;
import im.arun.toon4j.core.Primitives;

import java.util.*;

import static im.arun.toon4j.core.Constants.LIST_ITEM_PREFIX;

@SuppressWarnings("unchecked")
public final class Encoders {
    private Encoders() {}

    // Main encode entry point
    public static String encodeValue(Object value, EncodeOptions options) {
        if (Normalize.isJsonPrimitive(value)) {
            return Primitives.encodePrimitive(value, options.getDelimiterValue());
        }

        LineWriter writer = new LineWriter(options.getIndent());

        if (Normalize.isJsonArray(value)) {
            encodeArray(null, (List<?>) value, writer, 0, options);
        } else if (Normalize.isJsonObject(value)) {
            encodeObject((Map<String, Object>) value, writer, 0, options);
        }

        return writer.toString();
    }

    // Encode object
    public static void encodeObject(Map<String, Object> obj, LineWriter writer, int depth, EncodeOptions options) {
        for (Map.Entry<String, Object> entry : obj.entrySet()) {
            encodeKeyValuePair(entry.getKey(), entry.getValue(), writer, depth, options);
        }
    }

    // Encode key-value pair
    public static void encodeKeyValuePair(String key, Object value, LineWriter writer, int depth, EncodeOptions options) {
        String encodedKey = Primitives.encodeKey(key);

        if (Normalize.isJsonPrimitive(value)) {
            writer.push(depth, encodedKey + ": " + Primitives.encodePrimitive(value, options.getDelimiterValue()));
        } else if (Normalize.isJsonArray(value)) {
            encodeArray(key, (List<?>) value, writer, depth, options);
        } else if (Normalize.isJsonObject(value)) {
            Map<String, Object> nestedObj = (Map<String, Object>) value;
            if (nestedObj.isEmpty()) {
                writer.push(depth, encodedKey + ":");
            } else {
                writer.push(depth, encodedKey + ":");
                encodeObject(nestedObj, writer, depth + 1, options);
            }
        }
    }

    // Encode array
    public static void encodeArray(String key, List<?> array, LineWriter writer, int depth, EncodeOptions options) {
        if (array.isEmpty()) {
            String header = Primitives.formatHeader(0, key, null, options.getDelimiterValue(), options.hasLengthMarker());
            writer.push(depth, header);
            return;
        }

        // Primitive array
        if (Normalize.isArrayOfPrimitives(array)) {
            encodeInlinePrimitiveArray(key, array, writer, depth, options);
            return;
        }

        // Array of arrays (all primitives)
        if (Normalize.isArrayOfArrays(array)) {
            boolean allPrimitiveArrays = true;
            for (Object item : array) {
                if (!Normalize.isArrayOfPrimitives((List<?>) item)) {
                    allPrimitiveArrays = false;
                    break;
                }
            }
            if (allPrimitiveArrays) {
                encodeArrayOfArraysAsListItems(key, (List<List<?>>) (List<?>) array, writer, depth, options);
                return;
            }
        }

        // Array of objects
        if (Normalize.isArrayOfObjects(array)) {
            List<String> header = detectTabularHeader((List<Map<String, Object>>) (List<?>) array);
            if (header != null) {
                encodeArrayOfObjectsAsTabular(key, (List<Map<String, Object>>) (List<?>) array, header, writer, depth, options);
            } else {
                encodeMixedArrayAsListItems(key, array, writer, depth, options);
            }
            return;
        }

        // Mixed array: fallback to expanded format
        encodeMixedArrayAsListItems(key, array, writer, depth, options);
    }

    // Encode inline primitive array
    private static void encodeInlinePrimitiveArray(String key, List<?> values, LineWriter writer, int depth, EncodeOptions options) {
        String formatted = formatInlineArray(values, key, options);
        writer.push(depth, formatted);
    }

    private static String formatInlineArray(List<?> values, String key, EncodeOptions options) {
        String header = Primitives.formatHeader(values.size(), key, null, options.getDelimiterValue(), options.hasLengthMarker());
        if (values.isEmpty()) {
            return header;
        }
        String joinedValue = Primitives.joinEncodedValues(values, options.getDelimiterValue());
        return header + " " + joinedValue;
    }

    // Encode array of arrays as list items
    private static void encodeArrayOfArraysAsListItems(String key, List<List<?>> arrays, LineWriter writer, int depth, EncodeOptions options) {
        String header = Primitives.formatHeader(arrays.size(), key, null, options.getDelimiterValue(), options.hasLengthMarker());
        writer.push(depth, header);

        for (List<?> arr : arrays) {
            if (Normalize.isArrayOfPrimitives(arr)) {
                String inline = formatInlineArray(arr, null, options);
                writer.push(depth + 1, LIST_ITEM_PREFIX + inline);
            }
        }
    }

    // Encode array of objects as tabular
    private static void encodeArrayOfObjectsAsTabular(String key, List<Map<String, Object>> rows, List<String> header, LineWriter writer, int depth, EncodeOptions options) {
        String headerStr = Primitives.formatHeader(rows.size(), key, header, options.getDelimiterValue(), options.hasLengthMarker());
        writer.push(depth, headerStr);

        writeTabularRows(rows, header, writer, depth + 1, options);
    }

    // Detect if array can use tabular format
    private static List<String> detectTabularHeader(List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return null;
        }

        Map<String, Object> firstRow = rows.get(0);
        List<String> firstKeys = new ArrayList<>(firstRow.keySet());

        if (firstKeys.isEmpty()) {
            return null;
        }

        if (isTabularArray(rows, firstKeys)) {
            return firstKeys;
        }

        return null;
    }

    // Optimized: Avoid double Map lookup (containsKey + get)
    private static boolean isTabularArray(List<Map<String, Object>> rows, List<String> header) {
        for (Map<String, Object> row : rows) {
            // Check same number of keys
            if (row.size() != header.size()) {
                return false;
            }

            // Check all header keys exist and all values are primitives
            // Single Map lookup per key instead of containsKey + get
            for (String key : header) {
                Object value = row.get(key);
                // null value might be valid if key exists, or indicate missing key
                if (value == null && !row.containsKey(key)) {
                    return false;
                }
                if (!Normalize.isJsonPrimitive(value)) {
                    return false;
                }
            }
        }
        return true;
    }

    // Optimized: Pre-size ArrayList to avoid resizing
    private static void writeTabularRows(List<Map<String, Object>> rows, List<String> header, LineWriter writer, int depth, EncodeOptions options) {
        int headerSize = header.size();
        for (Map<String, Object> row : rows) {
            List<Object> values = new ArrayList<>(headerSize); // Pre-sized!
            for (String key : header) {
                values.add(row.get(key));
            }
            String joinedValue = Primitives.joinEncodedValues(values, options.getDelimiterValue());
            writer.push(depth, joinedValue);
        }
    }

    // Encode mixed array as list items
    private static void encodeMixedArrayAsListItems(String key, List<?> items, LineWriter writer, int depth, EncodeOptions options) {
        String header = Primitives.formatHeader(items.size(), key, null, options.getDelimiterValue(), options.hasLengthMarker());
        writer.push(depth, header);

        for (Object item : items) {
            if (Normalize.isJsonPrimitive(item)) {
                writer.push(depth + 1, LIST_ITEM_PREFIX + Primitives.encodePrimitive(item, options.getDelimiterValue()));
            } else if (Normalize.isJsonArray(item)) {
                List<?> arr = (List<?>) item;
                if (Normalize.isArrayOfPrimitives(arr)) {
                    String inline = formatInlineArray(arr, null, options);
                    writer.push(depth + 1, LIST_ITEM_PREFIX + inline);
                }
            } else if (Normalize.isJsonObject(item)) {
                encodeObjectAsListItem((Map<String, Object>) item, writer, depth + 1, options);
            }
        }
    }

    // Encode object as list item
    private static void encodeObjectAsListItem(Map<String, Object> obj, LineWriter writer, int depth, EncodeOptions options) {
        List<String> keys = new ArrayList<>(obj.keySet());
        if (keys.isEmpty()) {
            writer.push(depth, Constants.LIST_ITEM_MARKER);
            return;
        }

        // First key-value on the same line as "- "
        String firstKey = keys.get(0);
        String encodedKey = Primitives.encodeKey(firstKey);
        Object firstValue = obj.get(firstKey);

        if (Normalize.isJsonPrimitive(firstValue)) {
            writer.push(depth, LIST_ITEM_PREFIX + encodedKey + ": " + Primitives.encodePrimitive(firstValue, options.getDelimiterValue()));
        } else if (Normalize.isJsonArray(firstValue)) {
            List<?> arr = (List<?>) firstValue;
            if (Normalize.isArrayOfPrimitives(arr)) {
                String formatted = formatInlineArray(arr, firstKey, options);
                writer.push(depth, LIST_ITEM_PREFIX + formatted);
            } else if (Normalize.isArrayOfObjects(arr)) {
                List<String> header = detectTabularHeader((List<Map<String, Object>>) arr);
                if (header != null) {
                    String headerStr = Primitives.formatHeader(arr.size(), firstKey, header, options.getDelimiterValue(), options.hasLengthMarker());
                    writer.push(depth, LIST_ITEM_PREFIX + headerStr);
                    writeTabularRows((List<Map<String, Object>>) arr, header, writer, depth + 1, options);
                } else {
                    writer.push(depth, LIST_ITEM_PREFIX + encodedKey + "[" + arr.size() + "]:");
                    for (Object item : arr) {
                        if (Normalize.isJsonObject(item)) {
                            encodeObjectAsListItem((Map<String, Object>) item, writer, depth + 1, options);
                        }
                    }
                }
            } else {
                writer.push(depth, LIST_ITEM_PREFIX + encodedKey + "[" + arr.size() + "]:");
                for (Object item : arr) {
                    if (Normalize.isJsonPrimitive(item)) {
                        writer.push(depth + 1, LIST_ITEM_PREFIX + Primitives.encodePrimitive(item, options.getDelimiterValue()));
                    } else if (Normalize.isJsonArray(item) && Normalize.isArrayOfPrimitives((List<?>) item)) {
                        String inline = formatInlineArray((List<?>) item, null, options);
                        writer.push(depth + 1, LIST_ITEM_PREFIX + inline);
                    } else if (Normalize.isJsonObject(item)) {
                        encodeObjectAsListItem((Map<String, Object>) item, writer, depth + 1, options);
                    }
                }
            }
        } else if (Normalize.isJsonObject(firstValue)) {
            Map<String, Object> nestedObj = (Map<String, Object>) firstValue;
            if (nestedObj.isEmpty()) {
                writer.push(depth, LIST_ITEM_PREFIX + encodedKey + ":");
            } else {
                writer.push(depth, LIST_ITEM_PREFIX + encodedKey + ":");
                encodeObject(nestedObj, writer, depth + 2, options);
            }
        }

        // Remaining keys on indented lines
        for (int i = 1; i < keys.size(); i++) {
            String key = keys.get(i);
            encodeKeyValuePair(key, obj.get(key), writer, depth + 1, options);
        }
    }
}
