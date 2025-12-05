package im.arun.toon4j.core;

import im.arun.toon4j.EncodeReplacer;
import im.arun.toon4j.Normalize;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReplacerTest {

    @SuppressWarnings("unchecked")
    @Test
    void removesEntriesAndTransformsValues() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("password", "secret");
        data.put("profile", Map.of("age", 30, "active", true));
        data.put("tags", new ArrayList<>(List.of("alpha", "beta")));

        EncodeReplacer replacer = (key, value, path) -> {
            if ("password".equals(key)) {
                return EncodeReplacer.OMIT;
            }
            if ("age".equals(key)) {
                assertEquals(List.of("profile", "age"), path);
                return 31;
            }
            if ("0".equals(key) && path.equals(List.of("tags", 0))) {
                return "first";
            }
            return value;
        };

        Object result = Replacer.apply(Normalize.normalizeValue(data), replacer);

        Map<String, Object> root = (Map<String, Object>) result;
        assertFalse(root.containsKey("password"));
        Map<String, Object> profile = (Map<String, Object>) root.get("profile");
        assertEquals(31, profile.get("age"));
        List<String> tags = (List<String>) root.get("tags");
        assertEquals(List.of("first", "beta"), tags);
    }

    @Test
    void rootOmitDoesNotRemoveDocument() {
        Map<String, Object> data = Map.of("name", "Ada");
        EncodeReplacer rootOnly = (key, value, path) -> path.isEmpty()
            ? EncodeReplacer.OMIT
            : value;

        Object result = Replacer.apply(Normalize.normalizeValue(data), rootOnly);
        assertEquals(data, result);
    }
}
