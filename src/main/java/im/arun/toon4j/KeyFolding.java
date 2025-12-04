package im.arun.toon4j;

/**
 * Controls how keys are folded during encoding.
 *
 * SAFE folds single-key object chains into dot-separated keys when
 * all segments are valid identifiers and no collisions are detected.
 * OFF disables folding.
 */
public enum KeyFolding {
    OFF,
    SAFE
}
