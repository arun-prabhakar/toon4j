package im.arun.toon4j;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for LineWriter class.
 * Tests line writing, indentation, caching, and string building.
 */
class LineWriterTest {

    @Test
    void testEmptyWriter() {
        LineWriter writer = new LineWriter(2);
        assertEquals("", writer.toString());
        assertTrue(writer.getLines().isEmpty());
    }

    @Test
    void testPushSingleLineNoIndent() {
        LineWriter writer = new LineWriter(2);
        writer.push(0, "name: Ada");

        assertEquals("name: Ada", writer.toString());
        List<String> lines = writer.getLines();
        assertEquals(1, lines.size());
        assertEquals("name: Ada", lines.get(0));
    }

    @Test
    void testPushSingleLineWithIndent() {
        LineWriter writer = new LineWriter(2);
        writer.push(1, "name: Ada");

        assertEquals("  name: Ada", writer.toString());
    }

    @Test
    void testPushMultipleLines() {
        LineWriter writer = new LineWriter(2);
        writer.push(0, "user:");
        writer.push(1, "name: Ada");
        writer.push(1, "age: 25");

        String expected = "user:\n  name: Ada\n  age: 25";
        assertEquals(expected, writer.toString());
    }

    @Test
    void testPushWithVariableDepths() {
        LineWriter writer = new LineWriter(2);
        writer.push(0, "level0");
        writer.push(1, "level1");
        writer.push(2, "level2");
        writer.push(3, "level3");

        String expected = "level0\n  level1\n    level2\n      level3";
        assertEquals(expected, writer.toString());
    }

    @Test
    void testPushRaw() {
        LineWriter writer = new LineWriter(2);
        writer.pushRaw("raw line without indent");

        assertEquals("raw line without indent", writer.toString());
    }

    @Test
    void testPushRawAndNormalMixed() {
        LineWriter writer = new LineWriter(2);
        writer.push(0, "user:");
        writer.pushRaw("  custom indent");
        writer.push(1, "name: Ada");

        String expected = "user:\n  custom indent\n  name: Ada";
        assertEquals(expected, writer.toString());
    }

    @Test
    void testCustomIndentSize() {
        LineWriter writer = new LineWriter(4);
        writer.push(0, "root");
        writer.push(1, "child");
        writer.push(2, "grandchild");

        String expected = "root\n    child\n        grandchild";
        assertEquals(expected, writer.toString());
    }

    @Test
    void testIndentSizeOne() {
        LineWriter writer = new LineWriter(1);
        writer.push(0, "a");
        writer.push(1, "b");
        writer.push(2, "c");

        String expected = "a\n b\n  c";
        assertEquals(expected, writer.toString());
    }

    @Test
    void testDeepNestingWithCache() {
        // Test depths 0-10 which should use cached indents
        LineWriter writer = new LineWriter(2);
        for (int i = 0; i <= 10; i++) {
            writer.push(i, "level" + i);
        }

        List<String> lines = writer.getLines();
        assertEquals(11, lines.size());

        // Verify indentation increases correctly
        assertEquals("level0", lines.get(0));
        assertEquals("  level1", lines.get(1));
        assertEquals("    level2", lines.get(2));
        assertEquals("                    level10", lines.get(10)); // 20 spaces
    }

    @Test
    void testDeepNestingBeyondCache() {
        // Test depth > 10 which should compute indent dynamically
        LineWriter writer = new LineWriter(2);
        writer.push(15, "deep");

        String expected = " ".repeat(30) + "deep"; // 15 * 2 = 30 spaces
        assertEquals(expected, writer.toString());
    }

    @Test
    void testVeryDeepNesting() {
        LineWriter writer = new LineWriter(2);
        writer.push(50, "very deep");

        String expected = " ".repeat(100) + "very deep";
        assertEquals(expected, writer.toString());
    }

    @Test
    void testGetLinesReturnsNewList() {
        LineWriter writer = new LineWriter(2);
        writer.push(0, "line1");

        List<String> lines1 = writer.getLines();
        List<String> lines2 = writer.getLines();

        assertNotSame(lines1, lines2, "getLines should return a new list each time");
        assertEquals(lines1, lines2, "Contents should be equal");
    }

    @Test
    void testGetLinesIsImmutable() {
        LineWriter writer = new LineWriter(2);
        writer.push(0, "line1");

        List<String> lines = writer.getLines();
        lines.add("should not affect writer");

        // Original writer should be unchanged
        assertEquals(1, writer.getLines().size());
    }

    @Test
    void testMultipleToStringCalls() {
        LineWriter writer = new LineWriter(2);
        writer.push(0, "name: Ada");

        String result1 = writer.toString();
        String result2 = writer.toString();

        assertEquals(result1, result2);
        assertEquals("name: Ada", result1);
    }

    @Test
    void testEmptyContent() {
        LineWriter writer = new LineWriter(2);
        writer.push(0, "");
        writer.push(1, "");

        assertEquals("\n  ", writer.toString());
    }

    @Test
    void testWhitespaceContent() {
        LineWriter writer = new LineWriter(2);
        writer.push(0, "   ");
        writer.push(1, "\t");

        assertEquals("   \n  \t", writer.toString());
    }

    @Test
    void testSpecialCharacters() {
        LineWriter writer = new LineWriter(2);
        writer.push(0, "unicode: \u2764\uFE0F");
        writer.push(0, "quotes: \"hello\"");
        writer.push(0, "newline: \\n");

        List<String> lines = writer.getLines();
        assertEquals(3, lines.size());
        assertTrue(lines.get(0).contains("\u2764\uFE0F"));
        assertTrue(lines.get(1).contains("\"hello\""));
    }

    @Test
    void testLongContent() {
        LineWriter writer = new LineWriter(2);
        String longLine = "x".repeat(1000);
        writer.push(0, longLine);

        assertEquals(longLine, writer.toString());
    }

    @Test
    void testManyLines() {
        LineWriter writer = new LineWriter(2);
        for (int i = 0; i < 1000; i++) {
            writer.push(0, "line" + i);
        }

        List<String> lines = writer.getLines();
        assertEquals(1000, lines.size());
        assertEquals("line0", lines.get(0));
        assertEquals("line999", lines.get(999));
    }

    @Test
    void testIndentWithZeroSpacesPerLevel() {
        LineWriter writer = new LineWriter(0);
        writer.push(0, "a");
        writer.push(5, "b"); // depth 5 but 0 spaces per level = no indent

        assertEquals("a\nb", writer.toString());
    }

    @Test
    void testToStringWithSingleLine() {
        LineWriter writer = new LineWriter(2);
        writer.push(0, "single");

        assertEquals("single", writer.toString());
    }

    @Test
    void testToStringWithTwoLines() {
        LineWriter writer = new LineWriter(2);
        writer.push(0, "first");
        writer.push(0, "second");

        assertEquals("first\nsecond", writer.toString());
    }

    @Test
    void testComplexTabularFormat() {
        LineWriter writer = new LineWriter(2);
        writer.push(0, "items[2]{sku,qty,price}:");
        writer.push(1, "A1,2,9.99");
        writer.push(1, "B2,1,14.5");

        String expected = "items[2]{sku,qty,price}:\n  A1,2,9.99\n  B2,1,14.5";
        assertEquals(expected, writer.toString());
    }

    @Test
    void testMixedDepthPattern() {
        LineWriter writer = new LineWriter(2);
        writer.push(0, "root");
        writer.push(2, "skip level"); // Jump from 0 to 2
        writer.push(1, "back to 1");
        writer.push(0, "back to 0");

        List<String> lines = writer.getLines();
        assertEquals("root", lines.get(0));
        assertEquals("    skip level", lines.get(1));
        assertEquals("  back to 1", lines.get(2));
        assertEquals("back to 0", lines.get(3));
    }

    @Test
    void testPushRawWithEmptyString() {
        LineWriter writer = new LineWriter(2);
        writer.pushRaw("");

        assertEquals("", writer.toString());
        assertEquals(1, writer.getLines().size());
    }

    @Test
    void testMultiplePushRaw() {
        LineWriter writer = new LineWriter(2);
        writer.pushRaw("line1");
        writer.pushRaw("line2");
        writer.pushRaw("line3");

        assertEquals("line1\nline2\nline3", writer.toString());
    }

    @Test
    void testPushWithLargeIndentSize() {
        LineWriter writer = new LineWriter(8);
        writer.push(1, "content");

        assertEquals("        content", writer.toString()); // 8 spaces
    }

    @Test
    void testLineCountConsistency() {
        LineWriter writer = new LineWriter(2);
        writer.push(0, "a");
        writer.push(1, "b");
        writer.push(2, "c");

        assertEquals(3, writer.getLines().size());

        String str = writer.toString();
        int newlineCount = 0;
        for (char c : str.toCharArray()) {
            if (c == '\n') newlineCount++;
        }
        assertEquals(2, newlineCount); // 3 lines = 2 newlines
    }
}
