package im.arun.toon4j;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for Delimiter enum.
 * Tests enum values and getValue method.
 */
class DelimiterTest {

    @Test
    void testCommaDelimiter() {
        assertEquals(",", Delimiter.COMMA.getValue());
    }

    @Test
    void testTabDelimiter() {
        assertEquals("\t", Delimiter.TAB.getValue());
    }

    @Test
    void testPipeDelimiter() {
        assertEquals("|", Delimiter.PIPE.getValue());
    }

    @Test
    void testEnumValues() {
        Delimiter[] values = Delimiter.values();
        assertEquals(3, values.length);

        assertTrue(contains(values, Delimiter.COMMA));
        assertTrue(contains(values, Delimiter.TAB));
        assertTrue(contains(values, Delimiter.PIPE));
    }

    @Test
    void testValueOf() {
        assertEquals(Delimiter.COMMA, Delimiter.valueOf("COMMA"));
        assertEquals(Delimiter.TAB, Delimiter.valueOf("TAB"));
        assertEquals(Delimiter.PIPE, Delimiter.valueOf("PIPE"));
    }

    @Test
    void testValueOfInvalid() {
        assertThrows(IllegalArgumentException.class,
            () -> Delimiter.valueOf("INVALID"));
    }

    @Test
    void testEnumComparison() {
        assertSame(Delimiter.COMMA, Delimiter.valueOf("COMMA"));
        assertNotSame(Delimiter.COMMA, Delimiter.TAB);
    }

    @Test
    void testEnumName() {
        assertEquals("COMMA", Delimiter.COMMA.name());
        assertEquals("TAB", Delimiter.TAB.name());
        assertEquals("PIPE", Delimiter.PIPE.name());
    }

    @Test
    void testDelimiterValues() {
        assertNotEquals(Delimiter.COMMA.getValue(), Delimiter.TAB.getValue());
        assertNotEquals(Delimiter.COMMA.getValue(), Delimiter.PIPE.getValue());
        assertNotEquals(Delimiter.TAB.getValue(), Delimiter.PIPE.getValue());
    }

    @Test
    void testEnumOrdinal() {
        assertEquals(0, Delimiter.COMMA.ordinal());
        assertEquals(1, Delimiter.TAB.ordinal());
        assertEquals(2, Delimiter.PIPE.ordinal());
    }

    private boolean contains(Delimiter[] array, Delimiter value) {
        for (Delimiter d : array) {
            if (d == value) return true;
        }
        return false;
    }
}
