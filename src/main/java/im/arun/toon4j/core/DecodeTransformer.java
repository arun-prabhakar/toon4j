package im.arun.toon4j.core;

import im.arun.toon4j.DecodeReplacer;
import im.arun.toon4j.Normalize;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Applies a {@link DecodeReplacer} across a decoded value tree.
 */
public final class DecodeTransformer {
    private DecodeTransformer() {
    }

    public static Object apply(Object root, DecodeReplacer replacer) {
        Object replacedRoot = replacer.replace("", root, List.of());
        Object normalized = DecodeReplacer.OMIT.equals(replacedRoot) ? root : Normalize.normalizeValue(replacedRoot);
        return transform(normalized, replacer, List.of());
    }

    @SuppressWarnings("unchecked")
    private static Object transform(Object value, DecodeReplacer replacer, List<Object> path) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                Object child = entry.getValue();
                List<Object> childPath = extend(path, key);
                Object replaced = replacer.replace(key, child, childPath);
                if (DecodeReplacer.OMIT.equals(replaced)) {
                    continue;
                }
                Object normalized = Normalize.normalizeValue(replaced);
                result.put(key, transform(normalized, replacer, childPath));
            }
            return result;
        }

        if (value instanceof List<?> list) {
            List<Object> result = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                Object child = list.get(i);
                List<Object> childPath = extend(path, i);
                Object replaced = replacer.replace(String.valueOf(i), child, childPath);
                if (DecodeReplacer.OMIT.equals(replaced)) {
                    continue;
                }
                Object normalized = Normalize.normalizeValue(replaced);
                result.add(transform(normalized, replacer, childPath));
            }
            return result;
        }

        return value;
    }

    private static List<Object> extend(List<Object> path, Object segment) {
        List<Object> next = new ArrayList<>(path.size() + 1);
        next.addAll(path);
        next.add(segment);
        return next;
    }
}
