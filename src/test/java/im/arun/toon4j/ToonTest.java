package im.arun.toon4j;

import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ToonTest {

    @Test
    void testSimpleObject() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", 123);
        data.put("name", "Ada");
        data.put("active", true);

        String result = Toon.encode(data);
        String expected = "id: 123\nname: Ada\nactive: true";

        assertEquals(expected, result);
    }

    @Test
    void testNestedObject() {
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("id", 123);
        user.put("name", "Ada");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("user", user);

        String result = Toon.encode(data);
        String expected = "user:\n  id: 123\n  name: Ada";

        assertEquals(expected, result);
    }

    @Test
    void testPrimitiveArray() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("tags", List.of("admin", "ops", "dev"));

        String result = Toon.encode(data);
        String expected = "tags[3]: admin,ops,dev";

        assertEquals(expected, result);
    }

    @Test
    void testTabularArray() {
        List<Map<String, Object>> items = List.of(
            createMap("sku", "A1", "qty", 2, "price", 9.99),
            createMap("sku", "B2", "qty", 1, "price", 14.5)
        );

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("items", items);

        String result = Toon.encode(data);
        String expected = "items[2]{sku,qty,price}:\n  A1,2,9.99\n  B2,1,14.5";

        assertEquals(expected, result);
    }

    @Test
    void testEmptyObject() {
        Map<String, Object> data = new LinkedHashMap<>();
        String result = Toon.encode(data);
        assertEquals("", result);
    }

    @Test
    void testEmptyArray() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("items", List.of());

        String result = Toon.encode(data);
        String expected = "items[0]:";

        assertEquals(expected, result);
    }

    @Test
    void testNullValue() {
        String result = Toon.encode(null);
        assertEquals("null", result);
    }

    @Test
    void testQuotedStrings() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("note", "hello, world");
        data.put("path", "C:\\Users");

        String result = Toon.encode(data);
        assertTrue(result.contains("\"hello, world\""));
        assertTrue(result.contains("\"C:\\\\Users\""));
    }

    @Test
    void testNumbers() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("int", 42);
        data.put("float", 3.14);
        data.put("negative", -10);

        String result = Toon.encode(data);
        assertTrue(result.contains("int: 42"));
        assertTrue(result.contains("float: 3.14"));
        assertTrue(result.contains("negative: -10"));
    }

    @Test
    void testArrayOfArrays() {
        List<List<Integer>> pairs = List.of(
            List.of(1, 2),
            List.of(3, 4)
        );

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("pairs", pairs);

        String result = Toon.encode(data);
        String expected = "pairs[2]:\n  - [2]: 1,2\n  - [2]: 3,4";

        assertEquals(expected, result);
    }

    @Test
    void testMixedArray() {
        List<Object> items = List.of(
            1,
            createMap("a", 1),
            "text"
        );

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("items", items);

        String result = Toon.encode(data);
        assertTrue(result.contains("items[3]:"));
        assertTrue(result.contains("- 1"));
        assertTrue(result.contains("- a: 1"));
        assertTrue(result.contains("- text"));
    }

    @Test
    void testTabDelimiter() {
        List<Map<String, Object>> items = List.of(
            createMap("sku", "A1", "qty", 2),
            createMap("sku", "B2", "qty", 1)
        );

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("items", items);

        EncodeOptions options = EncodeOptions.builder()
            .delimiter(Delimiter.TAB)
            .build();

        String result = Toon.encode(data, options);
        String expected = "items[2\t]{sku\tqty}:\n  A1\t2\n  B2\t1";

        assertEquals(expected, result);
    }

    @Test
    void testPipeDelimiter() {
        List<Map<String, Object>> items = List.of(
            createMap("sku", "A1", "qty", 2),
            createMap("sku", "B2", "qty", 1)
        );

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("items", items);

        EncodeOptions options = EncodeOptions.builder()
            .delimiter(Delimiter.PIPE)
            .build();

        String result = Toon.encode(data, options);
        String expected = "items[2|]{sku|qty}:\n  A1|2\n  B2|1";

        assertEquals(expected, result);
    }

    @Test
    void testLengthMarker() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("tags", List.of("reading", "gaming", "coding"));

        EncodeOptions options = EncodeOptions.builder()
            .lengthMarker(true)
            .build();

        String result = Toon.encode(data, options);
        String expected = "tags[#3]: reading,gaming,coding";

        assertEquals(expected, result);
    }

    @Test
    void testCustomIndent() {
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("id", 123);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("user", user);

        EncodeOptions options = EncodeOptions.builder()
            .indent(4)
            .build();

        String result = Toon.encode(data, options);
        String expected = "user:\n    id: 123";

        assertEquals(expected, result);
    }

    @Test
    void testRootArray() {
        List<String> data = List.of("x", "y");
        String result = Toon.encode(data);
        String expected = "[2]: x,y";

        assertEquals(expected, result);
    }

    @Test
    void testRootTabularArray() {
        List<Map<String, Object>> data = List.of(
            createMap("id", 1),
            createMap("id", 2)
        );

        String result = Toon.encode(data);
        String expected = "[2]{id}:\n  1\n  2";

        assertEquals(expected, result);
    }

    @Test
    void testBooleanValues() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("enabled", true);
        data.put("disabled", false);

        String result = Toon.encode(data);
        assertTrue(result.contains("enabled: true"));
        assertTrue(result.contains("disabled: false"));
    }

    @Test
    void testDateConversion() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("timestamp", new Date(1700000000000L));

        String result = Toon.encode(data);
        assertTrue(result.contains("timestamp: "));
        assertTrue(result.contains("2023-11-14"));
    }

    // Helper method to create LinkedHashMap with predictable order
    private Map<String, Object> createMap(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }
}
