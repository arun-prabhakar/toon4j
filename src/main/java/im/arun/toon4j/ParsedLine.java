package im.arun.toon4j;

/**
 * Represents a parsed line with depth and content information.
 */
final class ParsedLine {
    final String raw;          // Original line
    final int depth;           // Indentation depth (indent / indentSize)
    final int indent;          // Raw indentation spaces
    final String content;      // Content without leading spaces
    final int lineNumber;      // 1-based line number

    ParsedLine(String raw, int depth, int indent, String content, int lineNumber) {
        this.raw = raw;
        this.depth = depth;
        this.indent = indent;
        this.content = content;
        this.lineNumber = lineNumber;
    }

    @Override
    public String toString() {
        return String.format("Line %d (depth=%d): %s", lineNumber, depth, content);
    }
}
