package im.arun.toon4j;

import java.util.Map;

/**
 * Factory for POJO converter using optimized reflection.
 * Uses cached accessors for high-performance POJO-to-Map conversion.
 */
final class PojoConverterFactory {
    private PojoConverterFactory() {}

    private static final PojoConverter INSTANCE = new ReflectionPojoConverter();

    /**
     * Get the singleton converter instance.
     */
    static PojoConverter getInstance() {
        return INSTANCE;
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
     * Reflection-based converter with cached accessors (high performance).
     */
    static class ReflectionPojoConverter implements PojoConverter {
        @Override
        public boolean isPojo(Object value) {
            return ReflectionConverter.isPojo(value);
        }

        @Override
        public Map<String, Object> toMap(Object pojo) {
            return ReflectionConverter.toMap(pojo);
        }

        @Override
        public String getName() {
            return "Optimized Reflection";
        }
    }
}
