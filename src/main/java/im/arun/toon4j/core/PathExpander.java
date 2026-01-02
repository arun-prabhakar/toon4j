package im.arun.toon4j.core;

import im.arun.toon4j.PathExpansion;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Expands dotted keys into nested maps when enabled.
 */
public final class PathExpander {
    private static final Pattern IDENTIFIER_SEGMENT = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$");
    private static final Pattern DOT_PATTERN = Pattern.compile("\\.");

    private PathExpander() {
    }

    @SuppressWarnings("unchecked")
    public static Object expand(Object value, PathExpansion mode) {
        if (mode != PathExpansion.SAFE) {
            return value;
        }

        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            Map<String, Object> expanded = new LinkedHashMap<>();

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object val = entry.getValue();
                if (shouldExpand(key)) {
                    expandInto(expanded, key, val, mode);
                } else {
                    expanded.put(key, expand(val, mode));
                }
            }
            return expanded;
        }

        if (value instanceof Iterable) {
            Iterable<?> it = (Iterable<?>) value;
            java.util.List<Object> result = new java.util.ArrayList<>();
            for (Object item : it) {
                result.add(expand(item, mode));
            }
            return result;
        }

        return value;
    }

    private static boolean shouldExpand(String key) {
        if (key == null || !key.contains(".")) {
            return false;
        }
        String[] segments = DOT_PATTERN.split(key);
        for (String seg : segments) {
            if (!IDENTIFIER_SEGMENT.matcher(seg).matches()) {
                return false;
            }
        }
        return true;
    }

    private static void expandInto(Map<String, Object> target, String dottedKey, Object value, PathExpansion mode) {
        String[] segments = dottedKey.split("\\.");
        Map<String, Object> current = target;

        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];
            if (!IDENTIFIER_SEGMENT.matcher(segment).matches()) {
                // Unsafe segment; keep literal key
                current.put(dottedKey, expand(value, mode));
                return;
            }

            boolean isLeaf = i == segments.length - 1;
            if (isLeaf) {
                current.put(segment, expand(value, mode));
                return;
            }

            Object next = current.get(segment);
            if (!(next instanceof Map)) {
                Map<String, Object> nested = new LinkedHashMap<>();
                current.put(segment, nested);
                current = nested;
            } else {
                current = (Map<String, Object>) next;
            }
        }
    }
}
