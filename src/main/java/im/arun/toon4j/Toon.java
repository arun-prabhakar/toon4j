package im.arun.toon4j;

import im.arun.toon4j.core.Constants;

/**
 * Token-Oriented Object Notation (TOON) encoder and decoder for Java.
 *
 * TOON is a compact, human-readable format designed for passing structured data
 * to Large Language Models with significantly reduced token usage.
 *
 * Example encoding:
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
 *
 * Example decoding:
 * <pre>
 * String toon = """
 *     id: 123
 *     name: Ada
 *     tags[2]: reading,gaming
 *     active: true
 *     """;
 *
 * Object data = Toon.decode(toon);
 * // Returns: {id=123, name=Ada, tags=[reading, gaming], active=true}
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

    /**
     * Decode TOON format string to Java objects with default options.
     *
     * @param input TOON-formatted string
     * @return decoded value (Map, List, or primitive)
     * @throws IllegalArgumentException if input is invalid or empty
     */
    public static Object decode(String input) {
        return decode(input, new DecodeOptions());
    }

    /**
     * Decode TOON format string to Java objects with custom options.
     *
     * @param input TOON-formatted string
     * @param options decoding options (indent size, strict validation)
     * @return decoded value (Map, List, or primitive)
     * @throws IllegalArgumentException if input is invalid or empty
     */
    public static Object decode(String input, DecodeOptions options) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Cannot decode empty input: input must be a non-empty string");
        }

        // Scan input into parsed lines
        java.util.List<ParsedLine> lines = Scanner.scan(input, options.getIndent(), options.isStrict());

        if (lines.isEmpty()) {
            throw new IllegalArgumentException("Cannot decode empty input: input must be a non-empty string");
        }

        // Create cursor and decode
        LineCursor cursor = new LineCursor(lines);
        return ToonDecoders.decodeValue(cursor, options);
    }
}