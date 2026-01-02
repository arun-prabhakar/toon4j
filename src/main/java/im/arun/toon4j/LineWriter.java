package im.arun.toon4j;

import java.util.ArrayList;
import java.util.List;

public class LineWriter {
    private final int spacesPerLevel;
    private final List<String> lines;
    private final String[] indentCache; // Cache common indents
    private int estimatedSize; // Track total size for pre-sizing StringBuilder

    public LineWriter(int spacesPerLevel) {
        this.spacesPerLevel = spacesPerLevel;
        this.lines = new ArrayList<>();
        this.estimatedSize = 0;

        // Pre-compute indents for depths 0-10 (covers 99% of cases)
        this.indentCache = new String[11];
        for (int i = 0; i <= 10; i++) {
            indentCache[i] = " ".repeat(i * spacesPerLevel);
        }
    }

    public void push(int depth, String content) {
        // Use cached indent if available, otherwise compute
        String indent = depth <= 10
            ? indentCache[depth]
            : " ".repeat(depth * spacesPerLevel);

        String line = indent + content;
        lines.add(line);
        estimatedSize += line.length() + 1; // +1 for newline
    }

    public List<String> getLines() {
        return new ArrayList<>(lines);
    }

    @Override
    public String toString() {
        if (lines.isEmpty()) {
            return "";
        }

        // Pre-size StringBuilder to avoid reallocations
        StringBuilder sb = new StringBuilder(estimatedSize);
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                sb.append('\n');
            }
            sb.append(lines.get(i));
        }
        return sb.toString();
    }
}
