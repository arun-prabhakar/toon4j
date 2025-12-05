package im.arun.toon4j.core;

import im.arun.toon4j.DecodeReplacer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DecodeTransformerTest {

    @SuppressWarnings("unchecked")
    @Test
    void appliesReplacerRecursivelyAndOmitsNodes() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", 7);
        data.put("secret", "keep");
        data.put("meta", new LinkedHashMap<>(Map.of("active", true)));
        data.put("roles", new ArrayList<>(List.of("admin", "user")));

        DecodeReplacer replacer = (key, value, path) -> {
            if ("secret".equals(key)) {
                return DecodeReplacer.OMIT;
            }
            if ("active".equals(key)) {
                return false;
            }
            if ("0".equals(key) && path.equals(List.of("roles", 0))) {
                return DecodeReplacer.OMIT;
            }
            return value;
        };

        Object transformed = DecodeTransformer.apply(data, replacer);

        Map<String, Object> root = (Map<String, Object>) transformed;
        assertFalse(root.containsKey("secret"));
        Map<String, Object> meta = (Map<String, Object>) root.get("meta");
        assertEquals(false, meta.get("active"));
        List<String> roles = (List<String>) root.get("roles");
        assertEquals(List.of("user"), roles);
    }

    @Test
    void rootOmitKeepsDocumentIntact() {
        Map<String, Object> data = Map.of("name", "Ada");
        DecodeReplacer replacer = (key, value, path) -> path.isEmpty()
            ? DecodeReplacer.OMIT
            : value;

        Object transformed = DecodeTransformer.apply(data, replacer);
        assertEquals(data, transformed);
    }
}
