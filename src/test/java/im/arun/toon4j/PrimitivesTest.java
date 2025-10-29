package im.arun.toon4j;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PrimitivesTest {

    @Test
    void testEncodePrimitiveNull() {
        assertEquals("null", Primitives.encodePrimitive(null, ","));
    }

    @Test
    void testEncodePrimitiveBoolean() {
        assertEquals("true", Primitives.encodePrimitive(true, ","));
        assertEquals("false", Primitives.encodePrimitive(false, ","));
    }

    @Test
    void testEncodePrimitiveNumber() {
        assertEquals("42", Primitives.encodePrimitive(42, ","));
        assertEquals("3.14", Primitives.encodePrimitive(3.14, ","));
    }

    @Test
    void testEncodePrimitiveString() {
        assertEquals("hello", Primitives.encodePrimitive("hello", ","));
        assertEquals("\"hello, world\"", Primitives.encodePrimitive("hello, world", ","));
    }

    @Test
    void testEscapeString() {
        assertEquals("hello", Primitives.escapeString("hello"));
        assertEquals("\\\"quoted\\\"", Primitives.escapeString("\"quoted\""));
        assertEquals("path\\\\to\\\\file", Primitives.escapeString("path\\to\\file"));
        assertEquals("line1\\nline2", Primitives.escapeString("line1\nline2"));
    }

    @Test
    void testIsSafeUnquoted() {
        // Safe strings
        assertTrue(Primitives.isSafeUnquoted("hello", ","));
        assertTrue(Primitives.isSafeUnquoted("hello world", ","));
        assertTrue(Primitives.isSafeUnquoted("user_name", ","));

        // Unsafe strings
        assertFalse(Primitives.isSafeUnquoted("", ","));
        assertFalse(Primitives.isSafeUnquoted(" padded ", ","));
        assertFalse(Primitives.isSafeUnquoted("true", ","));
        assertFalse(Primitives.isSafeUnquoted("false", ","));
        assertFalse(Primitives.isSafeUnquoted("null", ","));
        assertFalse(Primitives.isSafeUnquoted("42", ","));
        assertFalse(Primitives.isSafeUnquoted("hello:world", ","));
        assertFalse(Primitives.isSafeUnquoted("hello,world", ","));
        assertFalse(Primitives.isSafeUnquoted("- item", ","));
    }

    @Test
    void testDelimiterAwareQuoting() {
        // Comma delimiter - comma needs quoting
        assertFalse(Primitives.isSafeUnquoted("a,b", ","));
        assertTrue(Primitives.isSafeUnquoted("a|b", ","));

        // Pipe delimiter - pipe needs quoting
        assertFalse(Primitives.isSafeUnquoted("a|b", "|"));
        assertTrue(Primitives.isSafeUnquoted("a,b", "|"));

        // Tab delimiter - tab needs quoting
        assertFalse(Primitives.isSafeUnquoted("a\tb", "\t"));
        assertTrue(Primitives.isSafeUnquoted("a,b", "\t"));
    }

    @Test
    void testEncodeKey() {
        assertEquals("id", Primitives.encodeKey("id"));
        assertEquals("userName", Primitives.encodeKey("userName"));
        assertEquals("user_name", Primitives.encodeKey("user_name"));
        assertEquals("user.name", Primitives.encodeKey("user.name"));
        assertEquals("\"user name\"", Primitives.encodeKey("user name"));
        assertEquals("\"user-name\"", Primitives.encodeKey("user-name"));
        assertEquals("\"123\"", Primitives.encodeKey("123"));
    }

    @Test
    void testFormatHeader() {
        // Simple array
        assertEquals("[3]:", Primitives.formatHeader(3, null, null, ",", false));

        // With key
        assertEquals("items[3]:", Primitives.formatHeader(3, "items", null, ",", false));

        // With fields (tabular)
        assertEquals("items[2]{id,name}:",
            Primitives.formatHeader(2, "items", java.util.List.of("id", "name"), ",", false));

        // With length marker
        assertEquals("items[#3]:", Primitives.formatHeader(3, "items", null, ",", true));

        // With tab delimiter
        assertEquals("items[3\t]:", Primitives.formatHeader(3, "items", null, "\t", false));

        // With pipe delimiter
        assertEquals("items[3|]:", Primitives.formatHeader(3, "items", null, "|", false));
    }

    @Test
    void testJoinEncodedValues() {
        java.util.List<Object> values = java.util.List.of(1, "hello", true);
        assertEquals("1,hello,true", Primitives.joinEncodedValues(values, ","));

        // When using pipe delimiter, comma doesn't need quoting
        java.util.List<Object> values2 = java.util.List.of(1, "a,b", true);
        assertEquals("1|a,b|true", Primitives.joinEncodedValues(values2, "|"));

        // But when using comma delimiter, comma DOES need quoting
        assertEquals("1,\"a,b\",true", Primitives.joinEncodedValues(values2, ","));
    }
}
