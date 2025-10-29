package im.arun.toon4j;

import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class NormalizeTest {

    @Test
    void testNormalizeNull() {
        assertNull(Normalize.normalizeValue(null));
    }

    @Test
    void testNormalizePrimitives() {
        assertEquals("hello", Normalize.normalizeValue("hello"));
        assertEquals(true, Normalize.normalizeValue(true));
        assertEquals(false, Normalize.normalizeValue(false));
    }

    @Test
    void testNormalizeNumbers() {
        assertEquals(42, Normalize.normalizeValue(42));
        assertEquals(3.14, Normalize.normalizeValue(3.14));
        assertEquals(-10, Normalize.normalizeValue(-10));
    }

    @Test
    void testNormalizeNegativeZero() {
        assertEquals(0, Normalize.normalizeValue(-0.0));
    }

    @Test
    void testNormalizeNaNAndInfinity() {
        assertNull(Normalize.normalizeValue(Double.NaN));
        assertNull(Normalize.normalizeValue(Double.POSITIVE_INFINITY));
        assertNull(Normalize.normalizeValue(Double.NEGATIVE_INFINITY));
    }

    @Test
    void testNormalizeBigInteger() {
        BigInteger small = BigInteger.valueOf(123);
        assertEquals(123L, Normalize.normalizeValue(small));

        BigInteger large = new BigInteger("999999999999999999999999999");
        assertEquals("999999999999999999999999999", Normalize.normalizeValue(large));
    }

    @Test
    void testNormalizeDate() {
        Date date = new Date(1700000000000L);
        Object result = Normalize.normalizeValue(date);
        assertTrue(result instanceof String);
        assertTrue(((String) result).contains("2023-11-14"));
    }

    @Test
    void testNormalizeCollection() {
        List<Object> list = List.of(1, 2, 3);
        Object result = Normalize.normalizeValue(list);
        assertTrue(result instanceof List);
        assertEquals(3, ((List<?>) result).size());
    }

    @Test
    void testNormalizeMap() {
        Map<String, Object> map = Map.of("id", 123, "name", "Ada");
        Object result = Normalize.normalizeValue(map);
        assertTrue(result instanceof Map);
        assertEquals(2, ((Map<?, ?>) result).size());
    }

    @Test
    void testIsJsonPrimitive() {
        assertTrue(Normalize.isJsonPrimitive(null));
        assertTrue(Normalize.isJsonPrimitive("hello"));
        assertTrue(Normalize.isJsonPrimitive(42));
        assertTrue(Normalize.isJsonPrimitive(true));
        assertFalse(Normalize.isJsonPrimitive(List.of()));
        assertFalse(Normalize.isJsonPrimitive(Map.of()));
    }

    @Test
    void testIsJsonArray() {
        assertTrue(Normalize.isJsonArray(List.of()));
        assertTrue(Normalize.isJsonArray(List.of(1, 2, 3)));
        assertFalse(Normalize.isJsonArray("hello"));
        assertFalse(Normalize.isJsonArray(Map.of()));
    }

    @Test
    void testIsJsonObject() {
        assertTrue(Normalize.isJsonObject(Map.of()));
        assertTrue(Normalize.isJsonObject(Map.of("id", 123)));
        assertFalse(Normalize.isJsonObject("hello"));
        assertFalse(Normalize.isJsonObject(List.of()));
        assertFalse(Normalize.isJsonObject(null));
    }

    @Test
    void testIsArrayOfPrimitives() {
        assertTrue(Normalize.isArrayOfPrimitives(List.of(1, 2, 3)));
        assertTrue(Normalize.isArrayOfPrimitives(List.of("a", "b", "c")));
        assertTrue(Normalize.isArrayOfPrimitives(List.of(true, false)));
        assertFalse(Normalize.isArrayOfPrimitives(List.of(1, List.of())));
        assertFalse(Normalize.isArrayOfPrimitives(List.of(Map.of())));
    }

    @Test
    void testIsArrayOfArrays() {
        assertTrue(Normalize.isArrayOfArrays(List.of(List.of(1), List.of(2))));
        assertFalse(Normalize.isArrayOfArrays(List.of(1, 2, 3)));
        assertFalse(Normalize.isArrayOfArrays(List.of(Map.of())));
    }

    @Test
    void testIsArrayOfObjects() {
        assertTrue(Normalize.isArrayOfObjects(List.of(Map.of("id", 1), Map.of("id", 2))));
        assertFalse(Normalize.isArrayOfObjects(List.of(1, 2, 3)));
        assertFalse(Normalize.isArrayOfObjects(List.of(List.of())));
    }
}
