package im.arun.toon4j.core;

import im.arun.toon4j.PathExpansion;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PathExpanderTest {

    @SuppressWarnings("unchecked")
    @Test
    void expandsSafeDottedKeysIntoNestedMaps() {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("user.name", "Ada");
        input.put("user.age", 32);
        input.put("meta.version", "1.0");

        Object expanded = PathExpander.expand(input, PathExpansion.SAFE);

        Map<String, Object> root = (Map<String, Object>) expanded;
        Map<String, Object> user = (Map<String, Object>) root.get("user");
        Map<String, Object> meta = (Map<String, Object>) root.get("meta");

        assertEquals("Ada", user.get("name"));
        assertEquals(32, user.get("age"));
        assertEquals("1.0", meta.get("version"));
        assertFalse(root.containsKey("user.name"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void skipsExpansionForUnsafeSegmentsOrOffMode() {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("user.name", "Ada");
        input.put("user.invalid-segment", "ignored");

        Object off = PathExpander.expand(input, PathExpansion.OFF);
        assertSame(input, off, "Expansion off should return original value");

        Object expanded = PathExpander.expand(input, PathExpansion.SAFE);
        Map<String, Object> root = (Map<String, Object>) expanded;
        assertEquals("ignored", root.get("user.invalid-segment"));
        assertFalse(root.containsKey("user.invalid"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void expandsMapsInsideIterablesRecursively() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("a.b.c", 10);

        List<Object> list = new ArrayList<>();
        list.add(map);

        Object expanded = PathExpander.expand(list, PathExpansion.SAFE);

        List<Object> result = (List<Object>) expanded;
        Map<String, Object> expandedMap = (Map<String, Object>) result.get(0);
        Map<String, Object> a = (Map<String, Object>) expandedMap.get("a");
        Map<String, Object> b = (Map<String, Object>) a.get("b");
        assertEquals(10, b.get("c"));
    }
}
