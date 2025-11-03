package im.arun.toon4j.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Thread-local object pools for frequently allocated objects.
 * Reduces GC pressure by reusing objects within the same thread.
 */
final class ObjectPool {
    private ObjectPool() {}

    // ThreadLocal StringBuilder pool
    private static final ThreadLocal<StringBuilder> stringBuilderPool = ThreadLocal.withInitial(() -> new StringBuilder(512));

    // ThreadLocal ArrayList pool
    private static final ThreadLocal<List<Object>> arrayListPool = ThreadLocal.withInitial(() -> new ArrayList<>(32));

    /**
     * Get a pooled StringBuilder, cleared and ready to use.
     */
    static StringBuilder getStringBuilder() {
        StringBuilder sb = stringBuilderPool.get();
        sb.setLength(0); // Clear
        return sb;
    }

    /**
     * Get a pooled ArrayList, cleared and ready to use.
     */
    static List<Object> getArrayList() {
        List<Object> list = arrayListPool.get();
        list.clear(); // Clear
        return list;
    }

    /**
     * Return a StringBuilder to the pool (for clarity, but not strictly necessary with ThreadLocal).
     */
    static void returnStringBuilder(StringBuilder sb) {
        if (sb.length() > 4096) {
            // If too large, discard and create a new one
            stringBuilderPool.set(new StringBuilder(512));
        }
        // Otherwise, it stays in ThreadLocal for reuse
    }

    /**
     * Return an ArrayList to the pool (for clarity, but not strictly necessary with ThreadLocal).
     */
    static void returnArrayList(List<Object> list) {
        if (list.size() > 256) {
            // If too large, discard and create a new one
            arrayListPool.set(new ArrayList<>(32));
        }
        // Otherwise, it stays in ThreadLocal for reuse
    }
}
