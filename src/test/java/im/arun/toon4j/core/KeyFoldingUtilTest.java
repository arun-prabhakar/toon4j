package im.arun.toon4j.core;

import im.arun.toon4j.EncodeOptions;
import im.arun.toon4j.KeyFolding;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class KeyFoldingUtilTest {

    @Test
    void foldsSingleKeyChainWhenFlattenEnabled() {
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("leaf", 1);
        Map<String, Object> mid = new LinkedHashMap<>();
        mid.put("child", nested);

        EncodeOptions options = EncodeOptions.builder()
            .flatten(true)
            .flattenDepth(5)
            .keyFolding(KeyFolding.SAFE)
            .build();

        var result = KeyFoldingUtil.tryFoldKeyChain(
            "root",
            mid,
            List.of("root"),
            options,
            Set.of(),
            "",
            null,
            new HashSet<>()
        );

        assertNotNull(result);
        assertEquals("root.child.leaf", result.foldedKey());
        assertEquals(1, result.leafValue());
        assertNull(result.remainder());
    }

    @Test
    void skipsFoldForInvalidIdentifierSegments() {
        Map<String, Object> nested = Map.of("bad-key", 1);
        EncodeOptions options = EncodeOptions.builder()
            .flatten(true)
            .keyFolding(KeyFolding.SAFE)
            .build();

        var result = KeyFoldingUtil.tryFoldKeyChain(
            "root",
            nested,
            List.of("root"),
            options,
            Set.of(),
            "",
            null,
            new HashSet<>()
        );

        assertNull(result);
    }

    @Test
    void respectsDepthBudget() {
        Map<String, Object> nested = Map.of("b", Map.of("c", 1));
        EncodeOptions options = EncodeOptions.builder()
            .flatten(true)
            .flattenDepth(2)
            .keyFolding(KeyFolding.SAFE)
            .build();

        // Depth budget of 1 (remainingDepth=1) prevents folding the full chain
        var result = KeyFoldingUtil.tryFoldKeyChain(
            "a",
            (Map<String, Object>) nested,
            List.of("a"),
            options,
            Set.of(),
            "",
            1,
            new HashSet<>()
        );

        assertNull(result);
    }

    @Test
    void blockedKeysPreventRefolding() {
        Map<String, Object> nested = Map.of("b", 1);
        EncodeOptions options = EncodeOptions.builder()
            .flatten(true)
            .keyFolding(KeyFolding.SAFE)
            .build();

        Set<String> blocked = new HashSet<>();
        blocked.add("a");

        var result = KeyFoldingUtil.tryFoldKeyChain(
            "a",
            (Map<String, Object>) nested,
            List.of("a"),
            options,
            Set.of(),
            "",
            null,
            blocked
        );

        assertNull(result);
    }

    @Test
    void skipsFoldWhenLiteralPathExists() {
        Map<String, Object> nested = Map.of("b", 1);
        EncodeOptions options = EncodeOptions.builder()
            .flatten(true)
            .keyFolding(KeyFolding.SAFE)
            .build();

        var result = KeyFoldingUtil.tryFoldKeyChain(
            "a",
            (Map<String, Object>) nested,
            List.of("a", "a.b"),
            options,
            Set.of("a.b"),
            "",
            null,
            new HashSet<>()
        );

        assertNull(result);
    }
}
