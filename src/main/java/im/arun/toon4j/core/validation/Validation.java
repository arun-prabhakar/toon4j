package im.arun.toon4j.core.validation;

import java.util.regex.Pattern;

/**
 * Shared validation helpers mirroring the reference TypeScript implementation.
 */
public final class Validation {
    private static final Pattern IDENTIFIER_SEGMENT = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$");

    private Validation() {
    }

    /**
     * Returns true if the segment is a valid identifier (used for safe folding/expansion).
     */
    public static boolean isIdentifierSegment(String segment) {
        return segment != null && IDENTIFIER_SEGMENT.matcher(segment).matches();
    }
}
