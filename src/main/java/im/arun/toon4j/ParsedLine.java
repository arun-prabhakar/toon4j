package im.arun.toon4j;

/**
 * Represents a parsed line with depth and content information.
 *
 * @param raw        Original line including indentation
 * @param depth      Indentation depth (indent / indentSize)
 * @param indent     Raw indentation in spaces
 * @param content    Content without leading spaces
 * @param lineNumber 1-based line number in the source
 */
record ParsedLine(String raw, int depth, int indent, String content, int lineNumber) {

    @Override
    public String toString() {
        return String.format("Line %d (depth=%d): %s", lineNumber, depth, content);
    }
}
