package im.arun.toon4j;

import java.util.*;

/**
 * Example demonstrating TOON encoding with various data structures.
 */
public class Example {
    public static void main(String[] args) {
        System.out.println("=== TOON4J Examples ===\n");

        // Example 1: Simple object
        System.out.println("1. Simple Object:");
        Map<String, Object> simple = new LinkedHashMap<>();
        simple.put("id", 123);
        simple.put("name", "Ada");
        simple.put("active", true);
        System.out.println(Toon.encode(simple));
        System.out.println();

        // Example 2: Nested object
        System.out.println("2. Nested Object:");
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("id", 123);
        user.put("name", "Ada");
        user.put("tags", List.of("reading", "gaming"));
        user.put("active", true);

        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("user", user);
        System.out.println(Toon.encode(nested));
        System.out.println();

        // Example 3: Array of objects (tabular)
        System.out.println("3. Array of Objects (Tabular):");
        List<Map<String, Object>> items = List.of(
            createMap("sku", "A1", "qty", 2, "price", 9.99),
            createMap("sku", "B2", "qty", 1, "price", 14.5)
        );
        Map<String, Object> tabular = new LinkedHashMap<>();
        tabular.put("items", items);
        System.out.println(Toon.encode(tabular));
        System.out.println();

        // Example 4: Mixed array
        System.out.println("4. Mixed Array:");
        List<Object> mixed = List.of(
            1,
            createMap("id", 2, "name", "Item"),
            "text"
        );
        Map<String, Object> mixedData = new LinkedHashMap<>();
        mixedData.put("items", mixed);
        System.out.println(Toon.encode(mixedData));
        System.out.println();

        // Example 5: Tab delimiter
        System.out.println("5. Tab Delimiter:");
        EncodeOptions tabOptions = EncodeOptions.builder()
            .delimiter(Delimiter.TAB)
            .build();
        System.out.println(Toon.encode(tabular, tabOptions));
        System.out.println();

        // Example 6: Length marker
        System.out.println("6. Length Marker:");
        EncodeOptions markerOptions = EncodeOptions.builder()
            .lengthMarker(true)
            .build();
        Map<String, Object> withMarker = new LinkedHashMap<>();
        withMarker.put("tags", List.of("reading", "gaming", "coding"));
        System.out.println(Toon.encode(withMarker, markerOptions));
        System.out.println();

        // Example 7: Complex nested structure
        System.out.println("7. Complex Structure:");
        Map<String, Object> order = new LinkedHashMap<>();
        order.put("orderId", "ORD-123");
        order.put("customer", createMap("id", 456, "name", "Bob", "email", "bob@example.com"));
        order.put("items", List.of(
            createMap("sku", "A1", "qty", 2, "price", 9.99),
            createMap("sku", "B2", "qty", 1, "price", 14.5)
        ));
        order.put("status", "shipped");
        order.put("tags", List.of("urgent", "express"));
        System.out.println(Toon.encode(order));
        System.out.println();

        // Example 8: Token comparison
        System.out.println("8. Token Efficiency (vs JSON):");
        String toonOutput = Toon.encode(order);
        String jsonOutput = toSimpleJson(order);
        System.out.println("TOON output (" + toonOutput.length() + " chars):");
        System.out.println(toonOutput);
        System.out.println("\nJSON output (" + jsonOutput.length() + " chars):");
        System.out.println(jsonOutput);
        System.out.println("\nReduction: " +
            String.format("%.1f%%", 100.0 * (jsonOutput.length() - toonOutput.length()) / jsonOutput.length()));
    }

    private static Map<String, Object> createMap(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    // Simple JSON-like string representation for comparison
    private static String toSimpleJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) return "\"" + obj + "\"";
        if (obj instanceof Number || obj instanceof Boolean) return obj.toString();
        if (obj instanceof Map) {
            StringBuilder sb = new StringBuilder("{");
            Map<?, ?> map = (Map<?, ?>) obj;
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(", ");
                sb.append("\"").append(entry.getKey()).append("\": ")
                  .append(toSimpleJson(entry.getValue()));
                first = false;
            }
            return sb.append("}").toString();
        }
        if (obj instanceof List) {
            StringBuilder sb = new StringBuilder("[");
            List<?> list = (List<?>) obj;
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(toSimpleJson(list.get(i)));
            }
            return sb.append("]").toString();
        }
        return obj.toString();
    }
}
