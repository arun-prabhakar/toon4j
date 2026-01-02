# Changelog

All notable changes to TOON4J will be documented in this file.

## [1.2.1] - 2026-01-02

### Added
- **encodeLines** API for streaming TOON output line-by-line without buffering the full string
- **encode replacer** hook to transform or filter values during encoding (JSON.stringify-style)
- **safe key folding** with configurable `flattenDepth` to collapse single-key chains into dotted keys
- **flatten** option to force collapsing single-key wrappers (with depth guard and collision checks)
- **safe path expansion** on decode to rebuild dotted keys into nested maps for lossless round-trips
- **decodeLines** to decode pre-split TOON lines directly
- **decodeToEvents** / **decodeLinesToEvents** for event-based consumption of decoded data
- **decode replacer** hook to transform or omit values post-decode
- **cleanupThreadLocals()** API for resource cleanup in managed environments (servlet containers, thread pools)
- Package-level Javadoc documentation (`package-info.java`)

### Changed
- Standardized array headers to the `[N]` form (no legacy `[#N]` marker) while still accepting old syntax when parsing
- Improved exception handling documentation in reflection code
- Converted `ParsedLine` to a Java record for cleaner code
- Pre-compiled regex patterns in `PathExpander` for better performance
- Moved benchmark dependencies (Jackson, JToon) to test scope only

### Fixed
- README Gradle version now correctly shows 1.2.0

## [1.2.0] - 2025-12-08

### Added
- Added encodeLines API for streaming TOON output line-by-line without buffering the full string.
- Introduced encode replacer hook to transform or filter values during encoding (JSON.stringify-style).
- Implemented safe key folding with configurable flattenDepth to collapse single-key chains into dotted keys for more compact output.
- Added flatten option to force collapsing single-key wrappers (with depth guard and collision checks).
- Added safe path expansion on decode to rebuild dotted keys into nested maps for lossless round-trips with folding.
- Added decodeLines to decode pre-split TOON lines directly.
- Added decodeToEvents / decodeLinesToEvents for event-based consumption of decoded data.
- Added decode replacer hook to transform or omit values post-decode.
- Standardized array headers to the [N] form (no legacy [#N] marker) while still accepting old syntax when parsing.
- Updated examples to cover the new APIs and removed legacy length-marker usage.

## [1.1.0] - 2025-11-05

This release introduces **automatic POJO deserialization**, a major new feature that eliminates manual type casting and brings TOON4J to parity with modern serialization libraries while maintaining zero dependencies.

### 🎉 Major Features

#### Automatic POJO Deserialization

Decode TOON format directly to strongly-typed Java objects with a single method call:

```java
// Before: Manual casting (verbose and error-prone)
Object decoded = Toon.decode(toon);
Map<String, Object> map = (Map<String, Object>) decoded;
User user = new User();
user.setId((Integer) map.get("id"));
user.setName((String) map.get("name"));
// ... more manual mapping

// After: Automatic deserialization (clean and type-safe)
User user = Toon.decode(toon, User.class);
```

**New API Methods:**
- `<T> T decode(String input, Class<T> targetClass)`
- `<T> T decode(String input, Class<T> targetClass, DecodeOptions options)`

**Supported Features:**
- ✅ **JavaBeans** with setter methods
- ✅ **Public fields** POJOs
- ✅ **Java Records** (Java 17+)
- ✅ **Nested POJOs** with automatic recursive deserialization
- ✅ **Collections with generics** - Automatic `List<Map>` → `List<Employee>` conversion
- ✅ **Primitive arrays** (int[], String[], etc.)
- ✅ **Enums** with automatic string-to-enum conversion
- ✅ **Automatic type conversions** (Integer → Long, Number → BigDecimal, etc.)
- ✅ **Cached reflection metadata** for high performance
