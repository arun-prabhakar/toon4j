package im.arun.toon4j;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for Scanner class.
 * Tests line scanning, indentation calculation, strict/lenient modes,
 * and edge cases like tabs, blank lines, and line endings.
 */
class ScannerTest {

    @Test
    void testScanNullInput() {
        List<ParsedLine> result = Scanner.scan(null, 2, false);
        assertTrue(result.isEmpty(), "Null input should return empty list");
    }

    @Test
    void testScanEmptyInput() {
        List<ParsedLine> result = Scanner.scan("", 2, false);
        assertTrue(result.isEmpty(), "Empty input should return empty list");
    }

    @Test
    void testScanSingleLine() {
        String input = "name: Ada";
        List<ParsedLine> result = Scanner.scan(input, 2, false);

        assertEquals(1, result.size());
        ParsedLine line = result.get(0);
        assertEquals(0, line.depth);
        assertEquals(0, line.indent);
        assertEquals("name: Ada", line.content);
        assertEquals(1, line.lineNumber);
    }

    @Test
    void testScanMultipleLines() {
        String input = "name: Ada\nage: 25\ncity: London";
        List<ParsedLine> result = Scanner.scan(input, 2, false);

        assertEquals(3, result.size());
        assertEquals("name: Ada", result.get(0).content);
        assertEquals("age: 25", result.get(1).content);
        assertEquals("city: London", result.get(2).content);
    }

    @Test
    void testScanWithIndentation() {
        String input = "user:\n  name: Ada\n  age: 25";
        List<ParsedLine> result = Scanner.scan(input, 2, false);

        assertEquals(3, result.size());

        ParsedLine line0 = result.get(0);
        assertEquals(0, line0.depth);
        assertEquals("user:", line0.content);

        ParsedLine line1 = result.get(1);
        assertEquals(1, line1.depth);
        assertEquals(2, line1.indent);
        assertEquals("name: Ada", line1.content);

        ParsedLine line2 = result.get(2);
        assertEquals(1, line2.depth);
        assertEquals(2, line2.indent);
        assertEquals("age: 25", line2.content);
    }

    @Test
    void testScanWithDeepNesting() {
        String input = "level0:\n  level1:\n    level2:\n      level3: value";
        List<ParsedLine> result = Scanner.scan(input, 2, false);

        assertEquals(4, result.size());
        assertEquals(0, result.get(0).depth);
        assertEquals(1, result.get(1).depth);
        assertEquals(2, result.get(2).depth);
        assertEquals(3, result.get(3).depth);
    }

    @Test
    void testScanSkipsBlankLines() {
        String input = "line1\n\nline2\n   \nline3";
        List<ParsedLine> result = Scanner.scan(input, 2, false);

        assertEquals(3, result.size());
        assertEquals("line1", result.get(0).content);
        assertEquals("line2", result.get(1).content);
        assertEquals("line3", result.get(2).content);
    }

    @Test
    void testScanLineNumberTracking() {
        String input = "line1\n\nline3\nline4";
        List<ParsedLine> result = Scanner.scan(input, 2, false);

        assertEquals(3, result.size());
        assertEquals(1, result.get(0).lineNumber);
        assertEquals(3, result.get(1).lineNumber); // Line 2 was blank
        assertEquals(4, result.get(2).lineNumber);
    }

    @Test
    void testScanStrictModeValidIndentation() {
        String input = "root:\n  child1:\n    child2: value";
        List<ParsedLine> result = Scanner.scan(input, 2, true);

        assertEquals(3, result.size());
        assertEquals(0, result.get(0).depth);
        assertEquals(1, result.get(1).depth);
        assertEquals(2, result.get(2).depth);
    }

    @Test
    void testScanStrictModeInvalidIndentation() {
        String input = "root:\n   child: value"; // 3 spaces (not multiple of 2)

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Scanner.scan(input, 2, true)
        );

        assertTrue(exception.getMessage().contains("Line 2"));
        assertTrue(exception.getMessage().contains("Indentation (3 spaces)"));
        assertTrue(exception.getMessage().contains("multiple of 2"));
    }

    @Test
    void testScanLenientModeAllowsIrregularIndentation() {
        String input = "root:\n   child: value"; // 3 spaces
        List<ParsedLine> result = Scanner.scan(input, 2, false);

        assertEquals(2, result.size());
        assertEquals(1, result.get(1).depth); // 3 / 2 = 1
    }

    @Test
    void testScanStrictModeRejectsTabs() {
        String input = "root:\n\tchild: value"; // Tab character

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Scanner.scan(input, 2, true)
        );

        assertTrue(exception.getMessage().contains("Line 2"));
        assertTrue(exception.getMessage().contains("Tabs not allowed"));
        assertTrue(exception.getMessage().contains("strict mode"));
    }

    @Test
    void testScanLenientModeAllowsTabs() {
        String input = "root:\n    child: value"; // 4 spaces instead of tab
        List<ParsedLine> result = Scanner.scan(input, 2, false);

        assertEquals(2, result.size());
        ParsedLine child = result.get(1);
        assertEquals(2, child.depth); // 4 spaces / 2 = depth 2
        assertEquals(4, child.indent);
        assertEquals("child: value", child.content);
    }

    @Test
    void testScanMixedIndentation() {
        String input = "root:\n      child: value"; // 6 spaces
        List<ParsedLine> result = Scanner.scan(input, 2, false);

        assertEquals(2, result.size());
        ParsedLine child = result.get(1);
        assertEquals(3, child.depth); // 6 / 2 = 3
        assertEquals(6, child.indent);
    }

    @Test
    void testScanWithCRLFLineEndings() {
        String input = "line1\r\nline2\r\nline3";
        List<ParsedLine> result = Scanner.scan(input, 2, false);

        assertEquals(3, result.size());
        assertEquals("line1", result.get(0).content);
        assertEquals("line2", result.get(1).content);
        assertEquals("line3", result.get(2).content);
    }

    @Test
    void testScanWithCustomIndentSize() {
        String input = "root:\n    child1:\n        child2: value";
        List<ParsedLine> result = Scanner.scan(input, 4, false);

        assertEquals(3, result.size());
        assertEquals(0, result.get(0).depth);
        assertEquals(1, result.get(1).depth);
        assertEquals(2, result.get(2).depth);
    }

    @Test
    void testScanPreservesRawLine() {
        String input = "  name: Ada";
        List<ParsedLine> result = Scanner.scan(input, 2, false);

        assertEquals(1, result.size());
        ParsedLine line = result.get(0);
        assertEquals("  name: Ada", line.raw); // Raw preserves indentation
        assertEquals("name: Ada", line.content); // Content strips it
    }

    @Test
    void testScanWithZeroIndent() {
        String input = "name: Ada\nage: 25";
        List<ParsedLine> result = Scanner.scan(input, 2, false);

        for (ParsedLine line : result) {
            assertEquals(0, line.depth);
            assertEquals(0, line.indent);
        }
    }

    @Test
    void testScanWithLargeIndentation() {
        // Test 10 levels deep
        StringBuilder input = new StringBuilder("level0:");
        for (int i = 1; i <= 10; i++) {
            input.append("\n");
            for (int j = 0; j < i * 2; j++) {
                input.append(" ");
            }
            input.append("level").append(i).append(":");
        }

        List<ParsedLine> result = Scanner.scan(input.toString(), 2, false);
        assertEquals(11, result.size());

        for (int i = 0; i <= 10; i++) {
            assertEquals(i, result.get(i).depth);
        }
    }

    @Test
    void testScanStrictModeWithValidTabularFormat() {
        String input = "items[2]{sku,qty}:\n  A1,2\n  B2,1";
        List<ParsedLine> result = Scanner.scan(input, 2, true);

        assertEquals(3, result.size());
        assertEquals(0, result.get(0).depth);
        assertEquals(1, result.get(1).depth);
        assertEquals(1, result.get(2).depth);
    }

    @Test
    void testScanWithTrailingWhitespace() {
        String input = "name: Ada   \nage: 25  ";
        List<ParsedLine> result = Scanner.scan(input, 2, false);

        assertEquals(2, result.size());
        // Content includes trailing whitespace (Scanner doesn't trim)
        assertEquals("name: Ada   ", result.get(0).content);
        assertEquals("age: 25  ", result.get(1).content);
    }

    @Test
    void testScanSingleBlankLine() {
        String input = "\n";
        List<ParsedLine> result = Scanner.scan(input, 2, false);
        assertTrue(result.isEmpty());
    }

    @Test
    void testScanMultipleConsecutiveBlankLines() {
        String input = "line1\n\n\n\nline2";
        List<ParsedLine> result = Scanner.scan(input, 2, false);

        assertEquals(2, result.size());
        assertEquals("line1", result.get(0).content);
        assertEquals("line2", result.get(1).content);
    }

    @Test
    void testScanOnlyWhitespace() {
        String input = "   \n\t\n  \t  ";
        List<ParsedLine> result = Scanner.scan(input, 2, false);
        assertTrue(result.isEmpty(), "Lines with only whitespace should be skipped");
    }

    @Test
    void testScanIndentSizeOne() {
        String input = "root:\n child:\n  grandchild: value";
        List<ParsedLine> result = Scanner.scan(input, 1, false);

        assertEquals(3, result.size());
        assertEquals(0, result.get(0).depth);
        assertEquals(1, result.get(1).depth);
        assertEquals(2, result.get(2).depth);
    }

    @Test
    void testScanComplexRealWorldExample() {
        String input = """
            user:
              id: 123
              name: Ada Lovelace
              tags[3]: admin,dev,ops
              address:
                city: London
                zip: SW1A 2AA
            """.trim();

        List<ParsedLine> result = Scanner.scan(input, 2, false);

        assertEquals(7, result.size());
        assertEquals("user:", result.get(0).content);
        assertEquals(0, result.get(0).depth);
        assertEquals("id: 123", result.get(1).content);
        assertEquals(1, result.get(1).depth);
    }

    @Test
    void testScanEmptyLinesAtStart() {
        String input = "\n\nname: Ada";
        List<ParsedLine> result = Scanner.scan(input, 2, false);

        assertEquals(1, result.size());
        assertEquals(3, result.get(0).lineNumber); // First non-blank is line 3
    }

    @Test
    void testScanEmptyLinesAtEnd() {
        String input = "name: Ada\n\n\n";
        List<ParsedLine> result = Scanner.scan(input, 2, false);

        assertEquals(1, result.size());
        assertEquals("name: Ada", result.get(0).content);
    }

    @Test
    void testScanWithSpecialCharactersInContent() {
        String input = "note: \"hello: world, [test]\"";
        List<ParsedLine> result = Scanner.scan(input, 2, false);

        assertEquals(1, result.size());
        assertEquals("note: \"hello: world, [test]\"", result.get(0).content);
    }

    @Test
    void testScanDeepIndentation() {
        String input = "root:\n            child: value"; // 12 spaces
        List<ParsedLine> result = Scanner.scan(input, 2, false);

        assertEquals(2, result.size());
        ParsedLine child = result.get(1);
        assertEquals(6, child.depth); // 12 / 2 = 6
        assertEquals(12, child.indent);
    }
}
