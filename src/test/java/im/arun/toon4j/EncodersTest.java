package im.arun.toon4j;

import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for Encoders class.
 * Tests encoding of primitives, objects, arrays, tabular data, and edge cases.
 */
class EncodersTest {

    @Test
    void testEncodeValuePrimitive() {
        EncodeOptions options = EncodeOptions.builder().build();

        assertEquals("123", Encoders.encodeValue(123, options));
        assertEquals("true", Encoders.encodeValue(true, options));
        assertEquals("null", Encoders.encodeValue(null, options));
        assertEquals("hello", Encoders.encodeValue("hello", options));
    }

    @Test
    void testEncodeValueEmptyObject() {
        EncodeOptions options = EncodeOptions.builder().build();
        Map<String, Object> empty = new LinkedHashMap<>();

        assertEquals("", Encoders.encodeValue(empty, options));
    }

    @Test
    void testEncodeValueSimpleObject() {
        EncodeOptions options = EncodeOptions.builder().build();
        Map<String, Object> obj = new LinkedHashMap<>();
        obj.put("name", "Ada");
        obj.put("age", 25);

        String result = Encoders.encodeValue(obj, options);
        assertTrue(result.contains("name: Ada"));
        assertTrue(result.contains("age: 25"));
    }

    @Test
    void testEncodeValuePrimitiveArray() {
        EncodeOptions options = EncodeOptions.builder().build();
        List<String> array = List.of("a", "b", "c");

        String result = Encoders.encodeValue(array, options);
        assertEquals("[3]: a,b,c", result);
    }

    @Test
    void testEncodeValueEmptyArray() {
        EncodeOptions options = EncodeOptions.builder().build();
        List<Object> empty = List.of();

        String result = Encoders.encodeValue(empty, options);
        assertEquals("[0]:", result);
    }

    @Test
    void testEncodeObjectSingleField() {
        LineWriter writer = new LineWriter(2);
        EncodeOptions options = EncodeOptions.builder().build();

        Map<String, Object> obj = new LinkedHashMap<>();
        obj.put("id", 123);

        Encoders.encodeObject(obj, writer, 0, options);
        assertEquals("id: 123", writer.toString());
    }

    @Test
    void testEncodeObjectMultipleFields() {
        LineWriter writer = new LineWriter(2);
        EncodeOptions options = EncodeOptions.builder().build();

        Map<String, Object> obj = new LinkedHashMap<>();
        obj.put("id", 123);
        obj.put("name", "Ada");
        obj.put("active", true);

        Encoders.encodeObject(obj, writer, 0, options);

        String result = writer.toString();
        assertTrue(result.contains("id: 123"));
        assertTrue(result.contains("name: Ada"));
        assertTrue(result.contains("active: true"));
    }

    @Test
    void testEncodeObjectWithDepth() {
        LineWriter writer = new LineWriter(2);
        EncodeOptions options = EncodeOptions.builder().build();

        Map<String, Object> obj = new LinkedHashMap<>();
        obj.put("id", 123);

        Encoders.encodeObject(obj, writer, 2, options);

        String result = writer.toString();
        assertTrue(result.startsWith("    ")); // 2 depth = 4 spaces
    }

    @Test
    void testEncodeKeyValuePairPrimitive() {
        LineWriter writer = new LineWriter(2);
        EncodeOptions options = EncodeOptions.builder().build();

        Encoders.encodeKeyValuePair("name", "Ada", writer, 0, options);
        assertEquals("name: Ada", writer.toString());
    }

    @Test
    void testEncodeKeyValuePairNestedObject() {
        LineWriter writer = new LineWriter(2);
        EncodeOptions options = EncodeOptions.builder().build();

        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("city", "London");

        Encoders.encodeKeyValuePair("address", nested, writer, 0, options);

        String result = writer.toString();
        assertTrue(result.contains("address:"));
        assertTrue(result.contains("city: London"));
    }

    @Test
    void testEncodeKeyValuePairEmptyObject() {
        LineWriter writer = new LineWriter(2);
        EncodeOptions options = EncodeOptions.builder().build();

        Map<String, Object> empty = new LinkedHashMap<>();
        Encoders.encodeKeyValuePair("empty", empty, writer, 0, options);

        assertEquals("empty:", writer.toString());
    }

    @Test
    void testEncodeKeyValuePairArray() {
        LineWriter writer = new LineWriter(2);
        EncodeOptions options = EncodeOptions.builder().build();

        List<String> tags = List.of("admin", "dev");
        Encoders.encodeKeyValuePair("tags", tags, writer, 0, options);

        String result = writer.toString();
        assertTrue(result.contains("tags[2]: admin,dev"));
    }

    @Test
    void testEncodeArrayPrimitives() {
        LineWriter writer = new LineWriter(2);
        EncodeOptions options = EncodeOptions.builder().build();

        List<Integer> numbers = List.of(1, 2, 3);
        Encoders.encodeArray("nums", numbers, writer, 0, options);

        assertEquals("nums[3]: 1,2,3", writer.toString());
    }

    @Test
    void testEncodeArrayOfArrays() {
        LineWriter writer = new LineWriter(2);
        EncodeOptions options = EncodeOptions.builder().build();

        List<List<Integer>> arrays = List.of(
            List.of(1, 2),
            List.of(3, 4)
        );

        Encoders.encodeArray("pairs", arrays, writer, 0, options);

        String result = writer.toString();
        assertTrue(result.contains("pairs[2]:"));
        assertTrue(result.contains("- [2]: 1,2"));
        assertTrue(result.contains("- [2]: 3,4"));
    }

    @Test
    void testEncodeArrayTabular() {
        LineWriter writer = new LineWriter(2);
        EncodeOptions options = EncodeOptions.builder().build();

        List<Map<String, Object>> rows = List.of(
            createMap("id", 1, "name", "Ada"),
            createMap("id", 2, "name", "Bob")
        );

        Encoders.encodeArray("users", rows, writer, 0, options);

        String result = writer.toString();
        assertTrue(result.contains("users[2]{id,name}:"));
        assertTrue(result.contains("1,Ada"));
        assertTrue(result.contains("2,Bob"));
    }

    @Test
    void testEncodeArrayTabularWithNullValues() {
        LineWriter writer = new LineWriter(2);
        EncodeOptions options = EncodeOptions.builder().build();

        List<Map<String, Object>> rows = List.of(
            createMap("id", 1, "name", "Ada"),
            createMap("id", 2, "name", null)
        );

        Encoders.encodeArray("users", rows, writer, 0, options);

        String result = writer.toString();
        assertTrue(result.contains("2,null"));
    }

    @Test
    void testEncodeArrayAllPrimitivesInline() {
        LineWriter writer = new LineWriter(2);
        EncodeOptions options = EncodeOptions.builder().build();

        // All primitives - will use inline format
        List<Object> allPrimitives = List.of(1, "text", true);

        Encoders.encodeArray("items", allPrimitives, writer, 0, options);

        String result = writer.toString();
        // All primitives should be encoded inline
        assertTrue(result.contains("items[3]: 1,text,true"));
    }

    @Test
    void testEncodeArrayMixedTypes() {
        LineWriter writer = new LineWriter(2);
        EncodeOptions options = EncodeOptions.builder().build();

        // Mix of primitives and objects - will use list item format
        List<Object> mixed = List.of(
            1,
            createMap("a", 1),
            "text"
        );

        Encoders.encodeArray("items", mixed, writer, 0, options);

        String result = writer.toString();
        assertTrue(result.contains("items[3]:"));
        assertTrue(result.contains("- 1"));
        assertTrue(result.contains("- a: 1"));
        assertTrue(result.contains("- text"));
    }

    @Test
    void testEncodeArrayRootLevel() {
        LineWriter writer = new LineWriter(2);
        EncodeOptions options = EncodeOptions.builder().build();

        List<String> array = List.of("x", "y");
        Encoders.encodeArray(null, array, writer, 0, options);

        assertEquals("[2]: x,y", writer.toString());
    }

    @Test
    void testEncodeWithoutLegacyLengthMarker() {
        EncodeOptions options = EncodeOptions.builder().build();

        List<String> array = List.of("a", "b", "c");
        String result = Encoders.encodeValue(array, options);

        assertTrue(result.contains("[3]:"));
        assertFalse(result.contains("#"));
    }

    @Test
    void testEncodeWithPipeDelimiter() {
        EncodeOptions options = EncodeOptions.builder()
            .delimiter(Delimiter.PIPE)
            .build();

        List<String> array = List.of("a", "b", "c");
        String result = Encoders.encodeValue(array, options);

        assertTrue(result.contains("[3|]: a|b|c"));
    }

    @Test
    void testEncodeWithTabDelimiter() {
        EncodeOptions options = EncodeOptions.builder()
            .delimiter(Delimiter.TAB)
            .build();

        List<String> array = List.of("a", "b", "c");
        String result = Encoders.encodeValue(array, options);

        assertTrue(result.contains("\t"));
    }

    @Test
    void testEncodeWithCustomIndent() {
        EncodeOptions options = EncodeOptions.builder()
            .indent(4)
            .build();

        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("id", 123);

        Map<String, Object> obj = new LinkedHashMap<>();
        obj.put("user", nested);

        String result = Encoders.encodeValue(obj, options);
        assertTrue(result.contains("    id: 123")); // 4 spaces
    }

    @Test
    void testEncodeComplexNestedStructure() {
        EncodeOptions options = EncodeOptions.builder().build();

        Map<String, Object> address = new LinkedHashMap<>();
        address.put("city", "London");
        address.put("zip", "SW1A");

        Map<String, Object> user = new LinkedHashMap<>();
        user.put("id", 123);
        user.put("name", "Ada");
        user.put("address", address);
        user.put("tags", List.of("admin", "dev"));

        String result = Encoders.encodeValue(user, options);

        assertTrue(result.contains("id: 123"));
        assertTrue(result.contains("name: Ada"));
        assertTrue(result.contains("address:"));
        assertTrue(result.contains("city: London"));
        assertTrue(result.contains("tags[2]: admin,dev"));
    }

    @Test
    void testEncodeArrayOfObjectsNonTabular() {
        LineWriter writer = new LineWriter(2);
        EncodeOptions options = EncodeOptions.builder().build();

        // Different field counts - not tabular
        List<Map<String, Object>> rows = List.of(
            createMap("id", 1),
            createMap("id", 2, "name", "Bob")
        );

        Encoders.encodeArray("items", rows, writer, 0, options);

        String result = writer.toString();
        assertTrue(result.contains("items[2]:"));
        assertTrue(result.contains("- id: 1"));
        assertTrue(result.contains("- id: 2"));
    }

    @Test
    void testEncodeEmptyArrayWithKey() {
        LineWriter writer = new LineWriter(2);
        EncodeOptions options = EncodeOptions.builder().build();

        List<Object> empty = List.of();
        Encoders.encodeArray("empty", empty, writer, 0, options);

        assertEquals("empty[0]:", writer.toString());
    }

    @Test
    void testEncodeArrayOfMixedObjects() {
        LineWriter writer = new LineWriter(2);
        EncodeOptions options = EncodeOptions.builder().build();

        List<Object> mixed = List.of(
            createMap("id", 1),
            "string",
            List.of(1, 2)
        );

        Encoders.encodeArray("items", mixed, writer, 0, options);

        String result = writer.toString();
        assertTrue(result.contains("items[3]:"));
        assertTrue(result.contains("- id: 1"));
        assertTrue(result.contains("- string"));
        assertTrue(result.contains("- [2]: 1,2"));
    }

    @Test
    void testEncodeQuotedKeys() {
        LineWriter writer = new LineWriter(2);
        EncodeOptions options = EncodeOptions.builder().build();

        Map<String, Object> obj = new LinkedHashMap<>();
        obj.put("x-code", "value");

        Encoders.encodeObject(obj, writer, 0, options);

        String result = writer.toString();
        assertTrue(result.contains("\"x-code\": value"));
    }

    @Test
    void testEncodeSpecialCharactersInValues() {
        EncodeOptions options = EncodeOptions.builder().build();

        Map<String, Object> obj = new LinkedHashMap<>();
        obj.put("note", "hello, world");

        String result = Encoders.encodeValue(obj, options);
        assertTrue(result.contains("\"hello, world\""));
    }

    @Test
    void testEncodeNumberTypes() {
        EncodeOptions options = EncodeOptions.builder().build();

        Map<String, Object> obj = new LinkedHashMap<>();
        obj.put("int", 42);
        obj.put("long", 9999999999L);
        obj.put("double", 3.14);
        obj.put("float", 2.5f);

        String result = Encoders.encodeValue(obj, options);
        assertTrue(result.contains("int: 42"));
        assertTrue(result.contains("long: 9999999999"));
        assertTrue(result.contains("double: 3.14"));
    }

    @Test
    void testEncodeDeeplyNestedStructure() {
        EncodeOptions options = EncodeOptions.builder().build();

        Map<String, Object> level3 = new LinkedHashMap<>();
        level3.put("value", "deep");

        Map<String, Object> level2 = new LinkedHashMap<>();
        level2.put("level3", level3);

        Map<String, Object> level1 = new LinkedHashMap<>();
        level1.put("level2", level2);

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("level1", level1);

        String result = Encoders.encodeValue(root, options);

        assertTrue(result.contains("level1:"));
        assertTrue(result.contains("level2:"));
        assertTrue(result.contains("level3:"));
        assertTrue(result.contains("value: deep"));
    }

    @Test
    void testEncodeArrayWithNestedArrays() {
        LineWriter writer = new LineWriter(2);
        EncodeOptions options = EncodeOptions.builder().build();

        List<Object> items = List.of(
            List.of(1, 2),
            List.of(3, 4, 5)
        );

        Encoders.encodeArray("matrix", items, writer, 0, options);

        String result = writer.toString();
        assertTrue(result.contains("matrix[2]:"));
        assertTrue(result.contains("- [2]: 1,2"));
        assertTrue(result.contains("- [3]: 3,4,5"));
    }

    // Helper method
    private Map<String, Object> createMap(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }
}
