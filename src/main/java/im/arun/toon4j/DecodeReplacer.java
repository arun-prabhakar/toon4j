package im.arun.toon4j;

import java.util.List;

/**
 * Callback to transform values during decoding.
 * Returning {@link #OMIT} removes the property/element; returning any other value replaces it.
 */
@FunctionalInterface
public interface DecodeReplacer {
    Object OMIT = new Object();

    /**
     * @param key  property name or array index (as string). Root uses an empty string.
     * @param value current decoded value
     * @param path  path from root to this node
     * @return replacement value, or {@link #OMIT} to remove it
     */
    Object replace(String key, Object value, List<Object> path);
}
