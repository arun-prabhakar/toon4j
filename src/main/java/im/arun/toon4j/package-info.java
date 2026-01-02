/**
 * TOON4J - High-performance, zero-dependency TOON encoder/decoder for Java.
 *
 * <p>TOON (Token-Oriented Object Notation) is a compact, human-readable format
 * designed for passing structured data to Large Language Models with significantly
 * reduced token usage (typically 30-60% fewer tokens than JSON).
 *
 * <h2>Quick Start</h2>
 *
 * <p><b>Encoding:</b>
 * <pre>{@code
 * Map<String, Object> data = Map.of(
 *     "user", Map.of("id", 123, "name", "Ada")
 * );
 * String toon = Toon.encode(data);
 * // Output:
 * // user:
 * //   id: 123
 * //   name: Ada
 * }</pre>
 *
 * <p><b>Decoding:</b>
 * <pre>{@code
 * String toon = "id: 123\nname: Ada";
 * Map<String, Object> data = (Map<String, Object>) Toon.decode(toon);
 * }</pre>
 *
 * <p><b>POJO Deserialization:</b>
 * <pre>{@code
 * User user = Toon.decode(toon, User.class);
 * }</pre>
 *
 * <h2>Key Features</h2>
 * <ul>
 *   <li>30-60% fewer tokens than JSON for LLM contexts</li>
 *   <li>Sub-5ms encoding for 256KB payloads</li>
 *   <li>Zero external runtime dependencies</li>
 *   <li>Full round-trip support (encode/decode)</li>
 *   <li>Automatic POJO serialization with cached reflection</li>
 *   <li>Streaming APIs ({@link im.arun.toon4j.Toon#encodeLines}, {@link im.arun.toon4j.Toon#decodeLines})</li>
 *   <li>Thread-safe with optimized object pooling</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 *
 * <p>All public methods in {@link im.arun.toon4j.Toon} are thread-safe. The library uses
 * thread-local object pools to reduce GC pressure. In managed environments (servlet containers,
 * thread pools), call {@link im.arun.toon4j.Toon#cleanupThreadLocals()} when a thread is done
 * processing to prevent memory leaks.
 *
 * <h2>Main Entry Points</h2>
 * <ul>
 *   <li>{@link im.arun.toon4j.Toon} - Main API for encoding and decoding</li>
 *   <li>{@link im.arun.toon4j.EncodeOptions} - Configuration for encoding</li>
 *   <li>{@link im.arun.toon4j.DecodeOptions} - Configuration for decoding</li>
 * </ul>
 *
 * @see im.arun.toon4j.Toon
 * @see im.arun.toon4j.EncodeOptions
 * @see im.arun.toon4j.DecodeOptions
 * @see <a href="https://github.com/arun-prabhakar/toon4j">GitHub Repository</a>
 */
package im.arun.toon4j;
