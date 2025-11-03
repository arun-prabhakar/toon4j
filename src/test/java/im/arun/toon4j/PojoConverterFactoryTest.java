package im.arun.toon4j;

import org.junit.jupiter.api.Test;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for PojoConverterFactory class.
 * Tests singleton pattern, converter interface, and delegation to ReflectionConverter.
 */
class PojoConverterFactoryTest {

    static class TestPojo {
        private String name;
        public TestPojo(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
    }

    @Test
    void testGetInstanceNotNull() {
        PojoConverterFactory.PojoConverter converter = PojoConverterFactory.getInstance();
        assertNotNull(converter);
    }

    @Test
    void testGetInstanceSingleton() {
        PojoConverterFactory.PojoConverter converter1 = PojoConverterFactory.getInstance();
        PojoConverterFactory.PojoConverter converter2 = PojoConverterFactory.getInstance();

        assertSame(converter1, converter2, "getInstance should return the same instance");
    }

    @Test
    void testConverterGetName() {
        PojoConverterFactory.PojoConverter converter = PojoConverterFactory.getInstance();
        String name = converter.getName();

        assertNotNull(name);
        assertEquals("Optimized Reflection", name);
    }

    @Test
    void testConverterIsPojo() {
        PojoConverterFactory.PojoConverter converter = PojoConverterFactory.getInstance();

        // Test with actual POJO
        assertTrue(converter.isPojo(new TestPojo("test")));

        // Test with non-POJO types
        assertFalse(converter.isPojo("string"));
        assertFalse(converter.isPojo(123));
        assertFalse(converter.isPojo(null));
        assertFalse(converter.isPojo(Map.of("key", "value")));
    }

    @Test
    void testConverterToMap() {
        PojoConverterFactory.PojoConverter converter = PojoConverterFactory.getInstance();
        TestPojo pojo = new TestPojo("Ada");

        Map<String, Object> map = converter.toMap(pojo);

        assertNotNull(map);
        assertEquals("Ada", map.get("name"));
    }

    @Test
    void testConverterToMapNull() {
        PojoConverterFactory.PojoConverter converter = PojoConverterFactory.getInstance();
        assertNull(converter.toMap(null));
    }

    @Test
    void testConverterDelegatesToReflectionConverter() {
        PojoConverterFactory.PojoConverter converter = PojoConverterFactory.getInstance();
        TestPojo pojo = new TestPojo("test");

        // Should produce same result as ReflectionConverter
        Map<String, Object> converterResult = converter.toMap(pojo);
        Map<String, Object> directResult = ReflectionConverter.toMap(pojo);

        assertEquals(directResult, converterResult);
    }

    @Test
    void testReflectionPojoConverterType() {
        PojoConverterFactory.PojoConverter converter = PojoConverterFactory.getInstance();
        assertTrue(converter instanceof PojoConverterFactory.ReflectionPojoConverter);
    }

    @Test
    void testConverterWithComplexPojo() {
        PojoConverterFactory.PojoConverter converter = PojoConverterFactory.getInstance();

        class ComplexPojo {
            private String field1 = "value1";
            private int field2 = 42;
            public String getField1() { return field1; }
            public int getField2() { return field2; }
        }

        ComplexPojo pojo = new ComplexPojo();
        Map<String, Object> map = converter.toMap(pojo);

        assertNotNull(map);
        assertEquals("value1", map.get("field1"));
        assertEquals(42, map.get("field2"));
    }

    @Test
    void testConverterIsPojoWithJavaTypes() {
        PojoConverterFactory.PojoConverter converter = PojoConverterFactory.getInstance();

        // Java time types should not be considered POJOs
        assertFalse(converter.isPojo(java.time.Instant.now()));
        assertFalse(converter.isPojo(new java.util.Date()));

        // Java standard library types
        assertFalse(converter.isPojo(new java.io.File("test")));
    }

    record TestRecord(String name, int value) {}

    @Test
    void testConverterWithRecord() {
        PojoConverterFactory.PojoConverter converter = PojoConverterFactory.getInstance();
        TestRecord record = new TestRecord("test", 123);

        assertTrue(converter.isPojo(record));

        Map<String, Object> map = converter.toMap(record);
        assertNotNull(map);
        assertEquals("test", map.get("name"));
        assertEquals(123, map.get("value"));
    }

    @Test
    void testFactoryCannotBeInstantiated() {
        // Factory class should have private constructor
        // We can't directly test this, but we verify it's final and methods are static
        assertTrue(java.lang.reflect.Modifier.isFinal(PojoConverterFactory.class.getModifiers()));
    }

    @Test
    void testMultipleConversions() {
        PojoConverterFactory.PojoConverter converter = PojoConverterFactory.getInstance();

        TestPojo pojo1 = new TestPojo("first");
        TestPojo pojo2 = new TestPojo("second");
        TestPojo pojo3 = new TestPojo("third");

        Map<String, Object> map1 = converter.toMap(pojo1);
        Map<String, Object> map2 = converter.toMap(pojo2);
        Map<String, Object> map3 = converter.toMap(pojo3);

        assertEquals("first", map1.get("name"));
        assertEquals("second", map2.get("name"));
        assertEquals("third", map3.get("name"));
    }
}
