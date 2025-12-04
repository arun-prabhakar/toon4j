package im.arun.toon4j;

import java.util.List;

/**
 * Callback invoked for every value during encoding.
 * Similar to {@link com.fasterxml.jackson.databind.ser.PropertyFilter} semantics,
 * but keeps the TOON-specific path information.
 *
 * Returning {@link #OMIT} skips the property/element. Returning any other value
 * (including {@code null}) replaces the value and will be normalized before encoding.
 */
@FunctionalInterface
public interface EncodeReplacer {
    /**
     * Sentinel object used to indicate omission of a value.
     */
    Object OMIT = new Object();

    /**
     * Transform a value before encoding.
     *
     * @param key  the property key or array index (root uses empty string)
     * @param value normalized value at this path
     * @param path  path from the root to this node
     * @return replacement value, or {@link #OMIT} to remove the entry
     */
    Object replace(String key, Object value, List<Object> path);
}
