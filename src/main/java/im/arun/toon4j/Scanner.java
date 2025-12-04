package im.arun.toon4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Scans TOON input and converts it to parsed lines with depth information.
 */
final class Scanner {
    private Scanner() {}

    /**
     * Parse input string into lines with depth information.
     * @param source Input TOON string
     * @param indentSize Number of spaces per indentation level
     * @param strict Enable strict validation (no tabs, exact indent multiples)
     * @return List of parsed lines (excluding blank lines)
     */
    static List<ParsedLine> scan(String source, int indentSize, boolean strict) {
        if (source == null || source.isEmpty()) {
            return new ArrayList<>();
        }

        String[] lines = source.split("\\r?\\n", -1);
        return scanLines(java.util.Arrays.asList(lines), indentSize, strict);
    }

    /**
     * Parse pre-split lines into parsed lines with depth information.
     * @param lines Iterable of TOON lines (without trailing newlines)
     * @param indentSize Number of spaces per indentation level
     * @param strict Enable strict validation (no tabs, exact indent multiples)
     * @return List of parsed lines (excluding blank lines)
     */
    static List<ParsedLine> scanLines(Iterable<String> lines, int indentSize, boolean strict) {
        List<ParsedLine> result = new ArrayList<>();
        int lineNumber = 0;

        for (String line : lines) {
            lineNumber++;

            // Skip blank lines
            if (line.trim().isEmpty()) {
                continue;
            }

            // Calculate indentation
            int indent = countLeadingSpaces(line, strict, lineNumber);

            // Validate indent is multiple of indentSize in strict mode
            if (strict && indent % indentSize != 0) {
                throw new IllegalArgumentException(
                    String.format("Line %d: Indentation (%d spaces) must be a multiple of %d",
                        lineNumber, indent, indentSize)
                );
            }

            int depth = indent / indentSize;
            String content = line.substring(indent);

            result.add(new ParsedLine(line, depth, indent, content, lineNumber));
        }

        return result;
    }

    /**
     * Count leading spaces in a line.
     * In strict mode, rejects tabs.
     */
    private static int countLeadingSpaces(String line, boolean strict, int lineNumber) {
        int count = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == ' ') {
                count++;
            } else if (c == '\t') {
                if (strict) {
                    throw new IllegalArgumentException(
                        String.format("Line %d: Tabs not allowed in indentation (strict mode)", lineNumber)
                    );
                }
                // In lenient mode, treat tab as 4 spaces
                count += 4;
            } else {
                break;
            }
        }
        return count;
    }
}
