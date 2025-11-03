package im.arun.toon4j;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for LineCursor class.
 * Tests cursor navigation, boundary conditions, and state management.
 */
class LineCursorTest {

    private ParsedLine createLine(String content, int depth, int lineNumber) {
        int indent = depth * 2;
        String raw = " ".repeat(indent) + content;
        return new ParsedLine(raw, depth, indent, content, lineNumber);
    }

    @Test
    void testEmptyCursor() {
        LineCursor cursor = new LineCursor(new ArrayList<>());

        assertTrue(cursor.atEnd());
        assertEquals(0, cursor.size());
        assertEquals(0, cursor.getPosition());
        assertNull(cursor.peek());
    }

    @Test
    void testSingleLine() {
        List<ParsedLine> lines = List.of(
            createLine("name: Ada", 0, 1)
        );
        LineCursor cursor = new LineCursor(lines);

        assertFalse(cursor.atEnd());
        assertEquals(1, cursor.size());
        assertEquals(0, cursor.getPosition());

        ParsedLine line = cursor.peek();
        assertNotNull(line);
        assertEquals("name: Ada", line.content());
        assertEquals(0, cursor.getPosition()); // peek doesn't advance
    }

    @Test
    void testNext() {
        List<ParsedLine> lines = List.of(
            createLine("line1", 0, 1),
            createLine("line2", 0, 2)
        );
        LineCursor cursor = new LineCursor(lines);

        ParsedLine line1 = cursor.next();
        assertEquals("line1", line1.content());
        assertEquals(1, cursor.getPosition());

        ParsedLine line2 = cursor.next();
        assertEquals("line2", line2.content());
        assertEquals(2, cursor.getPosition());

        assertTrue(cursor.atEnd());
    }

    @Test
    void testNextAtEnd() {
        LineCursor cursor = new LineCursor(new ArrayList<>());

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            cursor::next
        );

        assertTrue(exception.getMessage().contains("No more lines available"));
    }

    @Test
    void testAdvance() {
        List<ParsedLine> lines = List.of(
            createLine("line1", 0, 1),
            createLine("line2", 0, 2)
        );
        LineCursor cursor = new LineCursor(lines);

        assertEquals(0, cursor.getPosition());
        cursor.advance();
        assertEquals(1, cursor.getPosition());
        cursor.advance();
        assertEquals(2, cursor.getPosition());
        assertTrue(cursor.atEnd());
    }

    @Test
    void testAdvanceBeyondEnd() {
        List<ParsedLine> lines = List.of(
            createLine("line1", 0, 1)
        );
        LineCursor cursor = new LineCursor(lines);

        cursor.advance();
        assertTrue(cursor.atEnd());

        // Advancing at end should be safe (no-op)
        cursor.advance();
        assertTrue(cursor.atEnd());
        assertEquals(1, cursor.getPosition());
    }

    @Test
    void testPeekAtEnd() {
        List<ParsedLine> lines = List.of(
            createLine("line1", 0, 1)
        );
        LineCursor cursor = new LineCursor(lines);

        cursor.advance();
        assertNull(cursor.peek());
    }

    @Test
    void testPeekAtWithinBounds() {
        List<ParsedLine> lines = List.of(
            createLine("line1", 0, 1),
            createLine("line2", 0, 2),
            createLine("line3", 0, 3)
        );
        LineCursor cursor = new LineCursor(lines);

        // Peek at current (offset 0)
        ParsedLine line0 = cursor.peekAt(0);
        assertNotNull(line0);
        assertEquals("line1", line0.content());

        // Peek ahead (offset 1)
        ParsedLine line1 = cursor.peekAt(1);
        assertNotNull(line1);
        assertEquals("line2", line1.content());

        // Peek further (offset 2)
        ParsedLine line2 = cursor.peekAt(2);
        assertNotNull(line2);
        assertEquals("line3", line2.content());

        // Position should not change
        assertEquals(0, cursor.getPosition());
    }

    @Test
    void testPeekAtOutOfBounds() {
        List<ParsedLine> lines = List.of(
            createLine("line1", 0, 1)
        );
        LineCursor cursor = new LineCursor(lines);

        // Peek beyond end
        assertNull(cursor.peekAt(1));
        assertNull(cursor.peekAt(10));
    }

    @Test
    void testPeekAtNegativeOffset() {
        List<ParsedLine> lines = List.of(
            createLine("line1", 0, 1),
            createLine("line2", 0, 2)
        );
        LineCursor cursor = new LineCursor(lines);

        cursor.advance(); // Move to position 1

        // Peek backwards (negative offset)
        assertNull(cursor.peekAt(-1));
        assertNull(cursor.peekAt(-10));
    }

    @Test
    void testPeekAtAfterAdvancing() {
        List<ParsedLine> lines = List.of(
            createLine("line1", 0, 1),
            createLine("line2", 0, 2),
            createLine("line3", 0, 3)
        );
        LineCursor cursor = new LineCursor(lines);

        cursor.advance(); // Position 1

        ParsedLine current = cursor.peekAt(0);
        assertEquals("line2", current.content());

        ParsedLine next = cursor.peekAt(1);
        assertEquals("line3", next.content());

        assertNull(cursor.peekAt(2)); // Beyond end
    }

    @Test
    void testSize() {
        List<ParsedLine> lines = List.of(
            createLine("line1", 0, 1),
            createLine("line2", 0, 2),
            createLine("line3", 0, 3)
        );
        LineCursor cursor = new LineCursor(lines);

        assertEquals(3, cursor.size());

        cursor.advance();
        assertEquals(3, cursor.size()); // Size doesn't change

        cursor.advance();
        cursor.advance();
        assertEquals(3, cursor.size());
    }

    @Test
    void testGetPosition() {
        List<ParsedLine> lines = List.of(
            createLine("line1", 0, 1),
            createLine("line2", 0, 2)
        );
        LineCursor cursor = new LineCursor(lines);

        assertEquals(0, cursor.getPosition());

        cursor.next();
        assertEquals(1, cursor.getPosition());

        cursor.next();
        assertEquals(2, cursor.getPosition());
    }

    @Test
    void testAtEnd() {
        List<ParsedLine> lines = List.of(
            createLine("line1", 0, 1)
        );
        LineCursor cursor = new LineCursor(lines);

        assertFalse(cursor.atEnd());

        cursor.advance();
        assertTrue(cursor.atEnd());
    }

    @Test
    void testPeekDoesNotAdvance() {
        List<ParsedLine> lines = List.of(
            createLine("line1", 0, 1),
            createLine("line2", 0, 2)
        );
        LineCursor cursor = new LineCursor(lines);

        ParsedLine line1 = cursor.peek();
        ParsedLine line2 = cursor.peek();

        assertSame(line1, line2);
        assertEquals(0, cursor.getPosition());
    }

    @Test
    void testNextAdvancesPosition() {
        List<ParsedLine> lines = List.of(
            createLine("line1", 0, 1),
            createLine("line2", 0, 2)
        );
        LineCursor cursor = new LineCursor(lines);

        assertEquals(0, cursor.getPosition());

        ParsedLine line = cursor.next();
        assertEquals("line1", line.content());
        assertEquals(1, cursor.getPosition());
    }

    @Test
    void testCompleteTraversal() {
        List<ParsedLine> lines = List.of(
            createLine("line1", 0, 1),
            createLine("line2", 1, 2),
            createLine("line3", 2, 3)
        );
        LineCursor cursor = new LineCursor(lines);

        int count = 0;
        while (!cursor.atEnd()) {
            ParsedLine line = cursor.next();
            assertNotNull(line);
            count++;
        }

        assertEquals(3, count);
        assertTrue(cursor.atEnd());
    }

    @Test
    void testMixedPeekAndNext() {
        List<ParsedLine> lines = List.of(
            createLine("line1", 0, 1),
            createLine("line2", 0, 2),
            createLine("line3", 0, 3)
        );
        LineCursor cursor = new LineCursor(lines);

        ParsedLine peeked1 = cursor.peek();
        assertEquals("line1", peeked1.content());

        ParsedLine next1 = cursor.next();
        assertEquals("line1", next1.content());

        ParsedLine peeked2 = cursor.peek();
        assertEquals("line2", peeked2.content());

        ParsedLine next2 = cursor.next();
        assertEquals("line2", next2.content());

        assertEquals(2, cursor.getPosition());
    }

    @Test
    void testLargeCursor() {
        List<ParsedLine> lines = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            lines.add(createLine("line" + i, 0, i + 1));
        }

        LineCursor cursor = new LineCursor(lines);
        assertEquals(1000, cursor.size());

        for (int i = 0; i < 1000; i++) {
            assertFalse(cursor.atEnd());
            ParsedLine line = cursor.next();
            assertEquals("line" + i, line.content());
        }

        assertTrue(cursor.atEnd());
    }

    @Test
    void testPeekAtWithLargeOffset() {
        List<ParsedLine> lines = List.of(
            createLine("line1", 0, 1),
            createLine("line2", 0, 2)
        );
        LineCursor cursor = new LineCursor(lines);

        assertNull(cursor.peekAt(1000));
    }

    @Test
    void testPeekAtZeroOffset() {
        List<ParsedLine> lines = List.of(
            createLine("line1", 0, 1)
        );
        LineCursor cursor = new LineCursor(lines);

        ParsedLine peek = cursor.peek();
        ParsedLine peekAt0 = cursor.peekAt(0);

        assertSame(peek, peekAt0);
    }

    @Test
    void testAdvanceMultipleTimes() {
        List<ParsedLine> lines = List.of(
            createLine("line1", 0, 1),
            createLine("line2", 0, 2),
            createLine("line3", 0, 3)
        );
        LineCursor cursor = new LineCursor(lines);

        for (int i = 0; i < 3; i++) {
            assertFalse(cursor.atEnd());
            cursor.advance();
        }

        assertTrue(cursor.atEnd());
        assertEquals(3, cursor.getPosition());
    }

    @Test
    void testWithDifferentDepths() {
        List<ParsedLine> lines = List.of(
            createLine("root", 0, 1),
            createLine("child", 1, 2),
            createLine("grandchild", 2, 3),
            createLine("sibling", 1, 4)
        );
        LineCursor cursor = new LineCursor(lines);

        assertEquals(0, cursor.next().depth());
        assertEquals(1, cursor.next().depth());
        assertEquals(2, cursor.next().depth());
        assertEquals(1, cursor.next().depth());
    }

    @Test
    void testStateAfterMultiplePeeks() {
        List<ParsedLine> lines = List.of(
            createLine("line1", 0, 1)
        );
        LineCursor cursor = new LineCursor(lines);

        for (int i = 0; i < 10; i++) {
            cursor.peek();
        }

        assertEquals(0, cursor.getPosition());
        assertFalse(cursor.atEnd());
    }

    @Test
    void testLineNumberPreservation() {
        List<ParsedLine> lines = List.of(
            createLine("line1", 0, 5),
            createLine("line2", 0, 10),
            createLine("line3", 0, 15)
        );
        LineCursor cursor = new LineCursor(lines);

        assertEquals(5, cursor.next().lineNumber());
        assertEquals(10, cursor.next().lineNumber());
        assertEquals(15, cursor.next().lineNumber());
    }

    @Test
    void testPeekAtBoundary() {
        List<ParsedLine> lines = List.of(
            createLine("line1", 0, 1),
            createLine("line2", 0, 2)
        );
        LineCursor cursor = new LineCursor(lines);

        // At position 0, offset 1 should be valid
        assertNotNull(cursor.peekAt(1));

        // At position 0, offset 2 should be out of bounds
        assertNull(cursor.peekAt(2));
    }
}
