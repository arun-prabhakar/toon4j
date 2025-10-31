package im.arun.toon4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Benchmarks Toon4j vs JToon using provided JSON files.
 *
 * Usage:
 * - Default: looks for 256KB.json and 1MB.json in project root (or current dir)
 * - Or: pass file paths as args: java ... BenchmarkRunner path/to/256KB.json path/to/1MB.json
 *
 * Notes:
 * - JToon is invoked via reflection so this compiles without JToon on the classpath.
 *   To include JToon at runtime, add its jar (and Jackson) to the classpath.
 */
public final class BenchmarkRunner {
    private BenchmarkRunner() {}

    public static void main(String[] args) throws Exception {
        List<Path> inputs = resolveInputFiles(args);
        if (inputs.isEmpty()) {
            System.err.println("No input JSON files found. Expected 256KB.json and 1MB.json.");
            System.exit(1);
        }

        System.out.println("================ TOON Benchmarks ================");
        System.out.println("Found files: ");
        for (Path p : inputs) System.out.println(" - " + p.toAbsolutePath());
        System.out.println("=================================================");
        System.out.println();

        boolean jtoonAvailable = isClassPresent("com.felipestanzani.jtoon.JToon");
        if (!jtoonAvailable) {
            System.out.println("JToon not found on classpath. Will benchmark Toon4j only.");
            System.out.println("To include JToon, add it (and Jackson) to the runtime classpath.");
            System.out.println();
        }

        // Benchmark parameters
        int warmupIters = 10;
        int measureIters = 50;

        for (Path input : inputs) {
            System.out.println("File: " + input.getFileName());
            String json = Files.readString(input, StandardCharsets.UTF_8);

            // Parse once to neutral Java structures
            Object parsed = SimpleJson.parse(json);

            // Ensure parsed is either Map or List (root JSON types)
            if (!(parsed instanceof Map || parsed instanceof List)) {
                System.out.println("Skipping (root is not object/array). Root= " + (parsed == null ? "null" : parsed.getClass().getSimpleName()));
                continue;
            }

            // Benchmark Toon4j
            System.out.println("- Toon4j:");
            runBenchmark("Toon4j", () -> Toon.encode(parsed), warmupIters, measureIters);

            // Benchmark JToon if present
            if (jtoonAvailable) {
                System.out.println("- JToon:");
                runBenchmark("JToon", () -> encodeWithJToon(parsed), warmupIters, measureIters);
            }

            System.out.println();
        }
    }

    // Benchmark helper
    private static void runBenchmark(String name, Runnable encodeFn, int warmupIters, int measureIters) {
        // Warmup
        for (int i = 0; i < warmupIters; i++) {
            encodeFn.run();
        }

        // Measure
        long totalNs = 0L;
        for (int i = 0; i < measureIters; i++) {
            long start = System.nanoTime();
            encodeFn.run();
            long end = System.nanoTime();
            totalNs += (end - start);
        }

        double avgMs = (totalNs / 1_000_000.0) / measureIters;
        double tps = 1000.0 / avgMs;
        System.out.printf("  iterations=%d, avg=%.3f ms, throughput=%.0f encodes/s%n", measureIters, avgMs, tps);
    }

    // Tries multiple locations to find 256KB.json and 1MB.json
    private static List<Path> resolveInputFiles(String[] args) {
        List<String> candidates = new ArrayList<>();
        if (args != null && args.length > 0) {
            for (String a : args) candidates.add(a);
        } else {
            // Common names/cases
            candidates.add("256KB.json");
            candidates.add("256kb.json");
            candidates.add("1MB.json");
            candidates.add("1mb.json");
        }

        // Try current dir and up to two parents
        List<Path> roots = List.of(Paths.get("."), Paths.get(".."), Paths.get("..", ".."));
        List<Path> found = new ArrayList<>();
        Set<String> seenNames = new HashSet<>();

        for (String name : candidates) {
            if (seenNames.contains(name.toLowerCase(Locale.ROOT))) continue;
            for (Path root : roots) {
                Path p = root.resolve(name).normalize();
                if (Files.isRegularFile(p)) {
                    found.add(p);
                    seenNames.add(name.toLowerCase(Locale.ROOT));
                    break;
                }
            }
        }

        // Ensure both sizes if possible: prefer unique 256KB and 1MB
        // Already handled by names above, so just return what we found in order
        return found;
    }

    // Reflection-based JToon invocation to avoid compile-time dependency
    private static void encodeWithJToon(Object value) {
        try {
            Class<?> jtoon = Class.forName("com.felipestanzani.jtoon.JToon");
            // Prefer encode(Object)
            var method = jtoon.getMethod("encode", Object.class);
            method.invoke(null, value);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("JToon class not found on classpath", e);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to invoke JToon.encode(Object)", e);
        }
    }

    private static boolean isClassPresent(String fqcn) {
        try {
            Class.forName(fqcn);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    // Minimal JSON parser to convert input files into Map/List/primitive structures.
    // Borrowed from toon4j internal utility, simplified and made self-contained for the benchmark.
    private static final class SimpleJson {
        static Object parse(String json) {
            return new Parser(json).parse();
        }

        private static final class Parser {
            private final String json;
            private int pos = 0;

            Parser(String json) { this.json = json; }

            Object parse() {
                skipWs();
                Object v = parseValue();
                skipWs();
                return v;
            }

            private Object parseValue() {
                skipWs();
                char c = cur();
                if (c == '{') return parseObject();
                if (c == '[') return parseArray();
                if (c == '"') return parseString();
                if (c == 't') return parseTrue();
                if (c == 'f') return parseFalse();
                if (c == 'n') return parseNull();
                if (c == '-' || Character.isDigit(c)) return parseNumber();
                throw new RuntimeException("Unexpected char: " + c + " at " + pos);
            }

            private Map<String, Object> parseObject() {
                Map<String, Object> map = new LinkedHashMap<>();
                consume('{');
                skipWs();
                if (cur() == '}') { consume('}'); return map; }
                while (true) {
                    skipWs();
                    String key = parseString();
                    skipWs();
                    consume(':');
                    skipWs();
                    Object val = parseValue();
                    map.put(key, val);
                    skipWs();
                    if (cur() == '}') { consume('}'); break; }
                    consume(',');
                }
                return map;
            }

            private List<Object> parseArray() {
                List<Object> list = new ArrayList<>();
                consume('[');
                skipWs();
                if (cur() == ']') { consume(']'); return list; }
                while (true) {
                    skipWs();
                    list.add(parseValue());
                    skipWs();
                    if (cur() == ']') { consume(']'); break; }
                    consume(',');
                }
                return list;
            }

            private String parseString() {
                consume('"');
                StringBuilder sb = new StringBuilder();
                while (cur() != '"') {
                    if (cur() == '\\') {
                        pos++;
                        char e = cur();
                        switch (e) {
                            case '"': sb.append('"'); break;
                            case '\\': sb.append('\\'); break;
                            case '/': sb.append('/'); break;
                            case 'b': sb.append('\b'); break;
                            case 'f': sb.append('\f'); break;
                            case 'n': sb.append('\n'); break;
                            case 'r': sb.append('\r'); break;
                            case 't': sb.append('\t'); break;
                            case 'u':
                                pos++;
                                String hex = json.substring(pos, pos + 4);
                                sb.append((char) Integer.parseInt(hex, 16));
                                pos += 3; // +3 because we'll ++pos below
                                break;
                            default: sb.append(e);
                        }
                    } else {
                        sb.append(cur());
                    }
                    pos++;
                }
                consume('"');
                return sb.toString();
            }

            private Number parseNumber() {
                int start = pos;
                if (cur() == '-') pos++;
                while (pos < json.length()) {
                    char c = cur();
                    if (Character.isDigit(c) || c == '.' || c == 'e' || c == 'E' || c == '+' || c == '-') {
                        pos++;
                    } else {
                        break;
                    }
                }
                String num = json.substring(start, pos);
                if (num.contains(".") || num.contains("e") || num.contains("E")) {
                    return Double.parseDouble(num);
                }
                long val = Long.parseLong(num);
                if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) return (int) val;
                return val;
            }

            private Boolean parseTrue() { consume('t'); consume('r'); consume('u'); consume('e'); return true; }
            private Boolean parseFalse() { consume('f'); consume('a'); consume('l'); consume('s'); consume('e'); return false; }
            private Object parseNull() { consume('n'); consume('u'); consume('l'); consume('l'); return null; }

            private void skipWs() {
                while (pos < json.length() && Character.isWhitespace(cur())) pos++;
            }
            private char cur() { return json.charAt(pos); }
            private void consume(char expected) {
                if (cur() != expected) throw new RuntimeException("Expected '" + expected + "' got '" + cur() + "' at " + pos);
                pos++;
            }
        }
    }
}

