package im.arun.toon4j;

import im.arun.toon4j.core.Constants;
import im.arun.toon4j.core.DecodeTransformer;
import im.arun.toon4j.core.EventBuilder;
import im.arun.toon4j.core.ObjectPool;
import im.arun.toon4j.core.PathExpander;
import im.arun.toon4j.core.Replacer;

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
     * Clean up thread-local resources used by TOON4J.
     *
     * <p>Call this method in managed environments (servlet containers, thread pools,
     * application servers) to prevent memory leaks when threads are reused.
     *
     * <p>Example usage in a servlet filter:
     * <pre>{@code
     * public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) {
     *     try {
     *         chain.doFilter(req, res);
     *     } finally {
     *         Toon.cleanupThreadLocals();
     *     }
     * }
     * }</pre>
     *
     * <p>This is a no-op if no thread-local resources have been allocated.
     */
    public static void cleanupThreadLocals() {
        ObjectPool.cleanup();
    }

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

        if (options.getReplacer() != null) {
            normalized = Replacer.apply(normalized, options.getReplacer());
        }

        // Encode the normalized value
        return Encoders.encodeValue(normalized, options);
    }

    /**
     * Encode a value to TOON format as an iterable of lines.
     *
     * @param value the value to encode
     * @return TOON-formatted lines without trailing newlines
     */
    public static Iterable<String> encodeLines(Object value) {
        return encodeLines(value, new EncodeOptions());
    }

    /**
     * Encode a value to TOON format as an iterable of lines with options.
     *
     * @param value the value to encode
     * @param options encoding options
     * @return TOON-formatted lines without trailing newlines
     */
    public static Iterable<String> encodeLines(Object value, EncodeOptions options) {
        if (value == null) {
            return java.util.List.of(Constants.NULL_LITERAL);
        }

        Object normalized = Normalize.normalizeValue(value);
        if (options.getReplacer() != null) {
            normalized = Replacer.apply(normalized, options.getReplacer());
        }
        return Encoders.encodeValueLines(normalized, options);
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
        Object decoded = ToonDecoders.decodeValue(cursor, options);

        if (options.getExpandPaths() == PathExpansion.SAFE) {
            decoded = PathExpander.expand(decoded, options.getExpandPaths());
        }
        if (options.getReplacer() != null) {
            decoded = DecodeTransformer.apply(decoded, options.getReplacer());
        }
        return decoded;
    }

    /**
     * Decode TOON content to streaming events (built from the decoded value).
     *
     * @param input TOON-formatted string
     * @param options decoding options
     * @return list of decode events in traversal order
     */
    public static java.util.List<DecodeEvent> decodeToEvents(String input, DecodeOptions options) {
        Object decoded = decode(input, options);
        return EventBuilder.buildEvents(decoded);
    }

    /**
     * Decode TOON lines to streaming events (built from the decoded value).
     */
    public static java.util.List<DecodeEvent> decodeLinesToEvents(Iterable<String> lines, DecodeOptions options) {
        Object decoded = decodeLines(lines, options);
        return EventBuilder.buildEvents(decoded);
    }

    /**
     * Decode TOON lines (already split) to Java objects with custom options.
     *
     * @param lines iterable of TOON lines (no trailing newline characters)
     * @param options decoding options
     * @return decoded value (Map, List, or primitive)
     */
    public static Object decodeLines(Iterable<String> lines, DecodeOptions options) {
        java.util.List<ParsedLine> parsedLines = Scanner.scanLines(lines, options.getIndent(), options.isStrict());

        if (parsedLines.isEmpty()) {
            throw new IllegalArgumentException("Cannot decode empty input: lines must yield at least one entry");
        }

        LineCursor cursor = new LineCursor(parsedLines);
        Object decoded = ToonDecoders.decodeValue(cursor, options);
        if (options.getExpandPaths() == PathExpansion.SAFE) {
            decoded = PathExpander.expand(decoded, options.getExpandPaths());
        }
        if (options.getReplacer() != null) {
            decoded = DecodeTransformer.apply(decoded, options.getReplacer());
        }
        return decoded;
    }

    /**
     * Decode TOON format string to a POJO of the specified type with default options.
     *
     * @param input TOON-formatted string
     * @param targetClass the target POJO class
     * @param <T> the type of the POJO
     * @return deserialized POJO instance
     * @throws IllegalArgumentException if input is invalid or empty
     * @throws RuntimeException if deserialization fails
     */
    public static <T> T decode(String input, Class<T> targetClass) {
        return decode(input, targetClass, new DecodeOptions());
    }

    /**
     * Decode TOON format string to a POJO of the specified type with custom options.
     *
     * @param input TOON-formatted string
     * @param targetClass the target POJO class
     * @param options decoding options (indent size, strict validation)
     * @param <T> the type of the POJO
     * @return deserialized POJO instance
     * @throws IllegalArgumentException if input is invalid or empty
     * @throws RuntimeException if deserialization fails
     */
    @SuppressWarnings("unchecked")
    public static <T> T decode(String input, Class<T> targetClass, DecodeOptions options) {
        if (targetClass == null) {
            throw new IllegalArgumentException("Target class cannot be null");
        }

        // First decode to Map/List/primitive
        Object decoded = decode(input, options);

        // If target is Object.class or the decoded type matches, return as-is
        if (targetClass == Object.class || targetClass.isInstance(decoded)) {
            return (T) decoded;
        }

        // If decoded is a Map, deserialize to POJO
        if (decoded instanceof java.util.Map) {
            return PojoDeserializer.fromMap((java.util.Map<String, Object>) decoded, targetClass);
        }

        // If decoded is a List and target is an array, convert
        if (decoded instanceof java.util.List && targetClass.isArray()) {
            java.util.List<?> list = (java.util.List<?>) decoded;
            return (T) PojoDeserializer.deserializeValue(list, targetClass);
        }

        // For primitives or direct assignment
        return (T) PojoDeserializer.deserializeValue(decoded, targetClass);
    }
}
