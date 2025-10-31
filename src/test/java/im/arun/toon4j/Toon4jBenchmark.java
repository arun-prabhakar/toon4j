package im.arun.toon4j;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmark for Toon4j encoding performance.
 *
 * Run with: mvn clean test-compile exec:java -Dexec.mainClass="org.openjdk.jmh.Main" -Dexec.classpathScope=test
 * Or: java -jar target/benchmarks.jar
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(1)
public class Toon4jBenchmark {

    // Test data
    private Map<String, Object> simpleObject;
    private List<Map<String, Object>> smallArray;
    private List<Map<String, Object>> largeArray;
    private Map<String, Object> nestedObject;
    private TestPojo testPojo;
    private List<TestPojo> pojoList;

    @Setup
    public void setup() {
        // Simple object
        simpleObject = new LinkedHashMap<>();
        simpleObject.put("id", 123);
        simpleObject.put("name", "John Doe");
        simpleObject.put("active", true);
        simpleObject.put("score", 95.5);

        // Small array (10 items)
        smallArray = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", i);
            item.put("name", "Item " + i);
            item.put("value", i * 10);
            smallArray.add(item);
        }

        // Large array (1000 items)
        largeArray = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", i);
            item.put("name", "Item " + i);
            item.put("value", i * 10);
            item.put("category", "cat" + (i % 5));
            largeArray.add(item);
        }

        // Nested object
        nestedObject = new LinkedHashMap<>();
        nestedObject.put("user", simpleObject);
        nestedObject.put("items", smallArray);
        nestedObject.put("metadata", Map.of("version", "1.0", "timestamp", System.currentTimeMillis()));

        // POJO
        testPojo = new TestPojo(123, "John Doe", "john@example.com", true);

        // POJO list
        pojoList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            pojoList.add(new TestPojo(i, "User " + i, "user" + i + "@example.com", i % 2 == 0));
        }
    }

    @Benchmark
    public void encodeSimpleObject(Blackhole bh) {
        bh.consume(Toon.encode(simpleObject));
    }

    @Benchmark
    public void encodeSmallArray(Blackhole bh) {
        bh.consume(Toon.encode(smallArray));
    }

    @Benchmark
    public void encodeLargeArray(Blackhole bh) {
        bh.consume(Toon.encode(largeArray));
    }

    @Benchmark
    public void encodeNestedObject(Blackhole bh) {
        bh.consume(Toon.encode(nestedObject));
    }

    @Benchmark
    public void encodePojo(Blackhole bh) {
        bh.consume(Toon.encode(testPojo));
    }

    @Benchmark
    public void encodePojoList(Blackhole bh) {
        bh.consume(Toon.encode(pojoList));
    }

    @Benchmark
    public void encodePrimitiveArray(Blackhole bh) {
        List<Object> primitives = Arrays.asList(1, 2, 3, 4, 5, "a", "b", "c", true, false);
        bh.consume(Toon.encode(primitives));
    }

    @Benchmark
    public void encodeStringEscaping(Blackhole bh) {
        Map<String, Object> obj = new LinkedHashMap<>();
        obj.put("text", "This is a \"quoted\" string with\nnewlines and\ttabs");
        obj.put("path", "C:\\Users\\Documents\\file.txt");
        obj.put("special", "Contains: colons, {braces}, [brackets]");
        bh.consume(Toon.encode(obj));
    }

    // Test POJO class
    public static class TestPojo {
        private final int id;
        private final String name;
        private final String email;
        private final boolean active;

        public TestPojo(int id, String name, String email, boolean active) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.active = active;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public boolean isActive() { return active; }
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
}
