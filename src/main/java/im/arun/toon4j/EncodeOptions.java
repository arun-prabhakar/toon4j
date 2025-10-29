package im.arun.toon4j;

/**
 * Configuration options for encoding data to TOON format.
 *
 * Provides both builder pattern and convenient factory methods for common configurations.
 */
public class EncodeOptions {
    /** Default encoding options: 2 spaces indent, comma delimiter, no length marker */
    public static final EncodeOptions DEFAULT = new EncodeOptions();

    private final int indent;
    private final Delimiter delimiter;
    private final boolean lengthMarker;

    public EncodeOptions() {
        this(2, Delimiter.COMMA, false);
    }

    public EncodeOptions(int indent, Delimiter delimiter, boolean lengthMarker) {
        this.indent = indent;
        this.delimiter = delimiter;
        this.lengthMarker = lengthMarker;
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

    public boolean hasLengthMarker() {
        return lengthMarker;
    }

    // Factory methods for common configurations

    /**
     * Creates options with custom indent, using defaults for other settings.
     *
     * @param indent number of spaces per indentation level
     * @return new EncodeOptions with specified indent
     */
    public static EncodeOptions withIndent(int indent) {
        return new EncodeOptions(indent, Delimiter.COMMA, false);
    }

    /**
     * Creates options with custom delimiter, using defaults for other settings.
     *
     * @param delimiter the delimiter to use for arrays and tabular data
     * @return new EncodeOptions with specified delimiter
     */
    public static EncodeOptions withDelimiter(Delimiter delimiter) {
        return new EncodeOptions(2, delimiter, false);
    }

    /**
     * Creates options with length marker enabled, using defaults for other settings.
     *
     * @return new EncodeOptions with length marker enabled
     */
    public static EncodeOptions withLengthMarker() {
        return new EncodeOptions(2, Delimiter.COMMA, true);
    }

    /**
     * Creates options with tab delimiter for more compact output.
     *
     * @return new EncodeOptions with tab delimiter
     */
    public static EncodeOptions compact() {
        return new EncodeOptions(2, Delimiter.TAB, false);
    }

    /**
     * Creates options with tab delimiter and length marker for maximum clarity.
     *
     * @return new EncodeOptions with tab delimiter and length marker
     */
    public static EncodeOptions verbose() {
        return new EncodeOptions(2, Delimiter.TAB, true);
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
        private boolean lengthMarker = false;

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
         * Sets whether to include length marker (#) in array headers.
         *
         * @param lengthMarker true to include # prefix
         * @return this builder
         */
        public Builder lengthMarker(boolean lengthMarker) {
            this.lengthMarker = lengthMarker;
            return this;
        }

        /**
         * Builds the EncodeOptions with current configuration.
         *
         * @return new EncodeOptions instance
         */
        public EncodeOptions build() {
            return new EncodeOptions(indent, delimiter, lengthMarker);
        }
    }
}
