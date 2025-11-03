package im.arun.toon4j;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test decode functionality.
 */
class DecodeTest {

    @Test
    void testDecodePrimitives() {
        // Strings
        assertEquals("hello", Toon.decode("hello"));
        assertEquals("", Toon.decode("\"\""));
        assertEquals("true", Toon.decode("\"true\""));  // Quoted = string
        assertEquals("42", Toon.decode("\"42\""));      // Quoted = string

        // Numbers
        assertEquals(42, Toon.decode("42"));
        assertEquals(-3.14, Toon.decode("-3.14"));
        assertEquals(1e-6, Toon.decode("1e-6"));

        // Booleans
        assertEquals(true, Toon.decode("true"));
        assertEquals(false, Toon.decode("false"));

        // Null
        assertNull(Toon.decode("null"));
    }

    @Test
    void testDecodeEscapedStrings() {
        assertEquals("line1\nline2", Toon.decode("\"line1\\nline2\""));
        assertEquals("tab\there", Toon.decode("\"tab\\there\""));
        assertEquals("quote\"here", Toon.decode("\"quote\\\"here\""));
        assertEquals("back\\slash", Toon.decode("\"back\\\\slash\""));
        assertEquals("C:\\Users\\path", Toon.decode("\"C:\\\\Users\\\\path\""));
    }

    @Test
    void testDecodeSimpleObject() {
        String toon = """
            id: 123
            name: Ada
            active: true
            """;

        Map<?, ?> result = (Map<?, ?>) Toon.decode(toon);
        assertEquals(123, result.get("id"));
        assertEquals("Ada", result.get("name"));
        assertEquals(true, result.get("active"));
    }

    @Test
    void testDecodeNestedObject() {
        String toon = """
            user:
              id: 123
              name: Ada
            status: active
            """;

        Map<?, ?> result = (Map<?, ?>) Toon.decode(toon);
        Map<?, ?> user = (Map<?, ?>) result.get("user");
        assertEquals(123, user.get("id"));
        assertEquals("Ada", user.get("name"));
        assertEquals("active", result.get("status"));
    }

    @Test
    void testDecodeQuotedKeys() {
        String toon = """
            \"order:id\": 123
            \"user name\": Ada
            """;

        Map<?, ?> result = (Map<?, ?>) Toon.decode(toon);
        assertEquals(123, result.get("order:id"));
        assertEquals("Ada", result.get("user name"));
    }

    @Test
    void testDecodeInlineArray() {
        String toon = "tags[3]: reading,gaming,coding";

        Map<?, ?> result = (Map<?, ?>) Toon.decode(toon);
        List<?> tags = (List<?>) result.get("tags");
        assertEquals(3, tags.size());
        assertEquals("reading", tags.get(0));
        assertEquals("gaming", tags.get(1));
        assertEquals("coding", tags.get(2));
    }

    @Test
    void testDecodeRootInlineArray() {
        String toon = "[3]: a,b,c";

        List<?> result = (List<?>) Toon.decode(toon);
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
        assertEquals("c", result.get(2));
    }

    @Test
    void testDecodeTabularArray() {
        String toon = """
            items[2]{sku,qty}:
              A1,2
              B2,1
            """;

        Map<?, ?> result = (Map<?, ?>) Toon.decode(toon);
        List<?> items = (List<?>) result.get("items");
        assertEquals(2, items.size());

        Map<?, ?> item1 = (Map<?, ?>) items.get(0);
        assertEquals("A1", item1.get("sku"));
        assertEquals(2, item1.get("qty"));

        Map<?, ?> item2 = (Map<?, ?>) items.get(1);
        assertEquals("B2", item2.get("sku"));
        assertEquals(1, item2.get("qty"));
    }

    @Test
    void testDecodeListArray() {
        String toon = """
            items[2]:
              - id: 1
                name: First
              - id: 2
                name: Second
            """;

        Map<?, ?> result = (Map<?, ?>) Toon.decode(toon);
        List<?> items = (List<?>) result.get("items");
        assertEquals(2, items.size());

        Map<?, ?> item1 = (Map<?, ?>) items.get(0);
        assertEquals(1, item1.get("id"));
        assertEquals("First", item1.get("name"));

        Map<?, ?> item2 = (Map<?, ?>) items.get(1);
        assertEquals(2, item2.get("id"));
        assertEquals("Second", item2.get("name"));
    }

    @Test
    void testDecodeListArrayPrimitives() {
        String toon = """
            items[3]:
              - 42
              - hello
              - true
            """;

        Map<?, ?> result = (Map<?, ?>) Toon.decode(toon);
        List<?> items = (List<?>) result.get("items");
        assertEquals(3, items.size());
        assertEquals(42, items.get(0));
        assertEquals("hello", items.get(1));
        assertEquals(true, items.get(2));
    }

    @Test
    void testDecodeNestedArrays() {
        String toon = """
            pairs[2]:
              - [2]: a,b
              - [2]: c,d
            """;

        Map<?, ?> result = (Map<?, ?>) Toon.decode(toon);
        List<?> pairs = (List<?>) result.get("pairs");
        assertEquals(2, pairs.size());

        List<?> pair1 = (List<?>) pairs.get(0);
        assertEquals(2, pair1.size());
        assertEquals("a", pair1.get(0));
        assertEquals("b", pair1.get(1));

        List<?> pair2 = (List<?>) pairs.get(1);
        assertEquals(2, pair2.size());
        assertEquals("c", pair2.get(0));
        assertEquals("d", pair2.get(1));
    }

    @Test
    void testDecodeCustomDelimiter() {
        String toon = "tags[3|]: reading|gaming|coding";

        Map<?, ?> result = (Map<?, ?>) Toon.decode(toon);
        List<?> tags = (List<?>) result.get("tags");
        assertEquals(3, tags.size());
        assertEquals("reading", tags.get(0));
        assertEquals("gaming", tags.get(1));
        assertEquals("coding", tags.get(2));
    }

    @Test
    void testDecodeTabDelimiter() {
        String toon = "items[2\t]: a\tb";

        Map<?, ?> result = (Map<?, ?>) Toon.decode(toon);
        List<?> items = (List<?>) result.get("items");
        assertEquals(2, items.size());
        assertEquals("a", items.get(0));
        assertEquals("b", items.get(1));
    }

    @Test
    void testDecodeQuotedValuesWithDelimiter() {
        String toon = "items[2]: \"a,b\",c";

        Map<?, ?> result = (Map<?, ?>) Toon.decode(toon);
        List<?> items = (List<?>) result.get("items");
        assertEquals(2, items.size());
        assertEquals("a,b", items.get(0));
        assertEquals("c", items.get(1));
    }

    @Test
    void testDecodeComplexNested() {
        String toon = """
            user:
              id: 123
              name: Ada
              tags[2]: reading,gaming
              profile:
                age: 30
                location: SF
            status: active
            """;

        Map<?, ?> result = (Map<?, ?>) Toon.decode(toon);
        Map<?, ?> user = (Map<?, ?>) result.get("user");
        assertEquals(123, user.get("id"));
        assertEquals("Ada", user.get("name"));

        List<?> tags = (List<?>) user.get("tags");
        assertEquals(2, tags.size());

        Map<?, ?> profile = (Map<?, ?>) user.get("profile");
        assertEquals(30, profile.get("age"));
        assertEquals("SF", profile.get("location"));

        assertEquals("active", result.get("status"));
    }

    @Test
    void testDecodeStrictCountValidation() {
        String toon = "items[3]: a,b";  // Declared 3, but only 2 values

        assertThrows(IllegalArgumentException.class, () -> {
            Toon.decode(toon);
        });
    }

    @Test
    void testDecodeLenientCountValidation() {
        String toon = "items[3]: a,b";  // Declared 3, but only 2 values

        DecodeOptions options = DecodeOptions.lenient();
        Map<?, ?> result = (Map<?, ?>) Toon.decode(toon, options);
        List<?> items = (List<?>) result.get("items");
        assertEquals(2, items.size());  // Lenient mode accepts mismatch
    }

    @Test
    void testDecodeEmptyInput() {
        assertThrows(IllegalArgumentException.class, () -> {
            Toon.decode("");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            Toon.decode("   ");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            Toon.decode(null);
        });
    }

    @Test
    void testDecodeRoundTrip() {
        Map<String, Object> original = Map.of(
            "id", 123,
            "name", "Ada",
            "tags", List.of("reading", "gaming"),
            "active", true
        );

        String encoded = Toon.encode(original);
        Map<?, ?> decoded = (Map<?, ?>) Toon.decode(encoded);

        assertEquals(123, decoded.get("id"));
        assertEquals("Ada", decoded.get("name"));
        assertEquals(true, decoded.get("active"));

        List<?> tags = (List<?>) decoded.get("tags");
        assertEquals(2, tags.size());
        assertEquals("reading", tags.get(0));
        assertEquals("gaming", tags.get(1));
    }

    @Test
    void testHandlesEmptyListItem() {
        String toon = "items[2]:\n  -\n  - name: second";
        Map<?, ?> result = (Map<?, ?>) Toon.decode(toon);
        List<?> items = (List<?>) result.get("items");

        assertEquals(2, items.size());

        // Check first item is an empty map
        Object firstItem = items.get(0);
        assertTrue(firstItem instanceof Map);
        assertTrue(((Map<?, ?>) firstItem).isEmpty());

        // Check second item is as expected
        Object secondItem = items.get(1);
        assertTrue(secondItem instanceof Map);
        assertEquals("second", ((Map<?, ?>) secondItem).get("name"));
    }
}