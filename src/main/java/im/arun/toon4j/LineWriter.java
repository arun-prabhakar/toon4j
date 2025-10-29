package im.arun.toon4j;

import java.util.ArrayList;
import java.util.List;

public class LineWriter {
    private final int spacesPerLevel;
    private final List<String> lines;

    public LineWriter(int spacesPerLevel) {
        this.spacesPerLevel = spacesPerLevel;
        this.lines = new ArrayList<>();
    }

    public void push(int depth, String content) {
        String indent = " ".repeat(depth * spacesPerLevel);
        lines.add(indent + content);
    }

    public void pushRaw(String content) {
        lines.add(content);
    }

    public List<String> getLines() {
        return new ArrayList<>(lines);
    }

    @Override
    public String toString() {
        return String.join("\n", lines);
    }
}
