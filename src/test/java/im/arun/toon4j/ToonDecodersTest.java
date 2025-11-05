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
}
