package im.arun.toon4j.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Thread-local object pools for frequently allocated objects.
 * Reduces GC pressure by reusing objects within the same thread.
 *
 * <p><b>Important:</b> In managed environments (servlet containers, thread pools,
 * application servers), call {@link #cleanup()} when a thread is done using TOON4J
 * to prevent memory leaks. This is especially critical in environments where threads
 * are reused across different contexts.
 *
 * <p>Example usage in a servlet filter:
 * <pre>{@code
 * public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) {
 *     try {
 *         chain.doFilter(req, res);
 *     } finally {
 *         Toon.cleanupThreadLocals();  // Clean up TOON4J thread-local resources
 *     }
 * }
 * }</pre>
 *
 * @see im.arun.toon4j.Toon#cleanupThreadLocals()
 */
public final class ObjectPool {
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
        if (sb != null && sb.length() > 4096) {
            // If too large, discard and create a new one
            stringBuilderPool.set(new StringBuilder(512));
        }
        // Otherwise, it stays in ThreadLocal for reuse
    }

    /**
     * Return an ArrayList to the pool (for clarity, but not strictly necessary with ThreadLocal).
     */
    static void returnArrayList(List<Object> list) {
        if (list != null && list.size() > 256) {
            // If too large, discard and create a new one
            arrayListPool.set(new ArrayList<>(32));
        }
        // Otherwise, it stays in ThreadLocal for reuse
    }

    /**
     * Clean up ThreadLocal resources to prevent memory leaks.
     * Should be called when the thread is done using pooled objects,
     * especially in managed thread pools or application servers.
     *
     * <p>Call this method:
     * <ul>
     *   <li>At the end of servlet request processing (in a filter)</li>
     *   <li>Before returning a thread to a thread pool</li>
     *   <li>In application shutdown hooks</li>
     * </ul>
     *
     * @see im.arun.toon4j.Toon#cleanupThreadLocals()
     */
    public static void cleanup() {
        stringBuilderPool.remove();
        arrayListPool.remove();
    }
}
