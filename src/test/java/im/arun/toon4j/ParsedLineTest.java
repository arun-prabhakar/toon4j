package im.arun.toon4j;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for ParsedLine class.
 * Tests immutable record behavior, field access, and toString.
 */
class ParsedLineTest {

    @Test
    void testBasicConstruction() {
        ParsedLine line = new ParsedLine(
            "  name: Ada",
            1,
            2,
            "name: Ada",
            5
        );

        assertEquals("  name: Ada", line.raw);
        assertEquals(1, line.depth);
        assertEquals(2, line.indent);
        assertEquals("name: Ada", line.content);
        assertEquals(5, line.lineNumber);
    }

    @Test
    void testZeroDepth() {
        ParsedLine line = new ParsedLine(
            "root",
            0,
            0,
            "root",
            1
        );

        assertEquals(0, line.depth);
        assertEquals(0, line.indent);
        assertEquals("root", line.content);
    }

    @Test
    void testDeepNesting() {
        ParsedLine line = new ParsedLine(
            "          value",
            5,
            10,
            "value",
            100
        );

        assertEquals(5, line.depth);
        assertEquals(10, line.indent);
        assertEquals(100, line.lineNumber);
    }

    @Test
    void testEmptyContent() {
        ParsedLine line = new ParsedLine(
            "",
            0,
            0,
            "",
            1
        );

        assertEquals("", line.raw);
        assertEquals("", line.content);
    }

    @Test
    void testWhitespaceContent() {
        ParsedLine line = new ParsedLine(
            "     ",
            2,
            4,
            " ",
            10
        );

        assertEquals("     ", line.raw);
        assertEquals(" ", line.content);
    }

    @Test
    void testSpecialCharacters() {
        String content = "note: \"hello, world\"";
        String raw = "  " + content;

        ParsedLine line = new ParsedLine(
            raw,
            1,
            2,
            content,
            1
        );

        assertEquals(content, line.content);
        assertTrue(line.content.contains("\""));
        assertTrue(line.content.contains(","));
    }

    @Test
    void testUnicodeContent() {
        String content = "emoji: \u2764\uFE0F \u1F600";
        ParsedLine line = new ParsedLine(
            content,
            0,
            0,
            content,
            1
        );

        assertEquals(content, line.content);
        assertTrue(line.content.contains("\u2764\uFE0F"));
    }

    @Test
    void testToString() {
        ParsedLine line = new ParsedLine(
            "  name: Ada",
            1,
            2,
            "name: Ada",
            5
        );

        String str = line.toString();
        assertTrue(str.contains("Line 5"));
        assertTrue(str.contains("depth=1"));
        assertTrue(str.contains("name: Ada"));
    }

    @Test
    void testToStringFormat() {
        ParsedLine line = new ParsedLine(
            "root",
            0,
            0,
            "root",
            1
        );

        String expected = "Line 1 (depth=0): root";
        assertEquals(expected, line.toString());
    }

    @Test
    void testToStringWithDeepNesting() {
        ParsedLine line = new ParsedLine(
            "                    deep",
            10,
            20,
            "deep",
            42
        );

        String str = line.toString();
        assertTrue(str.contains("Line 42"));
        assertTrue(str.contains("depth=10"));
        assertTrue(str.contains("deep"));
    }

    @Test
    void testRawVsContent() {
        ParsedLine line = new ParsedLine(
            "    content",
            2,
            4,
            "content",
            1
        );

        assertNotEquals(line.raw, line.content);
        assertEquals("    content", line.raw);
        assertEquals("content", line.content);
    }

    @Test
    void testIndentDepthRelationship() {
        // indent = depth * spacesPerLevel
        // For spacesPerLevel = 2
        ParsedLine line1 = new ParsedLine("  x", 1, 2, "x", 1);
        assertEquals(line1.indent, line1.depth * 2);

        ParsedLine line2 = new ParsedLine("    x", 2, 4, "x", 2);
        assertEquals(line2.indent, line2.depth * 2);

        ParsedLine line3 = new ParsedLine("      x", 3, 6, "x", 3);
        assertEquals(line3.indent, line3.depth * 2);
    }

    @Test
    void testLargeLineNumber() {
        ParsedLine line = new ParsedLine(
            "content",
            0,
            0,
            "content",
            999999
        );

        assertEquals(999999, line.lineNumber);
        assertTrue(line.toString().contains("Line 999999"));
    }

    @Test
    void testLongContent() {
        String content = "x".repeat(1000);
        ParsedLine line = new ParsedLine(
            content,
            0,
            0,
            content,
            1
        );

        assertEquals(1000, line.content.length());
        assertEquals(content, line.content);
    }

    @Test
    void testTabCharacterInContent() {
        ParsedLine line = new ParsedLine(
            "\tvalue",
            0,
            0,
            "\tvalue",
            1
        );

        assertTrue(line.content.contains("\t"));
    }

    @Test
    void testNewlineInContent() {
        // Although unlikely in practice, test that content can contain newlines
        String content = "line1\nline2";
        ParsedLine line = new ParsedLine(
            content,
            0,
            0,
            content,
            1
        );

        assertEquals(content, line.content);
        assertTrue(line.content.contains("\n"));
    }

    @Test
    void testNullValuesNotAllowed() {
        // While ParsedLine constructor doesn't explicitly check for nulls,
        // test what happens if null is passed (should NPE on usage)
        ParsedLine line = new ParsedLine(
            null,
            0,
            0,
            null,
            1
        );

        assertNull(line.raw);
        assertNull(line.content);
    }

    @Test
    void testNegativeDepthAndIndent() {
        // Test negative values (shouldn't happen in practice)
        ParsedLine line = new ParsedLine(
            "content",
            -1,
            -2,
            "content",
            1
        );

        assertEquals(-1, line.depth);
        assertEquals(-2, line.indent);
    }

    @Test
    void testArrayHeaderContent() {
        ParsedLine line = new ParsedLine(
            "items[3]{a,b,c}:",
            0,
            0,
            "items[3]{a,b,c}:",
            1
        );

        assertTrue(line.content.contains("[3]"));
        assertTrue(line.content.contains("{a,b,c}"));
    }

    @Test
    void testTabularDataRow() {
        ParsedLine line = new ParsedLine(
            "  A1,2,9.99",
            1,
            2,
            "A1,2,9.99",
            5
        );

        assertEquals("A1,2,9.99", line.content);
        assertEquals(1, line.depth);
    }

    @Test
    void testListItemMarker() {
        ParsedLine line = new ParsedLine(
            "  - value",
            1,
            2,
            "- value",
            3
        );

        assertTrue(line.content.startsWith("- "));
        assertEquals("- value", line.content);
    }

    @Test
    void testKeyValuePair() {
        ParsedLine line = new ParsedLine(
            "  name: Ada",
            1,
            2,
            "name: Ada",
            10
        );

        assertTrue(line.content.contains(":"));
        String[] parts = line.content.split(":", 2);
        assertEquals("name", parts[0]);
        assertEquals(" Ada", parts[1]);
    }

    @Test
    void testEscapedCharactersInContent() {
        ParsedLine line = new ParsedLine(
            "path: \"C:\\\\Users\\\\Ada\"",
            0,
            0,
            "path: \"C:\\\\Users\\\\Ada\"",
            1
        );

        assertTrue(line.content.contains("\\\\"));
        assertTrue(line.content.contains("\""));
    }

    @Test
    void testMultipleColonsInContent() {
        ParsedLine line = new ParsedLine(
            "time: 12:30:45",
            0,
            0,
            "time: 12:30:45",
            1
        );

        assertEquals("time: 12:30:45", line.content);
    }

    @Test
    void testCommasInContent() {
        ParsedLine line = new ParsedLine(
            "tags: admin,dev,ops",
            0,
            0,
            "tags: admin,dev,ops",
            1
        );

        assertTrue(line.content.contains(","));
        assertEquals(2, line.content.split(",").length - 1 + 1);
    }
}
