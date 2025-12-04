package im.arun.toon4j;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EncodeOptionsTest {

    @Test
    void testDefaultOptions() {
        EncodeOptions options = EncodeOptions.DEFAULT;
        assertEquals(2, options.getIndent());
        assertEquals(Delimiter.COMMA, options.getDelimiter());
        assertEquals(",", options.getDelimiterValue());
        assertEquals(KeyFolding.OFF, options.getKeyFolding());
        assertFalse(options.isFlatten());
        assertEquals(Integer.MAX_VALUE, options.getFlattenDepth());
        assertNull(options.getReplacer());
    }

    @Test
    void testBuilder() {
        EncodeOptions options = EncodeOptions.builder()
            .indent(4)
            .delimiter(Delimiter.TAB)
            .keyFolding(KeyFolding.SAFE)
            .flatten(true)
            .flattenDepth(3)
            .build();

        assertEquals(4, options.getIndent());
        assertEquals(Delimiter.TAB, options.getDelimiter());
        assertEquals("\t", options.getDelimiterValue());
        assertEquals(KeyFolding.SAFE, options.getKeyFolding());
        assertTrue(options.isFlatten());
        assertEquals(3, options.getFlattenDepth());
    }

    @Test
    void testBuilderWithDefaults() {
        EncodeOptions options = EncodeOptions.builder().build();
        assertEquals(2, options.getIndent());
        assertEquals(Delimiter.COMMA, options.getDelimiter());
        assertEquals(KeyFolding.OFF, options.getKeyFolding());
    }

    @Test
    void testFactoryWithIndent() {
        EncodeOptions options = EncodeOptions.withIndent(8);
        assertEquals(8, options.getIndent());
        assertEquals(Delimiter.COMMA, options.getDelimiter()); // Should be default
    }

    @Test
    void testFactoryWithDelimiter() {
        EncodeOptions options = EncodeOptions.withDelimiter(Delimiter.PIPE);
        assertEquals(2, options.getIndent()); // Should be default
        assertEquals(Delimiter.PIPE, options.getDelimiter());
        assertEquals("|", options.getDelimiterValue());
    }

    @Test
    void testPresetCompact() {
        EncodeOptions options = EncodeOptions.compact();
        assertEquals(2, options.getIndent());
        assertEquals(Delimiter.TAB, options.getDelimiter());
        assertEquals(KeyFolding.OFF, options.getKeyFolding());
    }

    @Test
    void testPresetVerbose() {
        EncodeOptions options = EncodeOptions.verbose();
        assertEquals(2, options.getIndent());
        assertEquals(Delimiter.TAB, options.getDelimiter());
        assertEquals(KeyFolding.SAFE, options.getKeyFolding());
    }

    @Test
    void testImmutability() {
        EncodeOptions options1 = EncodeOptions.builder().indent(4).build();
        // No setters, so no way to change it after creation.
        // We can verify that builders create new instances.
        EncodeOptions options2 = EncodeOptions.builder().indent(4).build();
        assertNotSame(options1, options2);
        assertEquals(options1.getIndent(), options2.getIndent());
    }
}
