package im.arun.toon4j;

import java.util.List;

/**
 * Cursor for traversing parsed lines during decoding.
 */
final class LineCursor {
    private final List<ParsedLine> lines;
    private int position;

    LineCursor(List<ParsedLine> lines) {
        this.lines = lines;
        this.position = 0;
    }

    /**
     * Peek at the current line without advancing.
     */
    ParsedLine peek() {
        if (atEnd()) {
            return null;
        }
        return lines.get(position);
    }

    /**
     * Get the current line and advance to the next.
     */
    ParsedLine next() {
        if (atEnd()) {
            throw new IllegalStateException("No more lines available");
        }
        return lines.get(position++);
    }

    /**
     * Advance to the next line.
     */
    void advance() {
        if (!atEnd()) {
            position++;
        }
    }

    /**
     * Check if at the end of lines.
     */
    boolean atEnd() {
        return position >= lines.size();
    }

    /**
     * Get current position.
     */
    int getPosition() {
        return position;
    }

    /**
     * Get total number of lines.
     */
    int size() {
        return lines.size();
    }

    /**
     * Peek at line at specific offset from current position.
     * @param offset Offset from current position (0 = current, 1 = next, etc.)
     */
    ParsedLine peekAt(int offset) {
        int index = position + offset;
        if (index < 0 || index >= lines.size()) {
            return null;
        }
        return lines.get(index);
    }
}
