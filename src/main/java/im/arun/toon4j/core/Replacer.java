package im.arun.toon4j.core;

import im.arun.toon4j.EncodeReplacer;
import im.arun.toon4j.Normalize;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Applies an {@link EncodeReplacer} to a normalized value tree.
 */
public final class Replacer {
    private Replacer() {
    }

    /**
     * Applies the replacer to the normalized value.
     *
     * @param root      normalized value (Map/List/primitive)
     * @param replacer  replacer callback
     * @return transformed value tree
     */
    public static Object apply(Object root, EncodeReplacer replacer) {
        // Root uses empty key and empty path
        Object replacedRoot = replacer.replace("", root, List.of());

        // For root, OMIT means "no change" (cannot drop the root)
        Object normalizedRoot = EncodeReplacer.OMIT.equals(replacedRoot)
            ? root
            : Normalize.normalizeValue(replacedRoot);

        return transformChildren(normalizedRoot, replacer, List.of());
    }

    @SuppressWarnings("unchecked")
    private static Object transformChildren(Object value, EncodeReplacer replacer, List<Object> path) {
        if (Normalize.isJsonObject(value)) {
            Map<String, Object> obj = (Map<String, Object>) value;
            Map<String, Object> result = new LinkedHashMap<>();

            for (Map.Entry<String, Object> entry : obj.entrySet()) {
                String key = entry.getKey();
                Object childValue = entry.getValue();
                List<Object> childPath = extendPath(path, key);
                Object replaced = replacer.replace(key, childValue, childPath);

                if (EncodeReplacer.OMIT.equals(replaced)) {
                    continue; // omit this property
                }

                Object normalized = Normalize.normalizeValue(replaced);
                result.put(key, transformChildren(normalized, replacer, childPath));
            }
            return result;
        }

        if (Normalize.isJsonArray(value)) {
            List<Object> arr = (List<Object>) value;
            List<Object> result = new ArrayList<>();

            for (int i = 0; i < arr.size(); i++) {
                Object childValue = arr.get(i);
                List<Object> childPath = extendPath(path, i);
                Object replaced = replacer.replace(String.valueOf(i), childValue, childPath);

                if (EncodeReplacer.OMIT.equals(replaced)) {
                    continue; // omit element
                }

                Object normalized = Normalize.normalizeValue(replaced);
                result.add(transformChildren(normalized, replacer, childPath));
            }
            return result;
        }

        // Primitive
        return value;
    }

    private static List<Object> extendPath(List<Object> path, Object segment) {
        List<Object> newPath = new ArrayList<>(path.size() + 1);
        newPath.addAll(path);
        newPath.add(segment);
        return newPath;
    }
}
