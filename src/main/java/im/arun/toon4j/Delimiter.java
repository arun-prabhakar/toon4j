package im.arun.toon4j;

public enum Delimiter {
    COMMA(","),
    TAB("\t"),
    PIPE("|");

    private final String value;

    Delimiter(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
