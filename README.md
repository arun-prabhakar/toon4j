![TOON logo with step‑by‑step guide](./.github/og.png)
# TOON4J

Token-Oriented Object Notation (TOON) encoder for Java.

TOON is a compact, human-readable format designed for passing structured data to Large Language Models with significantly reduced token usage. This is the Java implementation port of the [original TOON specification](https://github.com/byjohann/toon).

## Features

- 💸 **Token-efficient:** typically 30–60% fewer tokens than JSON
- 🤿 **LLM-friendly guardrails:** explicit lengths and field lists help models validate output
- 🍱 **Minimal syntax:** removes redundant punctuation (braces, brackets, most quotes)
- 📐 **Indentation-based structure:** replaces braces with whitespace for better readability
- 🧺 **Tabular arrays:** declare keys once, then stream rows without repetition
- 🤖 **Automatic POJO serialization:** works with any Java object out of the box (DSL-JSON)
- 🪶 **Lightweight:** only ~150KB total (toon4j + DSL-JSON)
- ✨ **Comprehensive type support:** Optional, Stream, primitive arrays, and all Java temporal types

## Installation

### Maven

```xml
<dependency>
    <groupId>im.arun</groupId>
    <artifactId>toon4j</artifactId>
    <version>0.0.1</version>
</dependency>
```

### Gradle

```gradle
implementation 'im.arun:toon4j:0.0.1'
```

## Quick Start

```java
import im.arun.toon4j.Toon;
import java.util.*;

public class Example {
    public static void main(String[] args) {
        Map<String, Object> data = Map.of(
            "user", Map.of(
                "id", 123,
                "name", "Ada",
                "tags", List.of("reading", "gaming"),
                "active", true
            )
        );

        String toon = Toon.encode(data);
        System.out.println(toon);
    }
}
```

Output:

```
user:
  id: 123
  name: Ada
  tags[2]: reading,gaming
  active: true
```

## What's Included

✅ **Core TOON Encoding**
- Objects (nested)
- Arrays (inline, tabular, list formats)
- Empty containers
- Root arrays

✅ **Complete Type Support**
- All primitives (String, Boolean, Number)
- BigInteger and BigDecimal (with smart conversion)
- Optional<T> (automatic unwrapping)
- Stream<T> (automatic materialization)
- All primitive arrays (int[], double[], boolean[], etc.)
- All Java temporal types (LocalDate, OffsetDateTime, etc.)
- Collections (List, Set, Map)
- **POJOs (automatic serialization via DSL-JSON)**

✅ **Flexible Configuration**
- Builder pattern
- Factory methods
- Preset configurations (compact, verbose)
- Custom delimiters (comma, tab, pipe)
- Optional length markers

✅ **Lightweight Dependency**
- DSL-JSON for POJO serialization (~100KB)
- Total footprint: ~150KB (toon4j + DSL-JSON)
- 10x lighter than Jackson-based solutions (150KB vs 2MB)

## Usage Examples

### Simple Objects

```java
Map<String, Object> obj = Map.of(
    "id", 123,
    "name", "Ada",
    "active", true
);

Toon.encode(obj);
```

Output:
```
id: 123
name: Ada
active: true
```

### POJOs (Automatic Serialization)

toon4j automatically serializes any Java object (POJO) without manual conversion:

```java
public class User {
    private int id;
    private String name;
    private boolean active;

    public User(int id, String name, boolean active) {
        this.id = id;
        this.name = name;
        this.active = active;
    }

    // getters/setters...
}

// Automatic POJO serialization - no manual conversion needed!
User user = new User(123, "Ada", true);
String toon = Toon.encode(user);
```

Output:
```
id: 123
name: Ada
active: true
```

**Nested POJOs:**

```java
public class Address {
    private String city;
    private String country;
    // constructors, getters...
}

public class Person {
    private String name;
    private int age;
    private Address address;
    // constructors, getters...
}

Person person = new Person(
    "Alice",
    30,
    new Address("Paris", "France")
);

String toon = Toon.encode(person);
```

Output:
```
name: Alice
age: 30
address:
  city: Paris
  country: France
```

**Lists of POJOs:**

```java
public class Product {
    private String sku;
    private int quantity;
    private double price;
    // constructors, getters...
}

List<Product> products = List.of(
    new Product("A1", 2, 9.99),
    new Product("B2", 1, 14.50),
    new Product("C3", 5, 7.25)
);

String toon = Toon.encode(Map.of("products", products));
```

Output:
```
products[3]{sku,quantity,price}:
  A1,2,9.99
  B2,1,14.5
  C3,5,7.25
```

### Arrays of Objects (Tabular Format)

```java
List<Map<String, Object>> items = List.of(
    Map.of("sku", "A1", "qty", 2, "price", 9.99),
    Map.of("sku", "B2", "qty", 1, "price", 14.5)
);

Toon.encode(Map.of("items", items));
```

Output:
```
items[2]{sku,qty,price}:
  A1,2,9.99
  B2,1,14.5
```

### Custom Delimiters

```java
EncodeOptions options = EncodeOptions.builder()
    .delimiter(Delimiter.TAB)
    .build();

Toon.encode(data, options);
```

### Length Markers

```java
EncodeOptions options = EncodeOptions.builder()
    .lengthMarker(true)
    .build();

Toon.encode(data, options);
// Output: tags[#2]: reading,gaming
```

### Optional and Stream Support

```java
import java.util.Optional;
import java.util.stream.Stream;

Map<String, Object> data = Map.of(
    "presentValue", Optional.of("Hello"),
    "emptyValue", Optional.empty(),
    "numbers", Stream.of(1, 2, 3, 4, 5)
);

Toon.encode(data);
```

Output:
```
presentValue: Hello
emptyValue: null
numbers[5]: 1,2,3,4,5
```

### Primitive Arrays

```java
int[] integers = {1, 2, 3, 4, 5};
double[] doubles = {1.5, 2.5, 3.5};
boolean[] booleans = {true, false, true};

Map<String, Object> data = Map.of(
    "ints", integers,
    "doubles", doubles,
    "bools", booleans
);

Toon.encode(data);
```

Output:
```
ints[5]: 1,2,3,4,5
doubles[3]: 1.5,2.5,3.5
bools[3]: true,false,true
```

### Temporal Types

```java
import java.time.*;

Map<String, Object> data = Map.of(
    "timestamp", OffsetDateTime.now(),
    "date", LocalDate.of(2025, 1, 15),
    "time", LocalTime.of(14, 30, 0),
    "instant", Instant.now()
);

Toon.encode(data);
```

## API Reference

### Main API

#### `Toon.encode(Object value)`

Encode a value to TOON format with default options.

**Parameters:**
- `value` - Any JSON-serializable value (Map, List, primitive)

**Returns:** TOON-formatted string

#### `Toon.encode(Object value, EncodeOptions options)`

Encode a value to TOON format with custom options.

**Parameters:**
- `value` - Any JSON-serializable value (Map, List, primitive)
- `options` - Encoding options

**Returns:** TOON-formatted string

### EncodeOptions

Configuration for encoding behavior. Multiple ways to create options:

#### Builder Pattern

```java
EncodeOptions options = EncodeOptions.builder()
    .indent(2)                    // Spaces per indentation level (default: 2)
    .delimiter(Delimiter.COMMA)   // Delimiter for arrays (default: COMMA)
    .lengthMarker(false)          // Prefix array lengths with # (default: false)
    .build();
```

#### Factory Methods

```java
// Custom indent
EncodeOptions.withIndent(4)

// Custom delimiter
EncodeOptions.withDelimiter(Delimiter.TAB)

// Enable length marker
EncodeOptions.withLengthMarker()
```

#### Preset Configurations

```java
// Default options (2 spaces, comma delimiter, no length marker)
EncodeOptions.DEFAULT

// Compact mode (tab delimiter for concise output)
EncodeOptions.compact()

// Verbose mode (tab delimiter + length marker for maximum clarity)
EncodeOptions.verbose()
```

**Delimiter Options:**
- `Delimiter.COMMA` - Use comma as delimiter (default)
- `Delimiter.TAB` - Use tab as delimiter
- `Delimiter.PIPE` - Use pipe as delimiter

## Type Conversions

| Java Type | TOON Output | Notes |
|---|---|---|
| **Primitives** | | |
| `null` | `null` | |
| `String` | Quoted if needed, unquoted otherwise | Minimal quoting based on content |
| `Boolean` | `true` or `false` | |
| `Integer`, `Long` | Decimal form | |
| `Double`, `Float` | Decimal form | Whole numbers converted to long |
| **Big Numbers** | | |
| `BigInteger` | Long if in range, string otherwise | Converted to Long when possible |
| `BigDecimal` | Long if whole number, double otherwise | Cleaner output for whole numbers |
| **Java 8+ Types** | | |
| `Optional<T>` | Unwrapped value or `null` | Automatically unwraps nested Optionals |
| `Stream<T>` | TOON array | Materialized to list |
| **Temporal Types** | | |
| `Date` | ISO instant string | |
| `Instant` | ISO instant string | |
| `ZonedDateTime` | ISO zoned date-time string | |
| `OffsetDateTime` | ISO offset date-time string | |
| `LocalDateTime` | ISO local date-time string | Preserves local time |
| `LocalDate` | ISO local date string | |
| `LocalTime` | ISO local time string | |
| **Primitive Arrays** | | |
| `int[]`, `long[]`, `short[]`, `byte[]` | Inline array | Efficient handling |
| `double[]`, `float[]` | Inline array | NaN/Infinity → null |
| `boolean[]` | Inline array | |
| `char[]` | Inline array | Converted to strings |
| `Object[]` | TOON array | Recursive encoding |
| **Collections** | | |
| `List`, `Set`, `Collection` | TOON array format | Tabular if uniform objects |
| `Map` | TOON object format | String keys required |
| **POJOs** | | |
| Custom Java objects | TOON object format | Automatic serialization via DSL-JSON |
| Nested POJOs | Nested TOON objects | Full object graph support |
| **Special Values** | | |
| `NaN`, `Infinity` | `null` | Invalid floating point values |

## Building from Source

```bash
cd toon4j
mvn clean install
```

## Running Tests

```bash
mvn test
```

## License

MIT License - see the [original TOON repository](https://github.com/johannschopplich/toon) for details.

## Related Projects

- **Elixir:** [toon_ex](https://github.com/kentaro/toon_ex)
- **PHP:** [toon-php](https://github.com/HelgeSverre/toon-php)
- **Python:** [pytoon](https://github.com/bpradana/pytoon)
    - [python-toon](https://github.com/xaviviro/python-toon)
    - [toon-python](https://gitlab.com/KanTakahiro/toon-python)
- **Ruby:** [toon-ruby](https://github.com/andrepcg/toon-ruby)
- **.NET:** [toon.NET](https://github.com/ghost1face/toon.NET)
- **Swift:** [TOONEncoder](https://github.com/mattt/TOONEncoder)
- **Go:** [gotoon](https://github.com/alpkeskin/gotoon)

## Specification

For the complete TOON specification, see [SPEC.md](https://github.com/johannschopplich/toon/blob/main/SPEC.md) in the original repository.
