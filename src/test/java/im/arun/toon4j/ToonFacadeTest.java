package im.arun.toon4j;

import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Additional comprehensive tests for Toon facade class.
 * Ensures all public API methods are tested.
 */
class ToonFacadeTest {

    @Test
    void testEncodeNull() {
        String result = Toon.encode(null);
        assertEquals("null", result);
    }

    @Test
    void testEncodePrimitive() {
        assertEquals("123", Toon.encode(123));
        assertEquals("true", Toon.encode(true));
        assertEquals("hello", Toon.encode("hello"));
    }

    @Test
    void testEncodeWithOptions() {
        EncodeOptions options = EncodeOptions.builder()
            .indent(4)
            .build();

        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("id", 123);

        Map<String, Object> obj = new LinkedHashMap<>();
        obj.put("user", nested);

        String result = Toon.encode(obj, options);
        assertTrue(result.contains("    id: 123")); // 4 spaces
    }

    @Test
    void testDecodeSimpleValue() {
        Object result = Toon.decode("123");
        assertEquals(123, result);
    }

    @Test
    void testDecodeObject() {
        String input = "name: Ada\nage: 25";
        Object result = Toon.decode(input);

        assertTrue(result instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result;

        assertEquals("Ada", map.get("name"));
        assertEquals(25, map.get("age"));
    }

    @Test
    void testDecodeArray() {
        String input = "[3]: a,b,c";
        Object result = Toon.decode(input);

        assertTrue(result instanceof List);
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) result;

        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
    }

    @Test
    void testDecodeWithOptions() {
        DecodeOptions options = DecodeOptions.lenient(4);
        String input = "user:\n    name: Ada";

        Object result = Toon.decode(input, options);

        assertTrue(result instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result;

        assertTrue(map.containsKey("user"));
    }

    @Test
    void testRoundTripSimple() {
        Map<String, Object> original = new LinkedHashMap<>();
        original.put("name", "Ada");
        original.put("age", 25);

        String encoded = Toon.encode(original);
        Object decoded = Toon.decode(encoded);

        assertTrue(decoded instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) decoded;

        assertEquals(original.get("name"), result.get("name"));
        assertEquals(original.get("age"), result.get("age"));
    }

    @Test
    void testRoundTripArray() {
        List<String> original = List.of("a", "b", "c");

        String encoded = Toon.encode(original);
        Object decoded = Toon.decode(encoded);

        assertTrue(decoded instanceof List);
        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) decoded;

        assertEquals(original.size(), result.size());
        for (int i = 0; i < original.size(); i++) {
            assertEquals(original.get(i), result.get(i));
        }
    }

    @Test
    void testRoundTripNested() {
        Map<String, Object> address = new LinkedHashMap<>();
        address.put("city", "London");

        Map<String, Object> original = new LinkedHashMap<>();
        original.put("name", "Ada");
        original.put("address", address);
        original.put("tags", List.of("admin", "dev"));

        String encoded = Toon.encode(original);
        Object decoded = Toon.decode(encoded);

        assertTrue(decoded instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) decoded;

        assertEquals("Ada", result.get("name"));
        assertTrue(result.get("address") instanceof Map);
        assertTrue(result.get("tags") instanceof List);
    }

    @Test
    void testEncodeEmptyObject() {
        Map<String, Object> empty = new LinkedHashMap<>();
        String result = Toon.encode(empty);
        assertEquals("", result);
    }

    @Test
    void testEncodeEmptyArray() {
        List<Object> empty = List.of();
        String result = Toon.encode(empty);
        assertEquals("[0]:", result);
    }

    @Test
    void testDecodeEmptyArray() {
        String input = "[0]:";
        Object result = Toon.decode(input);

        assertTrue(result instanceof List);
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) result;
        assertTrue(list.isEmpty());
    }

    @Test
    void testEncodeWithAllDelimiters() {
        List<String> data = List.of("a", "b", "c");

        // Comma (default)
        EncodeOptions comma = EncodeOptions.builder().delimiter(Delimiter.COMMA).build();
        String resultComma = Toon.encode(data, comma);
        assertTrue(resultComma.contains(","));

        // Pipe
        EncodeOptions pipe = EncodeOptions.builder().delimiter(Delimiter.PIPE).build();
        String resultPipe = Toon.encode(data, pipe);
        assertTrue(resultPipe.contains("|"));

        // Tab
        EncodeOptions tab = EncodeOptions.builder().delimiter(Delimiter.TAB).build();
        String resultTab = Toon.encode(data, tab);
        assertTrue(resultTab.contains("\t"));
    }

    @Test
    void testEncodeCompactOption() {
        Map<String, Object> obj = new LinkedHashMap<>();
        obj.put("name", "Ada");

        EncodeOptions compact = EncodeOptions.compact();
        String result = Toon.encode(obj, compact);

        assertNotNull(result);
        assertTrue(result.contains("name: Ada"));
    }

    @Test
    void testEncodeVerboseOption() {
        Map<String, Object> obj = new LinkedHashMap<>();
        obj.put("name", "Ada");

        EncodeOptions verbose = EncodeOptions.verbose();
        String result = Toon.encode(obj, verbose);

        assertNotNull(result);
        assertTrue(result.contains("name: Ada"));
    }

    @Test
    void testDecodeNullInput() {
        Object result = Toon.decode("null");
        assertNull(result);
    }

    @Test
    void testDecodeBooleans() {
        assertEquals(true, Toon.decode("true"));
        assertEquals(false, Toon.decode("false"));
    }

    @Test
    void testDecodeNumbers() {
        assertEquals(42, Toon.decode("42"));
        assertEquals(3.14, Toon.decode("3.14"));
        assertEquals(9999999999L, Toon.decode("9999999999"));
    }

    @Test
    void testDecodeQuotedString() {
        Object result = Toon.decode("\"hello, world\"");
        assertEquals("hello, world", result);
    }

    @Test
    void testEncodeSpecialCharacters() {
        Map<String, Object> obj = new LinkedHashMap<>();
        obj.put("note", "Line1\nLine2");

        String result = Toon.encode(obj);
        assertTrue(result.contains("\\n"));
    }

    @Test
    void testDecodeEscapedCharacters() {
        String input = "note: \"Line1\\nLine2\"";
        Object result = Toon.decode(input);

        assertTrue(result instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result;

        assertEquals("Line1\nLine2", map.get("note"));
    }

    @Test
    void testRoundTripWithLengthMarker() {
        List<String> original = List.of("a", "b", "c");

        EncodeOptions options = EncodeOptions.builder()
            .lengthMarker(true)
            .build();

        String encoded = Toon.encode(original, options);
        assertTrue(encoded.contains("[#3]:"));

        Object decoded = Toon.decode(encoded);
        assertTrue(decoded instanceof List);
        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) decoded;
        assertEquals(3, result.size());
    }

    @Test
    void testEncodeTabularArray() {
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> row1 = new LinkedHashMap<>();
        row1.put("id", 1);
        row1.put("name", "Ada");
        rows.add(row1);

        Map<String, Object> row2 = new LinkedHashMap<>();
        row2.put("id", 2);
        row2.put("name", "Bob");
        rows.add(row2);

        String result = Toon.encode(rows);

        assertTrue(result.contains("[2]{id,name}:"));
        assertTrue(result.contains("1,Ada"));
        assertTrue(result.contains("2,Bob"));
    }

    @Test
    void testDecodeTabularArray() {
        String input = "[2]{id,name}:\n  1,Ada\n  2,Bob";
        Object result = Toon.decode(input);

        assertTrue(result instanceof List);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) result;

        assertEquals(2, list.size());
        assertEquals(1, list.get(0).get("id"));
        assertEquals("Ada", list.get(0).get("name"));
    }
}
