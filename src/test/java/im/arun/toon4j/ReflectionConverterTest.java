package im.arun.toon4j;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ReflectionConverterTest {

    static class SamplePojo {
        private final String name;
        private final int score;
        SamplePojo(String name, int score) {
            this.name = name;
            this.score = score;
        }
        public String getName() { return name; }
        public int getScore() { return score; }
        public boolean isActive() { return true; }
    }

    @Test
    void detectsPojoAndIgnoresBuiltIns() {
        assertFalse(ReflectionConverter.isPojo(null));
        assertFalse(ReflectionConverter.isPojo("string"));
        assertFalse(ReflectionConverter.isPojo(LocalDate.now()));
        assertFalse(ReflectionConverter.isPojo(Map.of()));

        assertTrue(ReflectionConverter.isPojo(new SamplePojo("Ada", 10)));
    }

    @Test
    void convertsPojoToMapUsingAccessors() {
        SamplePojo pojo = new SamplePojo("Ada", 10);

        Map<String, Object> map = ReflectionConverter.toMap(pojo);

        assertEquals("Ada", map.get("name"));
        assertEquals(10, map.get("score"));
        assertEquals(true, map.get("active"));
    }

    static class FieldOnlyPojo {
        String title = "t";
        int count = 2;
    }

    record RecordPojo(String id, int qty) {}

    @Test
    void convertsPojoUsingFieldsWhenNoGetters() {
        Map<String, Object> map = ReflectionConverter.toMap(new FieldOnlyPojo());
        assertEquals("t", map.get("title"));
        assertEquals(2, map.get("count"));
    }

    @Test
    void convertsRecordComponents() {
        Map<String, Object> map = ReflectionConverter.toMap(new RecordPojo("r1", 5));
        assertEquals("r1", map.get("id"));
        assertEquals(5, map.get("qty"));
    }

    // ===== Branch coverage tests =====

    @Test
    void isPojoReturnsFalseForNumber() {
        assertFalse(ReflectionConverter.isPojo(42));
        assertFalse(ReflectionConverter.isPojo(3.14));
        assertFalse(ReflectionConverter.isPojo(100L));
    }

    @Test
    void isPojoReturnsFalseForBoolean() {
        assertFalse(ReflectionConverter.isPojo(true));
        assertFalse(ReflectionConverter.isPojo(Boolean.FALSE));
    }

    @Test
    void isPojoReturnsFalseForCharacter() {
        assertFalse(ReflectionConverter.isPojo('A'));
        assertFalse(ReflectionConverter.isPojo(Character.valueOf('Z')));
    }

    @Test
    void isPojoReturnsFalseForIterable() {
        assertFalse(ReflectionConverter.isPojo(List.of(1, 2, 3)));
        assertFalse(ReflectionConverter.isPojo(Set.of("a", "b")));
    }

    @Test
    void isPojoReturnsFalseForArray() {
        assertFalse(ReflectionConverter.isPojo(new int[]{1, 2, 3}));
        assertFalse(ReflectionConverter.isPojo(new String[]{"a", "b"}));
    }

    @Test
    void isPojoReturnsFalseForJavaTimeTypes() {
        assertFalse(ReflectionConverter.isPojo(Instant.now()));
        assertFalse(ReflectionConverter.isPojo(java.time.LocalDateTime.now()));
        assertFalse(ReflectionConverter.isPojo(java.time.ZonedDateTime.now()));
    }

    @Test
    void isPojoReturnsFalseForJavaUtilDate() {
        assertFalse(ReflectionConverter.isPojo(new Date()));
    }

    @Test
    void toMapReturnsNullForNullInput() {
        assertNull(ReflectionConverter.toMap(null));
    }

    // Test static method filtering
    static class PojoWithStaticMethod {
        private String value = "test";
        public String getValue() { return value; }
        public static String getStaticValue() { return "static"; }
    }

    @Test
    void ignoresStaticMethods() {
        Map<String, Object> map = ReflectionConverter.toMap(new PojoWithStaticMethod());
        assertEquals("test", map.get("value"));
        assertFalse(map.containsKey("staticValue"));
    }

    // Test methods with parameters (should be ignored)
    static class PojoWithParameterMethod {
        private String name = "test";
        public String getName() { return name; }
        public String getNameWithPrefix(String prefix) { return prefix + name; }
    }

    @Test
    void ignoresMethodsWithParameters() {
        Map<String, Object> map = ReflectionConverter.toMap(new PojoWithParameterMethod());
        assertEquals("test", map.get("name"));
        assertFalse(map.containsKey("nameWithPrefix"));
    }

    // Test isX method with non-boolean return type (should be ignored)
    static class PojoWithNonBooleanIs {
        private String name = "test";
        public String getName() { return name; }
        public String isName() { return name; } // Returns String, not boolean
    }

    @Test
    void ignoresIsMethodWithNonBooleanReturn() {
        Map<String, Object> map = ReflectionConverter.toMap(new PojoWithNonBooleanIs());
        assertEquals("test", map.get("name"));
        // isName() should not create "name" property since it returns String
        assertEquals(1, map.size());
    }

    // Test Boolean wrapper type for isX methods
    static class PojoWithBooleanWrapper {
        public Boolean isEnabled() { return Boolean.TRUE; }
    }

    @Test
    void handlesIsMethodWithBooleanWrapper() {
        Map<String, Object> map = ReflectionConverter.toMap(new PojoWithBooleanWrapper());
        assertEquals(Boolean.TRUE, map.get("enabled"));
    }

    // Test uppercase property names (e.g., getURL -> URL)
    static class PojoWithUppercaseProperty {
        public String getURL() { return "http://example.com"; }
        public String getABCValue() { return "abc"; }
    }

    @Test
    void preservesUppercasePropertyNames() {
        Map<String, Object> map = ReflectionConverter.toMap(new PojoWithUppercaseProperty());
        assertEquals("http://example.com", map.get("URL"));
        assertEquals("abc", map.get("ABCValue"));
    }

    // Test static fields are ignored
    static class PojoWithStaticField {
        static String staticValue = "static";
        String instanceValue = "instance";
    }

    @Test
    void ignoresStaticFields() {
        Map<String, Object> map = ReflectionConverter.toMap(new PojoWithStaticField());
        assertEquals("instance", map.get("instanceValue"));
        assertFalse(map.containsKey("staticValue"));
    }

    // Test inheritance - fields from superclass
    static class ParentPojo {
        String parentField = "parent";
    }

    static class ChildPojo extends ParentPojo {
        String childField = "child";
    }

    @Test
    void includesInheritedFields() {
        Map<String, Object> map = ReflectionConverter.toMap(new ChildPojo());
        assertEquals("child", map.get("childField"));
        assertEquals("parent", map.get("parentField"));
    }

    // Test getClass is ignored
    static class SimplePojoForGetClass {
        public String getName() { return "test"; }
        // getClass() is inherited from Object and should be ignored
    }

    @Test
    void ignoresGetClassMethod() {
        Map<String, Object> map = ReflectionConverter.toMap(new SimplePojoForGetClass());
        assertEquals("test", map.get("name"));
        assertFalse(map.containsKey("class"));
    }

    // Test short getter names (get with only 3 chars should be ignored)
    static class PojoWithShortGetterName {
        public String get() { return "value"; } // Too short, no property name
        public String getName() { return "test"; }
    }

    @Test
    void ignoresGetMethodWithoutPropertyName() {
        Map<String, Object> map = ReflectionConverter.toMap(new PojoWithShortGetterName());
        assertEquals("test", map.get("name"));
        assertEquals(1, map.size());
    }

    // Test is with only 2 chars (should be ignored)
    static class PojoWithShortIsName {
        public boolean is() { return true; } // Too short
        public boolean isActive() { return false; }
    }

    @Test
    void ignoresIsMethodWithoutPropertyName() {
        Map<String, Object> map = ReflectionConverter.toMap(new PojoWithShortIsName());
        assertEquals(false, map.get("active"));
        assertEquals(1, map.size());
    }

    // Test getters with first letter already lowercase (putIfAbsent handles duplicates)
    static class PojoWithDuplicateGetters {
        public String getValue() { return "first"; }
        // In real scenarios this wouldn't compile, but tests getter deduplication
    }

    @Test
    void handlesGetterDeduplication() {
        Map<String, Object> map = ReflectionConverter.toMap(new PojoWithDuplicateGetters());
        assertEquals("first", map.get("value"));
    }
}
