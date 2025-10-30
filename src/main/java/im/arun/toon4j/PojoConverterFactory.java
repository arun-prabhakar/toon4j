package im.arun.toon4j;

import java.util.Map;

/**
 * Factory for POJO converter using DSL-JSON.
 * DSL-JSON provides ~2x performance vs Jackson, ~4x vs Gson.
 */
final class PojoConverterFactory {
    private PojoConverterFactory() {}

    private static final PojoConverter INSTANCE = createConverter();

    /**
     * Get the singleton converter instance.
     */
    static PojoConverter getInstance() {
        return INSTANCE;
    }

    /**
     * Create DSL-JSON converter.
     */
    private static PojoConverter createConverter() {
        // Check if DSL-JSON is available
        try {
            Class.forName("com.dslplatform.json.DslJson");
            return new DslJsonPojoConverter();
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                "DSL-JSON library not found! Please add it to your dependencies:\n" +
                "<dependency>\n" +
                "  <groupId>com.dslplatform</groupId>\n" +
                "  <artifactId>dsl-json</artifactId>\n" +
                "  <version>2.0.2</version>\n" +
                "</dependency>"
            );
        }
    }

    /**
     * Common interface for POJO converters.
     */
    interface PojoConverter {
        boolean isPojo(Object value);
        Map<String, Object> toMap(Object pojo);
        String getName();
    }

    /**
     * DSL-JSON based converter (high performance).
     */
    static class DslJsonPojoConverter implements PojoConverter {
        @Override
        public boolean isPojo(Object value) {
            return DslJsonConverter.isPojo(value);
        }

        @Override
        public Map<String, Object> toMap(Object pojo) {
            return DslJsonConverter.toMap(pojo);
        }

        @Override
        public String getName() {
            return "DSL-JSON";
        }
    }
}
