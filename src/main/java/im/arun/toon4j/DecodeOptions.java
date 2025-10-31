package im.arun.toon4j;

/**
 * Options for decoding TOON format.
 */
public class DecodeOptions {
    private final int indent;
    private final boolean strict;

    /**
     * Create decode options with default values.
     * Default: indent=2, strict=true
     */
    public DecodeOptions() {
        this(2, true);
    }

    /**
     * Create decode options with custom indent size.
     * @param indent Indentation size (number of spaces per level)
     */
    public DecodeOptions(int indent) {
        this(indent, true);
    }

    /**
     * Create decode options with all parameters.
     * @param indent Indentation size (number of spaces per level)
     * @param strict Enable strict validation (counts, indentation, blank lines)
     */
    public DecodeOptions(int indent, boolean strict) {
        if (indent < 1) {
            throw new IllegalArgumentException("indent must be >= 1");
        }
        this.indent = indent;
        this.strict = strict;
    }

    public int getIndent() {
        return indent;
    }

    public boolean isStrict() {
        return strict;
    }

    /**
     * Create a lenient decode options (no strict validation).
     */
    public static DecodeOptions lenient() {
        return new DecodeOptions(2, false);
    }

    /**
     * Create a lenient decode options with custom indent.
     */
    public static DecodeOptions lenient(int indent) {
        return new DecodeOptions(indent, false);
    }
}
