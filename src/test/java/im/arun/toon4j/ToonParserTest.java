package im.arun.toon4j;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for ToonParser class.
 * Tests primitive parsing, string escaping, array headers, and key-value detection.
 */
class ToonParserTest {

    // ========== parsePrimitive Tests ==========

    @Test
    void testParsePrimitiveNull() {
        assertEquals(null, ToonParser.parsePrimitive("null"));
        assertEquals(null, ToonParser.parsePrimitive("  null  "));
    }

    @Test
    void testParsePrimitiveBoolean() {
        assertEquals(true, ToonParser.parsePrimitive("true"));
        assertEquals(false, ToonParser.parsePrimitive("false"));
        assertEquals(true, ToonParser.parsePrimitive("  true  "));
    }

    @Test
    void testParsePrimitiveInteger() {
        assertEquals(42, ToonParser.parsePrimitive("42"));
        assertEquals(-10, ToonParser.parsePrimitive("-10"));
        assertEquals(0, ToonParser.parsePrimitive("0"));
    }

    @Test
    void testParsePrimitiveLong() {
        long large = 9_999_999_999L;
        assertEquals(large, ToonParser.parsePrimitive("9999999999"));
    }

    @Test
    void testParsePrimitiveDouble() {
        assertEquals(3.14, ToonParser.parsePrimitive("3.14"));
        assertEquals(-2.5, ToonParser.parsePrimitive("-2.5"));
        assertEquals(0.0, ToonParser.parsePrimitive("0.0"));
    }

    @Test
    void testParsePrimitiveScientificNotation() {
        assertEquals(1.5e10, ToonParser.parsePrimitive("1.5e10"));
        assertEquals(2.0E-5, ToonParser.parsePrimitive("2.0E-5"));
        assertEquals(-3.14e+2, ToonParser.parsePrimitive("-3.14e+2"));
    }

    @Test
    void testParsePrimitiveQuotedString() {
        assertEquals("hello", ToonParser.parsePrimitive("\"hello\""));
        assertEquals("hello, world", ToonParser.parsePrimitive("\"hello, world\""));
    }

    @Test
    void testParsePrimitiveUnquotedString() {
        assertEquals("text", ToonParser.parsePrimitive("text"));
        assertEquals("hello", ToonParser.parsePrimitive("hello"));
    }

    @Test
    void testParsePrimitiveEmptyString() {
        assertEquals("", ToonParser.parsePrimitive(""));
        assertEquals("", ToonParser.parsePrimitive("   "));
    }

    @Test
    void testParsePrimitiveLeadingZeros() {
        // "007" should be treated as string, not number
        assertEquals("007", ToonParser.parsePrimitive("007"));
        assertEquals("00123", ToonParser.parsePrimitive("00123"));
    }

    // ========== parseStringLiteral Tests ==========

    @Test
    void testParseStringLiteralSimple() {
        assertEquals("hello", ToonParser.parseStringLiteral("\"hello\""));
        assertEquals("", ToonParser.parseStringLiteral("\"\""));
    }

    @Test
    void testParseStringLiteralWithEscapes() {
        assertEquals("hello\nworld", ToonParser.parseStringLiteral("\"hello\\nworld\""));
        assertEquals("tab\there", ToonParser.parseStringLiteral("\"tab\\there\""));
        assertEquals("quote\"test", ToonParser.parseStringLiteral("\"quote\\\"test\""));
        assertEquals("back\\slash", ToonParser.parseStringLiteral("\"back\\\\slash\""));
        assertEquals("return\r", ToonParser.parseStringLiteral("\"return\\r\""));
    }

    @Test
    void testParseStringLiteralUnclosed() {
        assertThrows(IllegalArgumentException.class,
            () -> ToonParser.parseStringLiteral("\"unclosed"));
    }

    @Test
    void testParseStringLiteralNoQuote() {
        assertThrows(IllegalArgumentException.class,
            () -> ToonParser.parseStringLiteral("noquote"));
    }

    // ========== findClosingQuote Tests ==========

    @Test
    void testFindClosingQuoteSimple() {
        assertEquals(5, ToonParser.findClosingQuote("\"test\"", 0));
        assertEquals(1, ToonParser.findClosingQuote("\"\"", 0));
    }

    @Test
    void testFindClosingQuoteWithEscapes() {
        assertEquals(8, ToonParser.findClosingQuote("\"a\\\"b\\\"c\"", 0)); // String has 9 chars, closing quote at index 8
        assertEquals(7, ToonParser.findClosingQuote("\"\\n\\t\\r\"", 0));
    }

    @Test
    void testFindClosingQuoteNotFound() {
        assertEquals(-1, ToonParser.findClosingQuote("\"unclosed", 0));
        assertEquals(-1, ToonParser.findClosingQuote("noquote", 0));
    }

    @Test
    void testFindClosingQuoteInvalidStart() {
        assertEquals(-1, ToonParser.findClosingQuote("test", 0));
        assertEquals(-1, ToonParser.findClosingQuote("\"test\"", 10));
    }

    // ========== unescapeString Tests ==========

    @Test
    void testUnescapeStringSimple() {
        assertEquals("hello", ToonParser.unescapeString("hello"));
    }

    @Test
    void testUnescapeStringEscapeSequences() {
        assertEquals("\n", ToonParser.unescapeString("\\n"));
        assertEquals("\t", ToonParser.unescapeString("\\t"));
        assertEquals("\r", ToonParser.unescapeString("\\r"));
        assertEquals("\\", ToonParser.unescapeString("\\\\"));
        assertEquals("\"", ToonParser.unescapeString("\\\""));
    }

    @Test
    void testUnescapeStringMultipleEscapes() {
        assertEquals("a\nb\tc", ToonParser.unescapeString("a\\nb\\tc"));
        assertEquals("\"hello\"", ToonParser.unescapeString("\\\"hello\\\""));
    }

    @Test
    void testUnescapeStringInvalidEscape() {
        // Invalid escape sequences are kept as-is
        assertEquals("\\x", ToonParser.unescapeString("\\x"));
        assertEquals("\\z", ToonParser.unescapeString("\\z"));
    }

    // ========== isNumericLiteral Tests ==========

    @Test
    void testIsNumericLiteralInteger() {
        assertTrue(ToonParser.isNumericLiteral("123"));
        assertTrue(ToonParser.isNumericLiteral("-456"));
        assertTrue(ToonParser.isNumericLiteral("0"));
    }

    @Test
    void testIsNumericLiteralDouble() {
        assertTrue(ToonParser.isNumericLiteral("3.14"));
        assertTrue(ToonParser.isNumericLiteral("-2.5"));
        assertTrue(ToonParser.isNumericLiteral("0.0"));
    }

    @Test
    void testIsNumericLiteralScientific() {
        assertTrue(ToonParser.isNumericLiteral("1.5e10"));
        assertTrue(ToonParser.isNumericLiteral("2E-5"));
        assertTrue(ToonParser.isNumericLiteral("-3.14e+2"));
    }

    @Test
    void testIsNumericLiteralLeadingZeros() {
        assertFalse(ToonParser.isNumericLiteral("007"));
        assertFalse(ToonParser.isNumericLiteral("00123"));
    }

    @Test
    void testIsNumericLiteralInvalid() {
        assertFalse(ToonParser.isNumericLiteral("abc"));
        assertFalse(ToonParser.isNumericLiteral("12.34.56"));
        assertFalse(ToonParser.isNumericLiteral("1e"));
    }

    // ========== parseNumber Tests ==========

    @Test
    void testParseNumberInteger() {
        assertEquals(42, ToonParser.parseNumber("42"));
        assertEquals(-10, ToonParser.parseNumber("-10"));
    }

    @Test
    void testParseNumberLong() {
        assertEquals(9_999_999_999L, ToonParser.parseNumber("9999999999"));
    }

    @Test
    void testParseNumberDouble() {
        assertEquals(3.14, ToonParser.parseNumber("3.14"));
        assertEquals(-2.5, ToonParser.parseNumber("-2.5"));
    }

    @Test
    void testParseNumberScientific() {
        assertEquals(1.5e10, ToonParser.parseNumber("1.5e10"));
        assertEquals(2.0E-5, ToonParser.parseNumber("2.0E-5"));
    }

    // ========== parseDelimitedValues Tests ==========

    @Test
    void testParseDelimitedValuesComma() {
        List<String> result = ToonParser.parseDelimitedValues("a,b,c", ",");
        assertEquals(List.of("a", "b", "c"), result);
    }

    @Test
    void testParseDelimitedValuesPipe() {
        List<String> result = ToonParser.parseDelimitedValues("a|b|c", "|");
        assertEquals(List.of("a", "b", "c"), result);
    }

    @Test
    void testParseDelimitedValuesTab() {
        List<String> result = ToonParser.parseDelimitedValues("a\tb\tc", "\t");
        assertEquals(List.of("a", "b", "c"), result);
    }

    @Test
    void testParseDelimitedValuesWithQuotes() {
        List<String> result = ToonParser.parseDelimitedValues("\"a,b\",c,d", ",");
        assertEquals(List.of("\"a,b\"", "c", "d"), result);
    }

    @Test
    void testParseDelimitedValuesWithEscapes() {
        List<String> result = ToonParser.parseDelimitedValues("\"a\\\"b\",c", ",");
        assertEquals(List.of("\"a\\\"b\"", "c"), result);
    }

    @Test
    void testParseDelimitedValuesWithSpaces() {
        List<String> result = ToonParser.parseDelimitedValues("a , b , c", ",");
        assertEquals(List.of("a", "b", "c"), result); // Trimmed
    }

    @Test
    void testParseDelimitedValuesSingle() {
        List<String> result = ToonParser.parseDelimitedValues("single", ",");
        assertEquals(List.of("single"), result);
    }

    @Test
    void testParseDelimitedValuesEmpty() {
        List<String> result = ToonParser.parseDelimitedValues("", ",");
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseDelimitedValuesTrailingDelimiter() {
        List<String> result = ToonParser.parseDelimitedValues("a,b,", ",");
        assertEquals(List.of("a", "b", ""), result);
    }

    // ========== parseArrayHeader Tests ==========

    @Test
    void testParseArrayHeaderSimple() {
        ToonParser.ArrayHeader header = ToonParser.parseArrayHeader("[3]:");
        assertNull(header.key);
        assertEquals(3, header.length);
        assertEquals(",", header.delimiter);
        assertNull(header.fields);
        assertFalse(header.hasLengthMarker);
    }

    @Test
    void testParseArrayHeaderWithKey() {
        ToonParser.ArrayHeader header = ToonParser.parseArrayHeader("items[5]:");
        assertEquals("items", header.key);
        assertEquals(5, header.length);
    }

    @Test
    void testParseArrayHeaderWithFields() {
        ToonParser.ArrayHeader header = ToonParser.parseArrayHeader("items[2]{sku,qty,price}:");
        assertEquals("items", header.key);
        assertEquals(2, header.length);
        assertEquals(List.of("sku", "qty", "price"), header.fields);
        assertTrue(header.isTabular());
    }

    @Test
    void testParseArrayHeaderWithInlineValues() {
        ToonParser.ArrayHeader header = ToonParser.parseArrayHeader("[3]: a,b,c");
        assertEquals(3, header.length);
        assertEquals("a,b,c", header.inlineValues);
    }

    @Test
    void testParseArrayHeaderWithLengthMarker() {
        ToonParser.ArrayHeader header = ToonParser.parseArrayHeader("tags[#3]:");
        assertEquals("tags", header.key);
        assertEquals(3, header.length);
        assertTrue(header.hasLengthMarker);
    }

    @Test
    void testParseArrayHeaderWithPipeDelimiter() {
        ToonParser.ArrayHeader header = ToonParser.parseArrayHeader("items[2|]{a|b}:");
        assertEquals(2, header.length);
        assertEquals("|", header.delimiter);
        assertEquals(List.of("a", "b"), header.fields);
    }

    @Test
    void testParseArrayHeaderWithTabDelimiter() {
        ToonParser.ArrayHeader header = ToonParser.parseArrayHeader("items[2\t]{a\tb}:");
        assertEquals(2, header.length);
        assertEquals("\t", header.delimiter);
    }

    @Test
    void testParseArrayHeaderWithQuotedKey() {
        ToonParser.ArrayHeader header = ToonParser.parseArrayHeader("\"x-code\"[2]:");
        assertEquals("x-code", header.key);
        assertEquals(2, header.length);
    }

    @Test
    void testParseArrayHeaderInvalid() {
        assertNull(ToonParser.parseArrayHeader("not an array"));
        assertNull(ToonParser.parseArrayHeader("missing bracket"));
        assertNull(ToonParser.parseArrayHeader("[unclosed"));
    }

    // ========== isKeyValueLine Tests ==========

    @Test
    void testIsKeyValueLineTrue() {
        assertTrue(ToonParser.isKeyValueLine("name: Ada"));
        assertTrue(ToonParser.isKeyValueLine("age: 25"));
        assertTrue(ToonParser.isKeyValueLine("  nested: value  "));
    }

    @Test
    void testIsKeyValueLineWithQuotedKey() {
        assertTrue(ToonParser.isKeyValueLine("\"key-name\": value"));
        assertTrue(ToonParser.isKeyValueLine("\"x-code[0]\": test"));
    }

    @Test
    void testIsKeyValueLineFalse() {
        assertFalse(ToonParser.isKeyValueLine("no colon"));
        assertFalse(ToonParser.isKeyValueLine("\"unclosed"));
    }

    @Test
    void testIsKeyValueLineWithColonInQuotes() {
        assertTrue(ToonParser.isKeyValueLine("\"a:b\": value")); // Colon in key but also after
    }

    // ========== findUnquotedColon Tests ==========

    @Test
    void testFindUnquotedColonSimple() {
        assertEquals(4, ToonParser.findUnquotedColon("name: value"));
        assertEquals(3, ToonParser.findUnquotedColon("age: 25"));
    }

    @Test
    void testFindUnquotedColonNotFound() {
        assertEquals(-1, ToonParser.findUnquotedColon("no colon"));
        assertEquals(-1, ToonParser.findUnquotedColon(""));
    }

    @Test
    void testFindUnquotedColonInQuotes() {
        // Colon inside quotes should be ignored
        int pos = ToonParser.findUnquotedColon("\"a:b\": value");
        assertEquals(5, pos); // Colon at position 5 (after closing quote)
    }

    @Test
    void testFindUnquotedColonWithEscapes() {
        String content = "\"escaped\\\":\": value";
        int pos = ToonParser.findUnquotedColon(content);
        assertTrue(pos > 0);
    }

    // ========== parseKey Tests ==========

    @Test
    void testParseKeySimple() {
        assertEquals("name", ToonParser.parseKey("name: Ada"));
        assertEquals("age", ToonParser.parseKey("age: 25"));
    }

    @Test
    void testParseKeyQuoted() {
        assertEquals("x-code", ToonParser.parseKey("\"x-code\": value"));
        assertEquals("key with space", ToonParser.parseKey("\"key with space\": val"));
    }

    @Test
    void testParseKeyWithEscapes() {
        assertEquals("key\"quote", ToonParser.parseKey("\"key\\\"quote\": value"));
    }

    @Test
    void testParseKeyMissingColon() {
        assertThrows(IllegalArgumentException.class,
            () -> ToonParser.parseKey("nocolon"));
    }

    @Test
    void testParseKeyUnclosedQuote() {
        assertThrows(IllegalArgumentException.class,
            () -> ToonParser.parseKey("\"unclosed: value"));
    }

    // ========== isArrayHeaderAfterHyphen Tests ==========

    @Test
    void testIsArrayHeaderAfterHyphenTrue() {
        assertTrue(ToonParser.isArrayHeaderAfterHyphen("[2]: a,b"));
        assertTrue(ToonParser.isArrayHeaderAfterHyphen("key[3]:"));
    }

    @Test
    void testIsArrayHeaderAfterHyphenFalse() {
        assertFalse(ToonParser.isArrayHeaderAfterHyphen("just text"));
        assertFalse(ToonParser.isArrayHeaderAfterHyphen("name: value"));
    }

    // ========== isObjectFirstFieldAfterHyphen Tests ==========

    @Test
    void testIsObjectFirstFieldAfterHyphenTrue() {
        assertTrue(ToonParser.isObjectFirstFieldAfterHyphen("id: 123"));
        assertTrue(ToonParser.isObjectFirstFieldAfterHyphen("name: Ada"));
    }

    @Test
    void testIsObjectFirstFieldAfterHyphenFalse() {
        assertFalse(ToonParser.isObjectFirstFieldAfterHyphen("no colon"));
        // Note: "[2]:" contains a colon so isKeyValueLine returns true
        // This is expected behavior for the parser
    }

    // ========== ArrayHeader Tests ==========

    @Test
    void testArrayHeaderIsTabular() {
        ToonParser.ArrayHeader tabular = new ToonParser.ArrayHeader(
            "items", 2, ",", List.of("a", "b"), false, null
        );
        assertTrue(tabular.isTabular());

        ToonParser.ArrayHeader notTabular = new ToonParser.ArrayHeader(
            "items", 2, ",", null, false, null
        );
        assertFalse(notTabular.isTabular());
    }

    @Test
    void testArrayHeaderFields() {
        ToonParser.ArrayHeader header = new ToonParser.ArrayHeader(
            "users", 3, ",", List.of("id", "name", "age"), true, null
        );

        assertEquals("users", header.key);
        assertEquals(3, header.length);
        assertEquals(",", header.delimiter);
        assertEquals(List.of("id", "name", "age"), header.fields);
        assertTrue(header.hasLengthMarker);
        assertNull(header.inlineValues);
    }

    // ========== Complex/Edge Case Tests ==========

    @Test
    void testParseComplexQuotedStringWithMultipleEscapes() {
        String input = "\"Line1\\nLine2\\tTab\\r\\nCRLF\\\\Backslash\\\"Quote\"";
        String expected = "Line1\nLine2\tTab\r\nCRLF\\Backslash\"Quote";
        assertEquals(expected, ToonParser.parseStringLiteral(input));
    }

    @Test
    void testParseArrayHeaderComplexQuotedKey() {
        ToonParser.ArrayHeader header = ToonParser.parseArrayHeader(
            "\"complex[key]with[brackets]\"[5]{a,b,c}:"
        );
        assertEquals("complex[key]with[brackets]", header.key);
        assertEquals(5, header.length);
        assertEquals(List.of("a", "b", "c"), header.fields);
    }

    @Test
    void testParseDelimitedValuesComplexQuotes() {
        List<String> result = ToonParser.parseDelimitedValues(
            "\"a,b\",\"c,d\",e", ","
        );
        assertEquals(List.of("\"a,b\"", "\"c,d\"", "e"), result);
    }

    @Test
    void testParseNumberBoundaries() {
        assertEquals(Integer.MAX_VALUE, ToonParser.parseNumber("2147483647"));
        assertEquals(Integer.MIN_VALUE, ToonParser.parseNumber("-2147483648"));
    }

    @Test
    void testParsePrimitiveWithWhitespace() {
        assertEquals(42, ToonParser.parsePrimitive("  42  "));
        assertEquals("hello", ToonParser.parsePrimitive("  \"hello\"  "));
        assertEquals(true, ToonParser.parsePrimitive("  true  "));
    }

    @Test
    void testParseArrayHeaderWithZeroLength() {
        ToonParser.ArrayHeader header = ToonParser.parseArrayHeader("[0]:");
        assertEquals(0, header.length);
    }

    @Test
    void testFindClosingQuoteAtEnd() {
        assertEquals(4, ToonParser.findClosingQuote("\"end\"", 0));
    }

    @Test
    void testUnescapeStringNoEscapes() {
        assertEquals("plain text", ToonParser.unescapeString("plain text"));
    }

    @Test
    void testParseDelimitedValuesNestedQuotes() {
        List<String> result = ToonParser.parseDelimitedValues(
            "\"outer\\\"inner\\\"\",value", ","
        );
        assertEquals(List.of("\"outer\\\"inner\\\"\"", "value"), result);
    }
}
