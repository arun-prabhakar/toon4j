package im.arun.toon4j;

/**
 * Configuration options for encoding data to TOON format.
 *
 * Provides both builder pattern and convenient factory methods for common configurations.
 */
public class EncodeOptions {
    /** Default encoding options: 2 spaces indent, comma delimiter, key folding off */
    public static final EncodeOptions DEFAULT = new EncodeOptions();

    private final int indent;
    private final Delimiter delimiter;
    private final KeyFolding keyFolding;
    private final boolean flatten;
    private final int flattenDepth;
    private final EncodeReplacer replacer;

    public EncodeOptions() {
        this(2, Delimiter.COMMA, KeyFolding.OFF, false, Integer.MAX_VALUE, null);
    }

    public EncodeOptions(int indent, Delimiter delimiter, KeyFolding keyFolding, boolean flatten, int flattenDepth, EncodeReplacer replacer) {
        this.indent = indent;
        this.delimiter = delimiter;
        this.keyFolding = keyFolding;
        this.flatten = flatten;
        this.flattenDepth = flattenDepth;
        this.replacer = replacer;
    }

    public int getIndent() {
        return indent;
    }

    public Delimiter getDelimiter() {
        return delimiter;
    }

    public String getDelimiterValue() {
        return delimiter.getValue();
    }

    public KeyFolding getKeyFolding() {
        return keyFolding;
    }

    public boolean isFlatten() {
        return flatten;
    }

    public int getFlattenDepth() {
        return flattenDepth;
    }

    public EncodeReplacer getReplacer() {
        return replacer;
    }

    // Factory methods for common configurations

    /**
     * Creates options with custom indent, using defaults for other settings.
     *
     * @param indent number of spaces per indentation level
     * @return new EncodeOptions with specified indent
     */
    public static EncodeOptions withIndent(int indent) {
        return new EncodeOptions(indent, Delimiter.COMMA, KeyFolding.OFF, false, Integer.MAX_VALUE, null);
    }

    /**
     * Creates options with custom delimiter, using defaults for other settings.
     *
     * @param delimiter the delimiter to use for arrays and tabular data
     * @return new EncodeOptions with specified delimiter
     */
    public static EncodeOptions withDelimiter(Delimiter delimiter) {
        return new EncodeOptions(2, delimiter, KeyFolding.OFF, false, Integer.MAX_VALUE, null);
    }

    /**
     * Creates options with tab delimiter for more compact output.
     *
     * @return new EncodeOptions with tab delimiter
     */
    public static EncodeOptions compact() {
        return new EncodeOptions(2, Delimiter.TAB, KeyFolding.OFF, false, Integer.MAX_VALUE, null);
    }

    /**
     * Creates options with tab delimiter and safe key folding.
     *
     * @return new EncodeOptions with tab delimiter and safe folding
     */
    public static EncodeOptions verbose() {
        return new EncodeOptions(2, Delimiter.TAB, KeyFolding.SAFE, false, Integer.MAX_VALUE, null);
    }

    /**
     * Creates a builder for custom configuration.
     *
     * @return new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for creating custom EncodeOptions configurations.
     */
    public static class Builder {
        private int indent = 2;
        private Delimiter delimiter = Delimiter.COMMA;
        private KeyFolding keyFolding = KeyFolding.OFF;
        private boolean flatten = false;
        private int flattenDepth = Integer.MAX_VALUE;
        private EncodeReplacer replacer;

        /**
         * Sets the number of spaces per indentation level.
         *
         * @param indent number of spaces (typically 2 or 4)
         * @return this builder
         */
        public Builder indent(int indent) {
            this.indent = indent;
            return this;
        }

        /**
         * Sets the delimiter for arrays and tabular data.
         *
         * @param delimiter the delimiter (COMMA, TAB, or PIPE)
         * @return this builder
         */
        public Builder delimiter(Delimiter delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        /**
         * Builds the EncodeOptions with current configuration.
         *
         * @return new EncodeOptions instance
         */
        public EncodeOptions build() {
            return new EncodeOptions(indent, delimiter, keyFolding, flatten, flattenDepth, replacer);
        }

        /**
         * Enables safe key folding.
         */
        public Builder keyFolding(KeyFolding keyFolding) {
            this.keyFolding = keyFolding;
            return this;
        }

        /**
         * Enables aggressive flattening of single-key chains (paired with depth budget).
         */
        public Builder flatten(boolean flatten) {
            this.flatten = flatten;
            return this;
        }

        /**
         * Limits how deep folding can traverse single-key chains.
         */
        public Builder flattenDepth(int flattenDepth) {
            this.flattenDepth = flattenDepth;
            return this;
        }

        /**
         * Sets a replacer to transform or filter values.
         */
        public Builder replacer(EncodeReplacer replacer) {
            this.replacer = replacer;
            return this;
        }
    }
}
