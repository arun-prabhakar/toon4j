package im.arun.toon4j;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

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
}
