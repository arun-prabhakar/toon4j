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

    @Test
    void testKeyFoldingFlatteningCreatesDottedPath() {
        EncodeOptions options = EncodeOptions.builder()
            .flatten(true)
            .flattenDepth(5)
            .keyFolding(KeyFolding.SAFE)
            .build();

        Map<String, Object> value = new LinkedHashMap<>();
        value.put("deep", Map.of("leaf", 1));
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("root", value);

        String encoded = Encoders.encodeValue(root, options);
        assertTrue(encoded.contains("root.deep.leaf: 1"));
    }

    @Test
    void testKeyFoldingSkipsWhenLiteralPathPresent() {
        EncodeOptions options = EncodeOptions.builder()
            .flatten(true)
            .flattenDepth(5)
            .keyFolding(KeyFolding.SAFE)
            .build();

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("root.leaf", 5); // literal dotted key that should be preserved
        root.put("root", Map.of("leaf", 1));

        String encoded = Encoders.encodeValue(root, options);
        assertTrue(encoded.contains("root:"));
        assertTrue(encoded.contains("root.leaf: 5"));
    }

    @Test
    void testArrayTabularFallbackWhenHeaderMissing() {
        LineWriter writer = new LineWriter(2);
        EncodeOptions options = EncodeOptions.builder().build();

        List<Map<String, Object>> rows = List.of(
            createMap("id", 1, "name", "Ada"),
            createMap("id", 2) // missing name column -> not tabular
        );

        Encoders.encodeArray("users", rows, writer, 0, options);

        String result = writer.toString();
        assertTrue(result.contains("users[2]:"));
        assertTrue(result.contains("- id: 1"));
        assertTrue(result.contains("- id: 2"));
    }

    // ============ Additional tests for branch coverage ============

    @Test
    void testEncodeObjectAsListItemWithArrayFirstValue() {
        // Tests encodeObjectAsListItem when first value is an array (line 301+)
        LineWriter writer = new LineWriter(2);
        EncodeOptions options = EncodeOptions.builder().build();

        Map<String, Object> objWithArrayFirst = new LinkedHashMap<>();
        objWithArrayFirst.put("tags", List.of("a", "b", "c")); // Array first
        objWithArrayFirst.put("id", 1);

        List<Object> items = List.of(objWithArrayFirst);
        Encoders.encodeArray("items", items, writer, 0, options);

        String result = writer.toString();
        assertTrue(result.contains("items[1]:"));
        assertTrue(result.contains("- tags[3]: a,b,c"));
        assertTrue(result.contains("id: 1"));
    }

    @Test
    void testEncodeObjectAsListItemWithNestedObjectFirstValue() {
        // Tests encodeObjectAsListItem when first value is a nested object (line 329+)
        LineWriter writer = new LineWriter(2);
        EncodeOptions options = EncodeOptions.builder().build();

        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("city", "London");

        Map<String, Object> objWithObjectFirst = new LinkedHashMap<>();
        objWithObjectFirst.put("address", nested); // Object first
        objWithObjectFirst.put("id", 1);

        List<Object> items = List.of(objWithObjectFirst);
        Encoders.encodeArray("items", items, writer, 0, options);

        String result = writer.toString();
        assertTrue(result.contains("items[1]:"));
        assertTrue(result.contains("- address:"));
        assertTrue(result.contains("city: London"));
    }

    @Test
    void testEncodeObjectAsListItemWithEmptyNestedObject() {
        // Tests when first value is empty object (line 331-332)
        LineWriter writer = new LineWriter(2);
        EncodeOptions options = EncodeOptions.builder().build();

        Map<String, Object> objWithEmptyFirst = new LinkedHashMap<>();
        objWithEmptyFirst.put("empty", new LinkedHashMap<>()); // Empty object first
        objWithEmptyFirst.put("id", 1);

        List<Object> items = List.of(objWithEmptyFirst);
        Encoders.encodeArray("items", items, writer, 0, options);

        String result = writer.toString();
        assertTrue(result.contains("- empty:"));
    }

    @Test
    void testEncodeObjectAsListItemWithNonTabularArrayOfObjects() {
        // Tests encodeObjectAsListItem when first value is array of objects (non-tabular) (line 306-316)
        LineWriter writer = new LineWriter(2);
        EncodeOptions options = EncodeOptions.builder().build();

        // Non-tabular array of objects (different keys)
        List<Map<String, Object>> nestedArr = List.of(
            createMap("a", 1),
            createMap("b", 2, "c", 3) // different structure
        );

        Map<String, Object> objWithArrayOfObjects = new LinkedHashMap<>();
        objWithArrayOfObjects.put("items", nestedArr);
        objWithArrayOfObjects.put("id", 1);

        List<Object> items = List.of(objWithArrayOfObjects);
        Encoders.encodeArray("list", items, writer, 0, options);

        String result = writer.toString();
        assertTrue(result.contains("list[1]:"));
        assertTrue(result.contains("- items[2]:"));
    }

    @Test
    void testEncodeObjectAsListItemWithTabularArrayOfObjects() {
        // Tests encodeObjectAsListItem when first value is tabular array (line 303-311)
        LineWriter writer = new LineWriter(2);
        EncodeOptions options = EncodeOptions.builder().build();

        // Tabular array of objects
        List<Map<String, Object>> tabularArr = List.of(
            createMap("x", 1, "y", 2),
            createMap("x", 3, "y", 4)
        );

        Map<String, Object> objWithTabular = new LinkedHashMap<>();
        objWithTabular.put("coords", tabularArr);
        objWithTabular.put("id", 1);

        List<Object> items = List.of(objWithTabular);
        Encoders.encodeArray("list", items, writer, 0, options);

        String result = writer.toString();
        assertTrue(result.contains("- coords[2]{x,y}:"));
    }

    @Test
    void testEncodeObjectAsListItemWithMixedArray() {
        // Tests mixed array as first value (line 317-328)
        LineWriter writer = new LineWriter(2);
        EncodeOptions options = EncodeOptions.builder().build();

        // Mixed array (primitives + nested arrays)
        List<Object> mixedArr = List.of(
            1,
            List.of(2, 3),
            createMap("a", 1)
        );

        Map<String, Object> objWithMixed = new LinkedHashMap<>();
        objWithMixed.put("mixed", mixedArr);
        objWithMixed.put("id", 1);

        List<Object> items = List.of(objWithMixed);
        Encoders.encodeArray("list", items, writer, 0, options);

        String result = writer.toString();
        assertTrue(result.contains("- mixed[3]:"));
    }

    @Test
    void testEncodeArrayOfArraysWithNonPrimitiveInner() {
        // Tests array of arrays where inner arrays contain non-primitives (line 145-150)
        LineWriter writer = new LineWriter(2);
        EncodeOptions options = EncodeOptions.builder().build();

        List<Object> nonPrimitiveInner = new ArrayList<>();
        nonPrimitiveInner.add(1);
        nonPrimitiveInner.add(createMap("nested", true)); // non-primitive

        List<List<?>> arrays = List.of(
            List.of(1, 2),  // primitive
            (List<?>) nonPrimitiveInner  // contains non-primitive
        );

        Encoders.encodeArray("pairs", arrays, writer, 0, options);

        String result = writer.toString();
        assertTrue(result.contains("pairs[2]:"));
    }

    @Test
    void testEncodeMixedArrayWithNonPrimitiveSub() {
        // Tests mixed array fallback when item is array with non-primitives (line 275)
        LineWriter writer = new LineWriter(2);
        EncodeOptions options = EncodeOptions.builder().build();

        List<Object> innerWithObj = new ArrayList<>();
        innerWithObj.add(createMap("x", 1)); // non-primitive in inner array

        List<Object> mixed = new ArrayList<>();
        mixed.add(innerWithObj);
        mixed.add(createMap("id", 1));

        Encoders.encodeArray("items", mixed, writer, 0, options);

        String result = writer.toString();
        assertTrue(result.contains("items[2]:"));
    }

    @Test
    void testEncodeEmptyListAsFirstValue() {
        // Tests object as list item with empty keys list (line 289)
        LineWriter writer = new LineWriter(2);
        EncodeOptions options = EncodeOptions.builder().build();

        Map<String, Object> emptyObj = new LinkedHashMap<>();
        List<Object> items = List.of(emptyObj);
        Encoders.encodeArray("items", items, writer, 0, options);

        String result = writer.toString();
        assertTrue(result.contains("-")); // Just the list marker
    }

    @Test
    void testKeyFoldingWithArrayLeafValue() {
        // Tests key folding when leaf is an array (line 92)
        EncodeOptions options = EncodeOptions.builder()
            .flatten(true)
            .flattenDepth(5)
            .keyFolding(KeyFolding.SAFE)
            .build();

        Map<String, Object> inner = new LinkedHashMap<>();
        inner.put("tags", List.of("a", "b"));  // Array leaf value

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("user", inner);

        String encoded = Encoders.encodeValue(root, options);
        assertTrue(encoded.contains("user.tags[2]: a,b"));
    }

    @Test
    void testKeyFoldingWithEmptyObjectLeaf() {
        // Tests key folding when leaf is empty object (line 95)
        EncodeOptions options = EncodeOptions.builder()
            .flatten(true)
            .flattenDepth(5)
            .keyFolding(KeyFolding.SAFE)
            .build();

        Map<String, Object> empty = new LinkedHashMap<>();
        Map<String, Object> inner = new LinkedHashMap<>();
        inner.put("empty", empty);  // Empty object leaf

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("user", inner);

        String encoded = Encoders.encodeValue(root, options);
        assertTrue(encoded.contains("user.empty:"));
    }

    @Test
    void testKeyFoldingWithObjectRemainder() {
        // Tests key folding when there's a remainder object (line 100)
        EncodeOptions options = EncodeOptions.builder()
            .flatten(true)
            .flattenDepth(2)  // Limited depth
            .keyFolding(KeyFolding.SAFE)
            .build();

        Map<String, Object> deep = new LinkedHashMap<>();
        deep.put("a", 1);
        deep.put("b", 2);  // Multiple keys - becomes remainder

        Map<String, Object> mid = new LinkedHashMap<>();
        mid.put("deep", deep);

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("mid", mid);

        String encoded = Encoders.encodeValue(root, options);
        assertTrue(encoded.contains("mid.deep:"));  // Folded to depth limit
    }

    @Test
    void testDetectTabularHeaderEmptyFirstRow() {
        // Tests detectTabularHeader with empty first row keys (line 216)
        LineWriter writer = new LineWriter(2);
        EncodeOptions options = EncodeOptions.builder().build();

        List<Map<String, Object>> rows = List.of(
            new LinkedHashMap<>(),  // Empty first row
            createMap("id", 1)
        );

        Encoders.encodeArray("items", rows, writer, 0, options);

        String result = writer.toString();
        // Should fall back to list items
        assertTrue(result.contains("items[2]:"));
        assertTrue(result.contains("-")); // List item format
    }

    @Test
    void testIsTabularArrayWithNonPrimitiveValue() {
        // Tests isTabularArray returns false when value is non-primitive (line 243)
        LineWriter writer = new LineWriter(2);
        EncodeOptions options = EncodeOptions.builder().build();

        Map<String, Object> row1 = new LinkedHashMap<>();
        row1.put("id", 1);
        row1.put("data", createMap("nested", true));  // Non-primitive

        Map<String, Object> row2 = new LinkedHashMap<>();
        row2.put("id", 2);
        row2.put("data", createMap("nested", false));

        List<Map<String, Object>> rows = List.of(row1, row2);
        Encoders.encodeArray("items", rows, writer, 0, options);

        String result = writer.toString();
        // Should fall back to list items
        assertTrue(result.contains("items[2]:"));
        assertTrue(result.contains("- id: 1")); // List item format
    }

    @Test
    void testIsTabularArrayWithNullExistingKey() {
        // Tests isTabularArray with null value for existing key (line 240)
        LineWriter writer = new LineWriter(2);
        EncodeOptions options = EncodeOptions.builder().build();

        Map<String, Object> row1 = new LinkedHashMap<>();
        row1.put("id", 1);
        row1.put("name", null);  // Null value but key exists

        Map<String, Object> row2 = new LinkedHashMap<>();
        row2.put("id", 2);
        row2.put("name", "Bob");

        List<Map<String, Object>> rows = List.of(row1, row2);
        Encoders.encodeArray("items", rows, writer, 0, options);

        String result = writer.toString();
        // Should be tabular since null is valid
        assertTrue(result.contains("items[2]{id,name}:"));
    }

    @Test
    void testPathPrefixConcatenation() {
        // Tests path prefix building (line 83, 119)
        EncodeOptions options = EncodeOptions.builder()
            .flatten(true)
            .flattenDepth(10)
            .keyFolding(KeyFolding.SAFE)
            .build();

        // Deep nesting to trigger path prefix logic
        Map<String, Object> l4 = new LinkedHashMap<>();
        l4.put("val", 1);

        Map<String, Object> l3 = new LinkedHashMap<>();
        l3.put("c", l4);

        Map<String, Object> l2 = new LinkedHashMap<>();
        l2.put("b", l3);

        Map<String, Object> l1 = new LinkedHashMap<>();
        l1.put("a", l2);

        String encoded = Encoders.encodeValue(l1, options);
        assertTrue(encoded.contains("a.b.c.val: 1"));
    }

    @Test
    void testDepthBudgetCalculation() {
        // Tests depth budget handling (line 84)
        EncodeOptions options = EncodeOptions.builder()
            .flatten(true)
            .flattenDepth(2)  // Limit depth
            .keyFolding(KeyFolding.SAFE)
            .build();

        Map<String, Object> l3 = new LinkedHashMap<>();
        l3.put("x", 1);

        Map<String, Object> l2 = new LinkedHashMap<>();
        l2.put("c", l3);

        Map<String, Object> l1 = new LinkedHashMap<>();
        l1.put("b", l2);

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("a", l1);

        String encoded = Encoders.encodeValue(root, options);
        // Should fold to depth 2, then stop
        assertTrue(encoded.contains("a.b:"));
    }

    @Test
    void testBlockedKeysHandling() {
        // Tests blockedKeys null check (line 85)
        EncodeOptions options = EncodeOptions.builder()
            .flatten(true)
            .flattenDepth(5)
            .keyFolding(KeyFolding.SAFE)
            .build();

        Map<String, Object> inner = new LinkedHashMap<>();
        inner.put("x", 1);

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("a", inner);
        root.put("a.x", 2);  // Literal key that conflicts

        String encoded = Encoders.encodeValue(root, options);
        // Should preserve literal key
        assertTrue(encoded.contains("a.x: 2"));
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
