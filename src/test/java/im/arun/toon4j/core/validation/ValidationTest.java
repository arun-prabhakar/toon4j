package im.arun.toon4j.core.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationTest {

    @Test
    void acceptsValidIdentifierSegments() {
        assertTrue(Validation.isIdentifierSegment("foo"));
        assertTrue(Validation.isIdentifierSegment("Bar_123"));
        assertTrue(Validation.isIdentifierSegment("_under"));
    }

    @Test
    void rejectsInvalidIdentifierSegments() {
        assertFalse(Validation.isIdentifierSegment(null));
        assertFalse(Validation.isIdentifierSegment(""));
        assertFalse(Validation.isIdentifierSegment("123start"));
        assertFalse(Validation.isIdentifierSegment("has-dash"));
        assertFalse(Validation.isIdentifierSegment("white space"));
    }
}
