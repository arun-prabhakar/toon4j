package im.arun.toon4j;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for DecodeOptions class.
 * Tests factory methods, getters, and configuration options.
 */
class DecodeOptionsTest {

    @Test
    void testDefaultDecodeOptions() {
        DecodeOptions options = DecodeOptions.lenient();

        assertEquals(2, options.getIndent());
        assertFalse(options.isStrict());
    }

    @Test
    void testLenientFactory() {
        DecodeOptions options = DecodeOptions.lenient();

        assertNotNull(options);
        assertEquals(2, options.getIndent());
        assertFalse(options.isStrict());
    }

    @Test
    void testLenientWithCustomIndent() {
        DecodeOptions options = DecodeOptions.lenient(4);

        assertEquals(4, options.getIndent());
        assertFalse(options.isStrict());
    }

    @Test
    void testLenientIndentSizeOne() {
        DecodeOptions options = DecodeOptions.lenient(1);
        assertEquals(1, options.getIndent());
    }

    @Test
    void testLenientIndentSizeEight() {
        DecodeOptions options = DecodeOptions.lenient(8);
        assertEquals(8, options.getIndent());
    }

    @Test
    void testGetIndent() {
        DecodeOptions options = DecodeOptions.lenient(3);
        assertEquals(3, options.getIndent());
    }

    @Test
    void testIsStrict() {
        DecodeOptions lenient = DecodeOptions.lenient();
        assertFalse(lenient.isStrict());
    }

    @Test
    void testMultipleInstances() {
        DecodeOptions options1 = DecodeOptions.lenient();
        DecodeOptions options2 = DecodeOptions.lenient();

        // Should be different instances
        assertNotSame(options1, options2);

        // But with same values
        assertEquals(options1.getIndent(), options2.getIndent());
        assertEquals(options1.isStrict(), options2.isStrict());
    }

    @Test
    void testLenientIndentMinimum() {
        DecodeOptions options = DecodeOptions.lenient(1);
        assertEquals(1, options.getIndent());
    }

    @Test
    void testLenientIndentZeroThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> DecodeOptions.lenient(0));
    }

    @Test
    void testIndentValues() {
        for (int i = 1; i <= 10; i++) {
            DecodeOptions options = DecodeOptions.lenient(i);
            assertEquals(i, options.getIndent());
        }
    }

    @Test
    void testLargeIndentValue() {
        DecodeOptions options = DecodeOptions.lenient(100);
        assertEquals(100, options.getIndent());
    }

    @Test
    void testOptionsImmutability() {
        DecodeOptions options = DecodeOptions.lenient(4);

        int indent1 = options.getIndent();
        int indent2 = options.getIndent();

        assertEquals(indent1, indent2, "Multiple calls should return same value");
    }

    @Test
    void testStrictMode() {
        // Note: Current DecodeOptions only has lenient factory
        // This test documents expected behavior if strict mode is added
        DecodeOptions options = DecodeOptions.lenient();
        assertFalse(options.isStrict(), "Default should be lenient (not strict)");
    }
}
