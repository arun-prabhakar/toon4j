package im.arun.toon4j;

import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for ToonDecoders class.
 * Tests decoding of primitives, objects, arrays, tabular data, and edge cases.
 */
class ToonDecodersTest {

    @Test
    void testDecodeValuePrimitive() {
        DecodeOptions options = DecodeOptions.lenient();

        String input = "123";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Object result = ToonDecoders.decodeValue(cursor, options);
        assertEquals(123, result);
    }

    @Test
    void testDecodeValueString() {
        DecodeOptions options = DecodeOptions.lenient();

        String input = "hello";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Object result = ToonDecoders.decodeValue(cursor, options);
        assertEquals("hello", result);
    }

    @Test
    void testDecodeValueBoolean() {
        DecodeOptions options = DecodeOptions.lenient();

        String input = "true";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Object result = ToonDecoders.decodeValue(cursor, options);
        assertEquals(true, result);
    }

    @Test
    void testDecodeValueNull() {
        DecodeOptions options = DecodeOptions.lenient();

        String input = "null";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Object result = ToonDecoders.decodeValue(cursor, options);
        assertNull(result);
    }

    @Test
    void testDecodeValueEmptyInput() {
        DecodeOptions options = DecodeOptions.lenient();
        LineCursor cursor = new LineCursor(new ArrayList<>());

        assertThrows(IllegalArgumentException.class,
            () -> ToonDecoders.decodeValue(cursor, options));
    }

    @Test
    void testDecodeValueRootArray() {
        DecodeOptions options = DecodeOptions.lenient();

        String input = "[3]: a,b,c";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Object result = ToonDecoders.decodeValue(cursor, options);
        assertTrue(result instanceof List);

        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) result;
        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
    }

    @Test
    void testDecodeObjectSimple() {
        DecodeOptions options = DecodeOptions.lenient();

        String input = "name: Ada\nage: 25";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Map<String, Object> result = ToonDecoders.decodeObject(cursor, 0, options);

        assertEquals(2, result.size());
        assertEquals("Ada", result.get("name"));
        assertEquals(25, result.get("age"));
    }

    @Test
    void testDecodeObjectNested() {
        DecodeOptions options = DecodeOptions.lenient();

        String input = "user:\n  name: Ada\n  age: 25";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Map<String, Object> result = ToonDecoders.decodeObject(cursor, 0, options);

        assertEquals(1, result.size());
        assertTrue(result.get("user") instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, Object> user = (Map<String, Object>) result.get("user");
        assertEquals("Ada", user.get("name"));
        assertEquals(25, user.get("age"));
    }

    @Test
    void testDecodeObjectWithArray() {
        DecodeOptions options = DecodeOptions.lenient();

        String input = "name: Ada\ntags[2]: admin,dev";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Map<String, Object> result = ToonDecoders.decodeObject(cursor, 0, options);

        assertEquals(2, result.size());
        assertEquals("Ada", result.get("name"));

        assertTrue(result.get("tags") instanceof List);
        @SuppressWarnings("unchecked")
        List<Object> tags = (List<Object>) result.get("tags");
        assertEquals(2, tags.size());
    }

    @Test
    void testDecodeObjectEmpty() {
        DecodeOptions options = DecodeOptions.lenient();

        String input = "user:";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Map<String, Object> result = ToonDecoders.decodeObject(cursor, 0, options);

        assertEquals(1, result.size());
        assertTrue(result.containsKey("user"));
    }

    @Test
    void testDecodeObjectMultipleFields() {
        DecodeOptions options = DecodeOptions.lenient();

        String input = "id: 123\nname: Ada\nactive: true\nscore: 99.5";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Map<String, Object> result = ToonDecoders.decodeObject(cursor, 0, options);

        assertEquals(4, result.size());
        assertEquals(123, result.get("id"));
        assertEquals("Ada", result.get("name"));
        assertEquals(true, result.get("active"));
        assertEquals(99.5, result.get("score"));
    }

    @Test
    void testDecodeObjectDeeplyNested() {
        DecodeOptions options = DecodeOptions.lenient();

        String input = "level1:\n  level2:\n    level3:\n      value: deep";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Map<String, Object> result = ToonDecoders.decodeObject(cursor, 0, options);

        @SuppressWarnings("unchecked")
        Map<String, Object> level1 = (Map<String, Object>) result.get("level1");
        @SuppressWarnings("unchecked")
        Map<String, Object> level2 = (Map<String, Object>) level1.get("level2");
        @SuppressWarnings("unchecked")
        Map<String, Object> level3 = (Map<String, Object>) level2.get("level3");

        assertEquals("deep", level3.get("value"));
    }

    @Test
    void testDecodeObjectWithQuotedKey() {
        DecodeOptions options = DecodeOptions.lenient();

        String input = "\"x-code\": value";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Map<String, Object> result = ToonDecoders.decodeObject(cursor, 0, options);

        assertEquals(1, result.size());
        assertEquals("value", result.get("x-code"));
    }

    @Test
    void testDecodeObjectWithQuotedValue() {
        DecodeOptions options = DecodeOptions.lenient();

        String input = "note: \"hello, world\"";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Map<String, Object> result = ToonDecoders.decodeObject(cursor, 0, options);

        assertEquals("hello, world", result.get("note"));
    }

    @Test
    void testDecodeObjectWithNumbers() {
        DecodeOptions options = DecodeOptions.lenient();

        String input = "int: 42\nlong: 9999999999\ndouble: 3.14\nfloat: 2.5";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Map<String, Object> result = ToonDecoders.decodeObject(cursor, 0, options);

        assertEquals(42, result.get("int"));
        assertEquals(9999999999L, result.get("long"));
        assertEquals(3.14, result.get("double"));
        assertEquals(2.5, result.get("float"));
    }

    @Test
    void testDecodeArrayInline() {
        DecodeOptions options = DecodeOptions.lenient();

        String input = "[3]: a,b,c";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        // decodeValue should handle the array header
        Object result = ToonDecoders.decodeValue(cursor, options);

        assertTrue(result instanceof List);
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) result;
        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
    }

    @Test
    void testDecodeArrayEmpty() {
        DecodeOptions options = DecodeOptions.lenient();

        String input = "[0]:";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Object result = ToonDecoders.decodeValue(cursor, options);

        assertTrue(result instanceof List);
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) result;
        assertTrue(list.isEmpty());
    }

    @Test
    void testDecodeArrayTabular() {
        DecodeOptions options = DecodeOptions.lenient();

        String input = "[2]{id,name}:\n  1,Ada\n  2,Bob";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Object result = ToonDecoders.decodeValue(cursor, options);

        assertTrue(result instanceof List);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) result;
        assertEquals(2, list.size());

        assertEquals(1, list.get(0).get("id"));
        assertEquals("Ada", list.get(0).get("name"));
        assertEquals(2, list.get(1).get("id"));
        assertEquals("Bob", list.get(1).get("name"));
    }

    @Test
    void testDecodeArrayOfArrays() {
        DecodeOptions options = DecodeOptions.lenient();

        String input = "[2]:\n  - [2]: 1,2\n  - [2]: 3,4";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Object result = ToonDecoders.decodeValue(cursor, options);

        assertTrue(result instanceof List);
        @SuppressWarnings("unchecked")
        List<List<Object>> list = (List<List<Object>>) result;
        assertEquals(2, list.size());

        assertEquals(1, list.get(0).get(0));
        assertEquals(2, list.get(0).get(1));
        assertEquals(3, list.get(1).get(0));
        assertEquals(4, list.get(1).get(1));
    }

    @Test
    void testDecodeComplexStructure() {
        DecodeOptions options = DecodeOptions.lenient();

        String input = """
            id: 123
            name: Ada
            tags[2]: admin,dev
            address:
              city: London
              zip: SW1A
            """.trim();

        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Map<String, Object> result = ToonDecoders.decodeObject(cursor, 0, options);

        assertEquals(123, result.get("id"));
        assertEquals("Ada", result.get("name"));

        assertTrue(result.get("tags") instanceof List);
        assertTrue(result.get("address") instanceof Map);
    }

    @Test
    void testDecodeWithCustomIndent() {
        DecodeOptions options = DecodeOptions.lenient(4);

        String input = "user:\n    name: Ada";
        List<ParsedLine> lines = Scanner.scan(input, 4, false);
        LineCursor cursor = new LineCursor(lines);

        Map<String, Object> result = ToonDecoders.decodeObject(cursor, 0, options);

        assertTrue(result.containsKey("user"));
    }

    @Test
    void testDecodeObjectAtDepth() {
        DecodeOptions options = DecodeOptions.lenient();

        String input = "  name: Ada\n  age: 25";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Map<String, Object> result = ToonDecoders.decodeObject(cursor, 0, options);

        assertEquals(2, result.size());
        assertEquals("Ada", result.get("name"));
        assertEquals(25, result.get("age"));
    }

    @Test
    void testDecodeArrayWithPipeDelimiter() {
        DecodeOptions options = DecodeOptions.lenient();

        String input = "[3|]: a|b|c";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Object result = ToonDecoders.decodeValue(cursor, options);

        assertTrue(result instanceof List);
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) result;
        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
    }

    @Test
    void testDecodeArrayWithTabDelimiter() {
        DecodeOptions options = DecodeOptions.lenient();

        String input = "[3\t]: a\tb\tc";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Object result = ToonDecoders.decodeValue(cursor, options);

        assertTrue(result instanceof List);
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) result;
        assertEquals(3, list.size());
    }

    @Test
    void testDecodeTabularWithNulls() {
        DecodeOptions options = DecodeOptions.lenient();

        String input = "[2]{id,name}:\n  1,Ada\n  2,null";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Object result = ToonDecoders.decodeValue(cursor, options);

        assertTrue(result instanceof List);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) result;
        assertEquals(2, list.size());
        assertNull(list.get(1).get("name"));
    }

    @Test
    void testDecodeListItems() {
        DecodeOptions options = DecodeOptions.lenient();

        String input = "[3]:\n  - 1\n  - 2\n  - 3";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Object result = ToonDecoders.decodeValue(cursor, options);

        assertTrue(result instanceof List);
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) result;
        assertEquals(3, list.size());
        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals(3, list.get(2));
    }

    @Test
    void testDecodeListItemsWithObjects() {
        DecodeOptions options = DecodeOptions.lenient();

        String input = "[2]:\n  - id: 1\n    name: Ada\n  - id: 2\n    name: Bob";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Object result = ToonDecoders.decodeValue(cursor, options);

        assertTrue(result instanceof List);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) result;
        assertEquals(2, list.size());

        assertEquals(1, list.get(0).get("id"));
        assertEquals("Ada", list.get(0).get("name"));
    }

    // ===== Branch coverage tests =====

    @Test
    void testDecodeObjectWithMissingColon() {
        DecodeOptions options = DecodeOptions.lenient();
        String input = "invalid line without colon";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        // Should throw when line has no colon
        assertThrows(IllegalArgumentException.class, () -> ToonDecoders.decodeObject(cursor, 0, options));
    }

    @Test
    void testDecodeInlineArrayStrictModeCountMismatch() {
        DecodeOptions options = DecodeOptions.strict();
        String input = "[5]: a,b,c"; // declares 5 but only 3 values
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> ToonDecoders.decodeValue(cursor, options));
        assertTrue(ex.getMessage().contains("Expected 5 items"));
    }

    @Test
    void testDecodeTabularArrayStrictModeCountMismatch() {
        DecodeOptions options = DecodeOptions.strict();
        String input = "[5]{id,name}:\n  1,Ada\n  2,Bob"; // declares 5 but only 2 rows
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> ToonDecoders.decodeValue(cursor, options));
        assertTrue(ex.getMessage().contains("Expected 5 rows"));
    }

    @Test
    void testDecodeTabularArrayFieldCountMismatch() {
        DecodeOptions options = DecodeOptions.lenient();
        String input = "[2]{id,name,extra}:\n  1,Ada\n  2,Bob"; // declares 3 fields but rows have only 2
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> ToonDecoders.decodeValue(cursor, options));
        assertTrue(ex.getMessage().contains("Expected 3 fields"));
    }

    @Test
    void testDecodeListArrayStrictModeCountMismatch() {
        DecodeOptions options = DecodeOptions.strict();
        String input = "[5]:\n  - a\n  - b"; // declares 5 but only 2 items
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> ToonDecoders.decodeValue(cursor, options));
        assertTrue(ex.getMessage().contains("Expected 5 items"));
    }

    @Test
    void testDecodeListArrayInvalidItem() {
        DecodeOptions options = DecodeOptions.lenient();
        String input = "[2]:\n  not a list item\n  - valid"; // first item doesn't start with -
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> ToonDecoders.decodeValue(cursor, options));
        assertTrue(ex.getMessage().contains("must start with"));
    }

    @Test
    void testDecodeListItemEmptyMarker() {
        DecodeOptions options = DecodeOptions.lenient();
        String input = "[1]:\n  -"; // single hyphen only
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Object result = ToonDecoders.decodeValue(cursor, options);
        assertTrue(result instanceof List);

        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) result;
        assertEquals(1, list.size());
        assertTrue(list.get(0) instanceof Map);
        assertTrue(((Map<?, ?>) list.get(0)).isEmpty());
    }

    @Test
    void testDecodeListItemEmptyAfterHyphen() {
        DecodeOptions options = DecodeOptions.lenient();
        String input = "[1]:\n  - "; // hyphen with space but no content
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Object result = ToonDecoders.decodeValue(cursor, options);
        assertTrue(result instanceof List);

        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) result;
        assertEquals(1, list.size());
        assertTrue(list.get(0) instanceof Map);
    }

    @Test
    void testDecodeListItemWithQuotedKey() {
        DecodeOptions options = DecodeOptions.lenient();
        String input = "[1]:\n  - \"x-header\": value";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Object result = ToonDecoders.decodeValue(cursor, options);
        assertTrue(result instanceof List);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) result;
        assertEquals(1, list.size());
        assertEquals("value", list.get(0).get("x-header"));
    }

    @Test
    void testDecodeListItemWithNestedObject() {
        DecodeOptions options = DecodeOptions.lenient();
        String input = "[1]:\n  - parent:\n      child: nested";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Object result = ToonDecoders.decodeValue(cursor, options);
        assertTrue(result instanceof List);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) result;
        assertEquals(1, list.size());

        @SuppressWarnings("unchecked")
        Map<String, Object> parent = (Map<String, Object>) list.get(0).get("parent");
        assertEquals("nested", parent.get("child"));
    }

    @Test
    void testDecodeObjectWithQuotedKeyColon() {
        DecodeOptions options = DecodeOptions.lenient();
        String input = "\"key:with:colons\": value";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Map<String, Object> result = ToonDecoders.decodeObject(cursor, 0, options);
        assertEquals("value", result.get("key:with:colons"));
    }

    @Test
    void testDecodeTabularArrayDepthBreak() {
        DecodeOptions options = DecodeOptions.lenient();
        // Tabular array where rows don't continue at proper depth
        String input = "[3]{id,name}:\n  1,Ada\nnextKey: val"; // second line is at depth 0
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Object result = ToonDecoders.decodeValue(cursor, options);
        assertTrue(result instanceof List);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) result;
        assertEquals(1, list.size()); // Only one row parsed before depth break
    }

    @Test
    void testDecodeListArrayDepthBreak() {
        DecodeOptions options = DecodeOptions.lenient();
        String input = "[3]:\n  - a\nanotherKey: b"; // list item followed by shallow key
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Object result = ToonDecoders.decodeValue(cursor, options);
        assertTrue(result instanceof List);

        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) result;
        assertEquals(1, list.size()); // Only one item before depth break
    }

    @Test
    void testDecodeObjectDepthBreak() {
        DecodeOptions options = DecodeOptions.lenient();
        String input = "outer:\n  inner: val\nsibling: other";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Map<String, Object> result = ToonDecoders.decodeObject(cursor, 0, options);

        assertEquals(2, result.size());
        assertTrue(result.get("outer") instanceof Map);
        assertEquals("other", result.get("sibling"));
    }

    @Test
    void testDecodeKeyValueWithNestedArrayHeader() {
        DecodeOptions options = DecodeOptions.lenient();
        String input = "items[2]:\n  - first\n  - second";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Map<String, Object> result = ToonDecoders.decodeObject(cursor, 0, options);

        assertTrue(result.get("items") instanceof List);
        @SuppressWarnings("unchecked")
        List<Object> items = (List<Object>) result.get("items");
        assertEquals(2, items.size());
    }

    @Test
    void testDecodeObjectWithDifferentComputedDepth() {
        DecodeOptions options = DecodeOptions.lenient();
        // Object at depth 1 where baseDepth is 0
        String input = "  name: Ada\n  age: 25";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Map<String, Object> result = ToonDecoders.decodeObject(cursor, 0, options);
        assertEquals(2, result.size());
    }

    @Test
    void testDecodeValueWithKeyedArrayHeader() {
        DecodeOptions options = DecodeOptions.lenient();
        // Root array with key prefix (should not be root array)
        String input = "key[3]: a,b,c";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Object result = ToonDecoders.decodeValue(cursor, options);
        assertTrue(result instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result;
        assertTrue(map.get("key") instanceof List);
    }

    @Test
    void testDecodeListItemWithNestedArray() {
        DecodeOptions options = DecodeOptions.lenient();
        String input = "[1]:\n  - [2]: x,y"; // List item contains inline array
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Object result = ToonDecoders.decodeValue(cursor, options);
        assertTrue(result instanceof List);

        @SuppressWarnings("unchecked")
        List<Object> outer = (List<Object>) result;
        assertTrue(outer.get(0) instanceof List);

        @SuppressWarnings("unchecked")
        List<Object> inner = (List<Object>) outer.get(0);
        assertEquals(2, inner.size());
        assertEquals("x", inner.get(0));
    }

    @Test
    void testDecodeListItemObjectWithManyFields() {
        DecodeOptions options = DecodeOptions.lenient();
        String input = "[1]:\n  - a: 1\n    b: 2\n    c: 3";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Object result = ToonDecoders.decodeValue(cursor, options);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) result;
        assertEquals(1, list.size());
        assertEquals(3, list.get(0).size());
        assertEquals(1, list.get(0).get("a"));
        assertEquals(2, list.get(0).get("b"));
        assertEquals(3, list.get(0).get("c"));
    }

    @Test
    void testDecodeInlineArrayStrictModeSuccess() {
        DecodeOptions options = DecodeOptions.strict();
        String input = "[3]: a,b,c"; // correct count
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Object result = ToonDecoders.decodeValue(cursor, options);
        assertTrue(result instanceof List);
        assertEquals(3, ((List<?>) result).size());
    }

    @Test
    void testDecodeTabularArrayStrictModeSuccess() {
        DecodeOptions options = DecodeOptions.strict();
        String input = "[2]{id,name}:\n  1,Ada\n  2,Bob";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Object result = ToonDecoders.decodeValue(cursor, options);
        assertTrue(result instanceof List);
        assertEquals(2, ((List<?>) result).size());
    }

    @Test
    void testDecodeListArrayStrictModeSuccess() {
        DecodeOptions options = DecodeOptions.strict();
        String input = "[2]:\n  - a\n  - b";
        List<ParsedLine> lines = Scanner.scan(input, 2, false);
        LineCursor cursor = new LineCursor(lines);

        Object result = ToonDecoders.decodeValue(cursor, options);
        assertTrue(result instanceof List);
        assertEquals(2, ((List<?>) result).size());
    }
}
