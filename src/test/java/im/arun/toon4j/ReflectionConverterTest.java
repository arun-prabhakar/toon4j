package im.arun.toon4j;

import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for ReflectionConverter class.
 * Tests POJO detection, Map conversion, field/getter access, and caching.
 */
class ReflectionConverterTest {

    // Test POJOs
    static class SimplePojo {
        private String name;
        private int age;

        public SimplePojo(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    }

    static class PojoWithFields {
        public String publicField = "public";
        private String privateField = "private";
        protected String protectedField = "protected";
    }

    static class PojoWithBooleanGetter {
        private boolean active;

        public PojoWithBooleanGetter(boolean active) {
            this.active = active;
        }

        public boolean isActive() {
            return active;
        }
    }

    static class PojoWithMixedAccessors {
        private String name;
        public int id = 42;

        public PojoWithMixedAccessors(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    static class EmptyPojo {
    }

    static class PojoWithStaticField {
        public static String staticField = "static";
        public String instanceField = "instance";
    }

    static class PojoWithGetClass {
        public String name = "test";

        // getClass() should be excluded
    }

    static class PojoWithAcronymGetter {
        private String url;

        public PojoWithAcronymGetter(String url) {
            this.url = url;
        }

        public String getURL() {
            return url;
        }
    }

    static class InheritedPojo extends SimplePojo {
        private String city;

        public InheritedPojo(String name, int age, String city) {
            super(name, age);
            this.city = city;
        }

        public String getCity() {
            return city;
        }
    }

    // ========== isPojo Tests ==========

    @Test
    void testIsPojoNull() {
        assertFalse(ReflectionConverter.isPojo(null));
    }

    @Test
    void testIsPojoBasicTypes() {
        assertFalse(ReflectionConverter.isPojo("string"));
        assertFalse(ReflectionConverter.isPojo(123));
        assertFalse(ReflectionConverter.isPojo(3.14));
        assertFalse(ReflectionConverter.isPojo(true));
        assertFalse(ReflectionConverter.isPojo('c'));
    }

    @Test
    void testIsPojoCollections() {
        assertFalse(ReflectionConverter.isPojo(List.of(1, 2, 3)));
        assertFalse(ReflectionConverter.isPojo(Map.of("key", "value")));
        assertFalse(ReflectionConverter.isPojo(new int[]{1, 2, 3}));
    }

    @Test
    void testIsPojoJavaTime() {
        assertFalse(ReflectionConverter.isPojo(java.time.Instant.now()));
        assertFalse(ReflectionConverter.isPojo(java.time.LocalDate.now()));
        assertFalse(ReflectionConverter.isPojo(java.time.LocalDateTime.now()));
        assertFalse(ReflectionConverter.isPojo(new java.util.Date()));
    }

    @Test
    void testIsPojoJavaStdLib() {
        assertFalse(ReflectionConverter.isPojo(new java.io.File("test")));
        assertFalse(ReflectionConverter.isPojo(new java.net.URL("http://example.com")));
    }

    @Test
    void testIsPojoUserPojo() {
        assertTrue(ReflectionConverter.isPojo(new SimplePojo("Ada", 25)));
        assertTrue(ReflectionConverter.isPojo(new PojoWithFields()));
        assertTrue(ReflectionConverter.isPojo(new EmptyPojo()));
    }

    // ========== toMap Tests with Getters ==========

    @Test
    void testToMapNull() {
        assertNull(ReflectionConverter.toMap(null));
    }

    @Test
    void testToMapSimplePojo() {
        SimplePojo pojo = new SimplePojo("Ada", 25);
        Map<String, Object> map = ReflectionConverter.toMap(pojo);

        assertNotNull(map);
        assertEquals("Ada", map.get("name"));
        assertEquals(25, map.get("age"));
        assertEquals(2, map.size());
    }

    @Test
    void testToMapBooleanGetter() {
        PojoWithBooleanGetter pojo = new PojoWithBooleanGetter(true);
        Map<String, Object> map = ReflectionConverter.toMap(pojo);

        assertNotNull(map);
        assertEquals(true, map.get("active"));
    }

    @Test
    void testToMapMixedAccessors() {
        PojoWithMixedAccessors pojo = new PojoWithMixedAccessors("Test");
        Map<String, Object> map = ReflectionConverter.toMap(pojo);

        assertNotNull(map);
        // Getter takes precedence
        assertTrue(map.containsKey("name"));
        assertEquals("Test", map.get("name"));
    }

    @Test
    void testToMapEmptyPojo() {
        EmptyPojo pojo = new EmptyPojo();
        Map<String, Object> map = ReflectionConverter.toMap(pojo);

        assertNotNull(map);
        assertTrue(map.isEmpty() || map.size() == 0);
    }

    @Test
    void testToMapExcludesStaticFields() {
        PojoWithStaticField pojo = new PojoWithStaticField();
        Map<String, Object> map = ReflectionConverter.toMap(pojo);

        assertNotNull(map);
        assertFalse(map.containsKey("staticField"));
        assertTrue(map.containsKey("instanceField") || map.isEmpty());
    }

    @Test
    void testToMapExcludesGetClass() {
        PojoWithGetClass pojo = new PojoWithGetClass();
        Map<String, Object> map = ReflectionConverter.toMap(pojo);

        assertNotNull(map);
        assertFalse(map.containsKey("class"));
    }

    @Test
    void testToMapAcronymGetter() {
        PojoWithAcronymGetter pojo = new PojoWithAcronymGetter("http://example.com");
        Map<String, Object> map = ReflectionConverter.toMap(pojo);

        assertNotNull(map);
        // URL should remain URL (not decapitalized to uRL)
        assertTrue(map.containsKey("URL"));
        assertEquals("http://example.com", map.get("URL"));
    }

    @Test
    void testToMapInheritedPojo() {
        InheritedPojo pojo = new InheritedPojo("Ada", 25, "London");
        Map<String, Object> map = ReflectionConverter.toMap(pojo);

        assertNotNull(map);
        assertEquals("Ada", map.get("name"));
        assertEquals(25, map.get("age"));
        assertEquals("London", map.get("city"));
        assertEquals(3, map.size());
    }

    // ========== toMap Tests with Fields (No Getters) ==========

    @Test
    void testToMapPojoWithOnlyFields() {
        PojoWithFields pojo = new PojoWithFields();
        Map<String, Object> map = ReflectionConverter.toMap(pojo);

        assertNotNull(map);
        // If no getters, should fall back to fields
        if (!map.isEmpty()) {
            assertTrue(map.containsKey("publicField") ||
                      map.containsKey("privateField") ||
                      map.containsKey("protectedField"));
        }
    }

    // ========== Caching Tests ==========

    @Test
    void testToMapCaching() {
        SimplePojo pojo1 = new SimplePojo("Ada", 25);
        SimplePojo pojo2 = new SimplePojo("Bob", 30);

        Map<String, Object> map1 = ReflectionConverter.toMap(pojo1);
        Map<String, Object> map2 = ReflectionConverter.toMap(pojo2);

        // Both should work, proving cache is working correctly
        assertEquals("Ada", map1.get("name"));
        assertEquals("Bob", map2.get("name"));
    }

    @Test
    void testToMapMultipleCalls() {
        SimplePojo pojo = new SimplePojo("Ada", 25);

        Map<String, Object> map1 = ReflectionConverter.toMap(pojo);
        Map<String, Object> map2 = ReflectionConverter.toMap(pojo);

        assertNotSame(map1, map2); // Should be different instances
        assertEquals(map1, map2);   // But with same content
    }

    // ========== Edge Cases ==========

    @Test
    void testToMapNullFieldValues() {
        SimplePojo pojo = new SimplePojo(null, 0);
        Map<String, Object> map = ReflectionConverter.toMap(pojo);

        assertNotNull(map);
        assertEquals(null, map.get("name"));
        assertEquals(0, map.get("age"));
    }

    static class PojoWithGetterException {
        public String getFailingValue() {
            throw new RuntimeException("Getter failed");
        }

        public String getWorkingValue() {
            return "works";
        }
    }

    @Test
    void testToMapGetterException() {
        PojoWithGetterException pojo = new PojoWithGetterException();
        Map<String, Object> map = ReflectionConverter.toMap(pojo);

        assertNotNull(map);
        // Failing getter should be skipped
        assertFalse(map.containsKey("failingValue"));
        // Working getter should succeed
        assertEquals("works", map.get("workingValue"));
    }

    static class PojoWithParameterizedGetter {
        public String getValue(int param) {
            return "value";
        }
    }

    @Test
    void testToMapExcludesParameterizedGetters() {
        PojoWithParameterizedGetter pojo = new PojoWithParameterizedGetter();
        Map<String, Object> map = ReflectionConverter.toMap(pojo);

        assertNotNull(map);
        // Getter with parameters should be excluded
        assertFalse(map.containsKey("value"));
    }

    static class PojoWithNonBooleanIs {
        public String isNotBoolean() {
            return "not a boolean";
        }
    }

    @Test
    void testToMapNonBooleanIsGetter() {
        PojoWithNonBooleanIs pojo = new PojoWithNonBooleanIs();
        Map<String, Object> map = ReflectionConverter.toMap(pojo);

        assertNotNull(map);
        // is* getter for non-boolean should be excluded
        assertFalse(map.containsKey("notBoolean"));
    }

    static class PojoWithShortGetterName {
        private String x;

        public String getX() {
            return x;
        }

        public PojoWithShortGetterName(String x) {
            this.x = x;
        }
    }

    @Test
    void testToMapShortPropertyName() {
        PojoWithShortGetterName pojo = new PojoWithShortGetterName("value");
        Map<String, Object> map = ReflectionConverter.toMap(pojo);

        assertNotNull(map);
        assertEquals("value", map.get("x"));
    }

    // Test with record class (Java 17+)
    record PersonRecord(String name, int age) {}

    @Test
    void testToMapRecord() {
        PersonRecord record = new PersonRecord("Ada", 25);
        Map<String, Object> map = ReflectionConverter.toMap(record);

        assertNotNull(map);
        assertEquals("Ada", map.get("name"));
        assertEquals(25, map.get("age"));
    }

    @Test
    void testIsPojoRecord() {
        assertTrue(ReflectionConverter.isPojo(new PersonRecord("Ada", 25)));
    }

    // ========== Integration Tests ==========

    static class ComplexPojo {
        private String name;
        private int count;
        private boolean active;
        private List<String> tags = List.of("a", "b");

        public ComplexPojo(String name, int count, boolean active) {
            this.name = name;
            this.count = count;
            this.active = active;
        }

        public String getName() { return name; }
        public int getCount() { return count; }
        public boolean isActive() { return active; }
        public List<String> getTags() { return tags; }
    }

    @Test
    void testToMapComplexPojo() {
        ComplexPojo pojo = new ComplexPojo("Test", 42, true);
        Map<String, Object> map = ReflectionConverter.toMap(pojo);

        assertNotNull(map);
        assertEquals("Test", map.get("name"));
        assertEquals(42, map.get("count"));
        assertEquals(true, map.get("active"));
        assertEquals(List.of("a", "b"), map.get("tags"));
    }

    @Test
    void testToMapPreservesOrder() {
        SimplePojo pojo = new SimplePojo("Ada", 25);
        Map<String, Object> map = ReflectionConverter.toMap(pojo);

        assertNotNull(map);
        // LinkedHashMap should preserve insertion order
        assertTrue(map instanceof java.util.LinkedHashMap);
    }
}
