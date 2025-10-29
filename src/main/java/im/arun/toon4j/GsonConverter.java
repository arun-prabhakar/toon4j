package im.arun.toon4j;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

/**
 * Helper class to convert POJOs to Maps using Gson.
 * Internal use only.
 */
final class GsonConverter {
    private GsonConverter() {}

    private static final Gson GSON = new GsonBuilder()
        .serializeNulls()  // Include null values
        .create();

    /**
     * Check if a value is a POJO that needs conversion.
     * Returns false for primitives, Maps, Lists, and other already-supported types.
     */
    static boolean isPojo(Object value) {
        if (value == null) {
            return false;
        }

        // Already supported types - no conversion needed
        if (value instanceof String ||
            value instanceof Number ||
            value instanceof Boolean ||
            value instanceof Character ||
            value instanceof Map ||
            value instanceof Iterable ||
            value.getClass().isArray()) {
            return false;
        }

        // Check for java.time and java.util temporal types - already handled
        String className = value.getClass().getName();
        if (className.startsWith("java.time.") ||
            className.startsWith("java.util.Date") ||
            className.equals("java.time.Instant")) {
            return false;
        }

        // Check for Java standard library classes that shouldn't be serialized
        if (className.startsWith("java.") ||
            className.startsWith("javax.") ||
            className.startsWith("sun.")) {
            return false;
        }

        // If we get here, it's likely a user POJO
        return true;
    }

    /**
     * Convert a POJO to a Map using Gson.
     * This allows automatic serialization of custom Java objects.
     */
    @SuppressWarnings("unchecked")
    static Map<String, Object> toMap(Object pojo) {
        if (pojo == null) {
            return null;
        }

        // Convert: POJO -> JSON string -> Map
        // This ensures proper handling of all Gson features (annotations, custom serializers, etc.)
        String json = GSON.toJson(pojo);
        return GSON.fromJson(json, Map.class);
    }

    /**
     * Get the Gson instance used for conversions.
     * Can be used for custom configuration if needed in the future.
     */
    static Gson getGson() {
        return GSON;
    }
}
