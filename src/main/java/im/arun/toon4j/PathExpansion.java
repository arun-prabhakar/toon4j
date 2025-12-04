package im.arun.toon4j;

/**
 * Controls path expansion during decoding.
 *
 * SAFE expands dotted keys (e.g., {@code data.meta.value})
 * into nested maps when each segment is a valid identifier.
 * OFF leaves dotted keys as-is.
 */
public enum PathExpansion {
    OFF,
    SAFE
}
