package im.arun.toon4j;

/**
 * Options for decoding TOON format.
 */
public class DecodeOptions {
    private final int indent;
    private final boolean strict;
    private final PathExpansion expandPaths;
    private final DecodeReplacer replacer;

    /**
     * Create decode options with default values.
     * Default: indent=2, strict=true, expandPaths=OFF
     */
    public DecodeOptions() {
        this(2, true, PathExpansion.OFF, null);
    }

    /**
     * Create decode options with custom indent size.
     * @param indent Indentation size (number of spaces per level)
     */
    public DecodeOptions(int indent) {
        this(indent, true, PathExpansion.OFF, null);
    }

    /**
     * Create decode options with all parameters.
     * @param indent Indentation size (number of spaces per level)
     * @param strict Enable strict validation (counts, indentation, blank lines)
     * @param expandPaths Enable path expansion for dotted keys
     */
    public DecodeOptions(int indent, boolean strict, PathExpansion expandPaths) {
        this(indent, strict, expandPaths, null);
    }

    /**
     * Create decode options with all parameters and replacer.
     */
    public DecodeOptions(int indent, boolean strict, PathExpansion expandPaths, DecodeReplacer replacer) {
        if (indent < 1) {
            throw new IllegalArgumentException("indent must be >= 1");
        }
        this.indent = indent;
        this.strict = strict;
        this.expandPaths = expandPaths == null ? PathExpansion.OFF : expandPaths;
        this.replacer = replacer;
    }

    public int getIndent() {
        return indent;
    }

    public boolean isStrict() {
        return strict;
    }

    public PathExpansion getExpandPaths() {
        return expandPaths;
    }

    public DecodeReplacer getReplacer() {
        return replacer;
    }

    /**
     * Create a lenient decode options (no strict validation).
     */
    public static DecodeOptions lenient() {
        return new DecodeOptions(2, false, PathExpansion.OFF, null);
    }

    /**
     * Create a lenient decode options with custom indent.
     */
    public static DecodeOptions lenient(int indent) {
        return new DecodeOptions(indent, false, PathExpansion.OFF, null);
    }

    /**
     * Enable safe path expansion.
     */
    public static DecodeOptions withPathExpansion(PathExpansion mode) {
        return new DecodeOptions(2, true, mode, null);
    }

    /**
     * Attach a replacer to transform decoded values.
     */
    public static DecodeOptions withReplacer(DecodeReplacer replacer) {
        return new DecodeOptions(2, true, PathExpansion.OFF, replacer);
    }
}
