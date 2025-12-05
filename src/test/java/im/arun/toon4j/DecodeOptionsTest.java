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
        DecodeOptions options = DecodeOptions.lenient();
        assertFalse(options.isStrict(), "Lenient mode should not be strict");
    }

    @Test
    void testStrictFactory() {
        DecodeOptions options = DecodeOptions.strict();
        assertNotNull(options);
        assertEquals(2, options.getIndent());
        assertTrue(options.isStrict(), "Strict factory should create strict options");
    }

    @Test
    void testStrictWithCustomIndent() {
        DecodeOptions options = DecodeOptions.strict(4);
        assertEquals(4, options.getIndent());
        assertTrue(options.isStrict(), "Strict with custom indent should be strict");
    }

    @Test
    void testStrictIndentSizeOne() {
        DecodeOptions options = DecodeOptions.strict(1);
        assertEquals(1, options.getIndent());
        assertTrue(options.isStrict());
    }

    @Test
    void testStrictIndentZeroThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> DecodeOptions.strict(0));
    }

    @Test
    void testDefaultConstructor() {
        DecodeOptions options = new DecodeOptions();
        assertEquals(2, options.getIndent());
        assertTrue(options.isStrict(), "Default constructor should create strict options");
    }

    @Test
    void testWithPathExpansion() {
        DecodeOptions options = DecodeOptions.withPathExpansion(PathExpansion.SAFE);
        assertEquals(PathExpansion.SAFE, options.getExpandPaths());
        assertTrue(options.isStrict());
    }

    @Test
    void testWithReplacer() {
        DecodeReplacer replacer = (key, value, path) -> value;
        DecodeOptions options = DecodeOptions.withReplacer(replacer);
        assertNotNull(options.getReplacer());
    }

    @Test
    void testFullConstructor() {
        DecodeReplacer replacer = (key, value, path) -> value;
        DecodeOptions options = new DecodeOptions(4, false, PathExpansion.SAFE, replacer);

        assertEquals(4, options.getIndent());
        assertFalse(options.isStrict());
        assertEquals(PathExpansion.SAFE, options.getExpandPaths());
        assertNotNull(options.getReplacer());
    }

    @Test
    void testNullPathExpansionDefaultsToOff() {
        DecodeOptions options = new DecodeOptions(2, true, null);
        assertEquals(PathExpansion.OFF, options.getExpandPaths());
    }
}
