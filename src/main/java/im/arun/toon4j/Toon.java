package im.arun.toon4j;

/**
 * Token-Oriented Object Notation (TOON) encoder for Java.
 *
 * TOON is a compact, human-readable format designed for passing structured data
 * to Large Language Models with significantly reduced token usage.
 *
 * Example usage:
 * <pre>
 * Map&lt;String, Object&gt; data = Map.of(
 *     "user", Map.of(
 *         "id", 123,
 *         "name", "Ada",
 *         "tags", List.of("reading", "gaming"),
 *         "active", true
 *     )
 * );
 *
 * String toon = Toon.encode(data);
 * System.out.println(toon);
 * // Output:
 * // user:
 * //   id: 123
 * //   name: Ada
 * //   tags[2]: reading,gaming
 * //   active: true
 * </pre>
 */
public final class Toon {
    private Toon() {}

    /**
     * Encode a value to TOON format with default options.
     *
     * @param value the value to encode (Map, List, or primitive)
     * @return TOON-formatted string
     */
    public static String encode(Object value) {
        return encode(value, new EncodeOptions());
    }

    /**
     * Encode a value to TOON format with custom options.
     *
     * @param value the value to encode (Map, List, or primitive)
     * @param options encoding options (indent, delimiter, lengthMarker)
     * @return TOON-formatted string
     */
    public static String encode(Object value, EncodeOptions options) {
        if (value == null) {
            return Constants.NULL_LITERAL;
        }

        // Empty object returns empty string
        if (value instanceof java.util.Map && ((java.util.Map<?, ?>) value).isEmpty()) {
            return "";
        }

        // Normalize the value
        Object normalized = Normalize.normalizeValue(value);

        // Encode the normalized value
        return Encoders.encodeValue(normalized, options);
    }
}
