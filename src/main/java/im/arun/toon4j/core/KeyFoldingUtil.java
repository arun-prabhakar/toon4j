package im.arun.toon4j.core;

import im.arun.toon4j.EncodeOptions;
import im.arun.toon4j.KeyFolding;
import im.arun.toon4j.Normalize;
import im.arun.toon4j.core.validation.Validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility to fold nested single-key objects into dotted paths.
 */
public final class KeyFoldingUtil {
    private KeyFoldingUtil() {
    }

    public static FoldResult tryFoldKeyChain(
        String key,
        Object value,
        List<String> siblings,
        EncodeOptions options,
        Set<String> rootLiteralKeys,
        String pathPrefix,
        Integer remainingDepth,
        Set<String> blockedKeys
    ) {
        // If aggressive flatten is off and key folding is off, skip
        if (!options.isFlatten() && options.getKeyFolding() != KeyFolding.SAFE) {
            return null;
        }

        if (!Normalize.isJsonObject(value)) {
            return null;
        }

        // Avoid refolding keys that were already flattened in this pass
        if (blockedKeys != null && blockedKeys.contains(key)) {
            return null;
        }

        int depthBudget = remainingDepth != null ? remainingDepth : options.getFlattenDepth();
        Chain chain = collectSingleKeyChain(key, (Map<String, Object>) value, depthBudget);

        if (chain.segments.size() < 2) {
            return null;
        }

        // ensure all segments are identifier-safe
        if (options.getKeyFolding() == KeyFolding.SAFE || !options.isFlatten()) {
            for (String seg : chain.segments) {
                if (!Validation.isIdentifierSegment(seg)) {
                    return null;
                }
            }
        }

        String foldedKey = String.join(".", chain.segments);
        String absolutePath = pathPrefix == null || pathPrefix.isEmpty()
            ? foldedKey
            : pathPrefix + "." + foldedKey;

        if (siblings.contains(foldedKey)) {
            return null;
        }

        if (rootLiteralKeys != null && rootLiteralKeys.contains(absolutePath)) {
            return null;
        }

        if (blockedKeys != null) {
            blockedKeys.add(foldedKey);
        }

        return new FoldResult(foldedKey, chain.remainder, chain.leafValue, chain.segments.size());
    }

    private static Chain collectSingleKeyChain(String startKey, Map<String, Object> startValue, int maxDepth) {
        List<String> segments = new ArrayList<>();
        segments.add(startKey);
        Object current = startValue;

        while (segments.size() < maxDepth && Normalize.isJsonObject(current)) {
            Map<String, Object> map = (Map<String, Object>) current;
            if (map.size() != 1) {
                break;
            }
            String nextKey = map.keySet().iterator().next();
            Object nextValue = map.get(nextKey);
            segments.add(nextKey);
            current = nextValue;
        }

        Object remainder = null;
        Object leaf = current;
        if (Normalize.isJsonObject(current) && !((Map<?, ?>) current).isEmpty()) {
            remainder = current;
        }

        return new Chain(segments, remainder, leaf);
    }

    public record FoldResult(String foldedKey, Object remainder, Object leafValue, int segmentCount) {}

    private record Chain(List<String> segments, Object remainder, Object leafValue) {}
}
