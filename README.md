![TOON logo with step‑by‑step guide](./.github/og.png)
# TOON4J ![GitHub License](https://img.shields.io/github/license/arun-prabhakar/toon4j)


[![codecov](https://codecov.io/github/arun-prabhakar/toon4j/graph/badge.svg?token=JBNBPYI95S)](https://codecov.io/github/arun-prabhakar/toon4j) [![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=im.arun.toon4j&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=im.arun.toon4j)  [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=im.arun.toon4j&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=im.arun.toon4j) 

**High-performance, zero-dependency TOON encoder and decoder for Java.**

TOON (Token-Oriented Object Notation) is a compact, human-readable format designed for passing structured data to Large Language Models with significantly reduced token usage. 

TOON's sweet spot is **uniform arrays of objects** – multiple fields per row, with the same structure across all items. It borrows YAML's indentation-based structure for nested objects and CSV's tabular format for uniform data rows, then optimizes both for token efficiency in LLM contexts. Think of it as a translation layer: use standard Java objects (POJOs, Maps, etc.) programmatically, and convert to TOON for efficient LLM input.

This library, TOON4J, delivers **production-grade performance** (~5ms for 256KB) with zero external dependencies, making it the fastest and lightest Java TOON implementation available. It is a Java port of the [original TOON specification](https://github.com/byjohann/toon).

## Table of Contents

- [Quick Start](#quick-start)
- [Installation](#installation)
- [Examples](#examples)
- [Why TOON4J?](#why-toon4j)
- [Key Features](#key-features)
- [Usage Examples](#usage-examples)
- [Using TOON in LLM Prompts](#using-toon-in-llm-prompts)
- [Syntax Cheatsheet](#syntax-cheatsheet)
- [Performance](#-performance)
- [API Reference](#api-reference)
- [Type Conversions](#type-conversions)
- [Building from Source](#building-from-source)
- [Running Tests](#running-tests)
- [Specification](#specification)
- [Related Projects](#related-projects)
- [License](#license)

## Quick Start

### Encoding

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

### Decoding

```java
String toon = """
    user:
      id: 123
      name: Ada
      tags[2]: reading,gaming
      active: true
    """;

Object data = Toon.decode(toon);
// Returns: {user={id=123, name=Ada, tags=[reading, gaming], active=true}}
```

## Installation

### Maven

```xml
<dependency>
    <groupId>im.arun</groupId>
    <artifactId>toon4j</artifactId>
    <version>1.0.0</n>
</dependency>
```

### Gradle

```gradle
implementation 'im.arun:toon4j:1.0.0'
```

## Examples

Looking for comprehensive examples? Check out the **[toon4j-example](https://github.com/arun-prabhakar/toon4j-example)** project with runnable examples:

- **[EncoderExample.java](https://github.com/arun-prabhakar/toon4j-example/blob/main/src/main/java/im/arun/toon4j/example/EncoderExample.java)** - 9 encoding examples (primitives, objects, arrays, custom options, delimiters)
- **[DecoderExample.java](https://github.com/arun-prabhakar/toon4j-example/blob/main/src/main/java/im/arun/toon4j/example/DecoderExample.java)** - 9 decoding examples (primitives, objects, arrays, error handling, round-trip)
- **[AdvancedExample.java](https://github.com/arun-prabhakar/toon4j-example/blob/main/src/main/java/im/arun/toon4j/example/AdvancedExample.java)** - 5 advanced examples (LLM optimization, token savings, large datasets)
- **[PojoExample.java](https://github.com/arun-prabhakar/toon4j-example/blob/main/src/main/java/im/arun/toon4j/example/PojoExample.java)** - 7 POJO examples (serialization, deserialization, nested objects, records, enums)

Run any example:
```bash
git clone https://github.com/arun-prabhakar/toon4j-example.git
cd toon4j-example
mvn clean compile
mvn exec:java -Dexec.mainClass="im.arun.toon4j.example.EncoderExample"
```

See the **[Example README](https://github.com/arun-prabhakar/toon4j-example/README.md)** for complete documentation.

## Why TOON4J?

### Performance + Efficiency Combined

TOON4J uniquely combines **encoding efficiency** with **runtime performance**:

| Aspect | Value | Benefit |
|--------|-------|---------|
| **Token Usage** | 30-60% fewer than JSON | 🎯 Direct cost savings on LLM APIs |
| **Encoding Speed** | 4.9ms (256KB), 9.4ms (1MB) | ⚡ Fast enough for real-time APIs |
| **Dependencies** | Zero | 📦 No dependency conflicts or bloat |
| **Bundle Size** | ~80KB | 🪶 Minimal footprint |
| **Scalability** | Linear performance scaling | 📈 Predictable for any data size |
| **Decode Support** | Full round-trip | 🔄 Parse TOON back to objects |

### Key Advantages

**🚀 Production-Ready Performance**
- Sub-5ms encoding for typical API payloads (256KB)
- Sub-10ms for medium datasets (1MB)
- Linear scaling ensures predictable performance at any scale

**💰 Direct Cost Savings**
- 30-60% token reduction = 30-60% lower LLM API costs
- Example: $1000/month → $400-700/month savings
- ROI is immediate and measurable

**⚡ Zero-Dependency Architecture**
- No external libraries means no version conflicts
- Smaller bundle size (80KB vs 2MB+ for alternatives)
- Easier integration, faster builds, cleaner deployments

**☁️ Ideal for Serverless & Edge**
- **Fast Cold Starts:** Zero dependencies and a tiny footprint (~80KB) ensure minimal startup latency.
- **Low Memory Overhead:** Perfect for high-density, memory-constrained environments like AWS Lambda or Google Cloud Functions.
- **No Dependency Conflicts:** Simplifies packaging and deployment, avoiding common serverless packaging issues.

**🔄 Complete Functionality**
- Full encode/decode support (round-trip serialization)
- Strict and lenient parsing modes
- All TOON features supported (tabular arrays, custom delimiters, nested objects)

**📊 Enterprise-Ready**
- Thread-safe concurrent caching
- Optimized for high-throughput scenarios
- Minimal memory overhead
- Production-tested architecture

## Key Features

- 💸 **Token-efficient:** typically 30–60% fewer tokens than JSON
- ⚡ **High performance:** 4.9ms for 256KB, 9.4ms for 1MB, linear scaling with data size
- 🤿 **LLM-friendly guardrails:** explicit lengths and field lists help models validate output
- 🍱 **Minimal syntax:** removes redundant punctuation (braces, brackets, most quotes)
- 📐 **Indentation-based structure:** replaces braces with whitespace for better readability
- 🧺 **Tabular arrays:** declare keys once, then stream rows without repetition
- 🤖 **Automatic POJO serialization:** works with any Java object out of the box (optimized reflection with cached accessors)
- 🔄 **Full decode support:** parse TOON back into Java objects with strict/lenient modes
- 🪶 **Zero dependencies:** no external libraries required - pure Java implementation
- 📦 **Lightweight:** only ~80KB total
- ✨ **Comprehensive type support:** Optional, Stream, primitive arrays, and all Java temporal types

## Usage Examples

This section covers more advanced use cases beyond the Quick Start.

### POJOs (Automatic Serialization)

toon4j automatically serializes any Java object (POJO) using optimized reflection:

```java
public class User {
    private int id;
    private String name;
    private boolean active;

    // constructors, getters...
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

### Tabular Arrays (Lists of POJOs)

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

### Lenient Decoding

While strict decoding (the default) ensures data integrity, lenient mode can be useful for parsing potentially imperfect TOON data.

```java
// Strict mode (default) - throws error if count mismatch
String toon = "items[3]: a,b";  // Declared 3, but only 2 values
// Toon.decode(toon);  // Throws IllegalArgumentException

// Lenient mode - accepts count mismatch
DecodeOptions options = DecodeOptions.lenient();
Map<?, ?> result = (Map<?, ?>) Toon.decode(toon, options);
List<?> items = (List<?>) result.get("items");
// items.size() == 2 (lenient mode accepts it)
```

### POJO Deserialization (Automatic Type Conversion)

toon4j now supports automatic deserialization to POJOs, eliminating manual type casting:

```java
public class User {
    private int id;
    private String name;
    private String email;
    private boolean active;

    // Getters and setters...
}

String toon = """
    id: 123
    name: Ada
    email: ada@example.com
    active: true
    """;

// Deserialize directly to POJO
User user = Toon.decode(toon, User.class);

System.out.println(user.getName());  // "Ada"
System.out.println(user.getId());    // 123
```

**Supports:**
- ✅ **JavaBeans** (with setters)
- ✅ **Public fields**
- ✅ **Java Records** (Java 17+)
- ✅ **Nested POJOs**
- ✅ **Collections with generics** (`List<Employee>`, `Set<String>`)
- ✅ **Arrays** (primitive and object arrays)
- ✅ **Enums**
- ✅ **Numeric type conversions** (Integer → Long, etc.)

**Nested POJOs:**

```java
public class Address {
    private String street;
    private String city;
    // Getters/setters...
}

public class Employee {
    private int id;
    private String name;
    private Address address;
    // Getters/setters...
}

String toon = """
    id: 456
    name: Bob
    address:
      street: 123 Main St
      city: Boston
    """;

Employee employee = Toon.decode(toon, Employee.class);
System.out.println(employee.getAddress().getCity());  // "Boston"
```

**Collections with Generic Types:**

```java
public class Department {
    private String name;
    private List<Employee> employees;  // Automatically converts List<Map> to List<Employee>
    // Getters/setters...
}

String toon = """
    name: Engineering
    employees[2]{id,name}:
      1,Alice
      2,Bob
    """;

Department dept = Toon.decode(toon, Department.class);
System.out.println(dept.getEmployees().get(0).getName());  // "Alice"
```

**Java Records:**

```java
public record Person(String name, int age, String city) {}

String toon = """
    name: Charlie
    age: 30
    city: Seattle
    """;

Person person = Toon.decode(toon, Person.class);
System.out.println(person.name());  // "Charlie"
```

## Using TOON in LLM Prompts

TOON works best when you show the format instead of describing it. The structure is self-documenting – models parse it naturally once they see the pattern.

### Sending TOON to LLMs (Input)

Wrap your encoded data in a fenced code block (label it ```toon for clarity). The indentation and headers are usually enough – models treat it like familiar YAML or CSV. The explicit length markers (`[N]`) and field headers (`{field1,field2}`) help the model track structure, especially for large tables.

### Generating TOON from LLMs (Output)

For output, be more explicit. When you want the model to **generate** TOON:

- **Show the expected header** (`users[N]{id,name,role}:`). The model fills rows instead of repeating keys, reducing generation errors.
- **State the rules:** 2-space indent, no trailing spaces, `[N]` matches row count.

Here’s a prompt that works for both reading and generating:

````
Data is in TOON format (2-space indent, arrays show length and fields).

```toon
users[3]{id,name,role,lastLogin}:
  1,Alice,admin,2025-01-15T10:30:00Z
  2,Bob,user,2025-01-14T15:22:00Z
  3,Charlie,user,2025-01-13T09:45:00Z
```

Task: Return only users with role "user" as TOON. Use the same header. Set [N] to match the row count. Output only the code block.
````

## Syntax Cheatsheet

<details>
<summary>**Show format examples**</summary>

```
// Object
{ id: 1, name: 'Ada' }          → id: 1
                                  name: Ada

// Nested object
{ user: { id: 1 } }             → user:
                                    id: 1

// Primitive array (inline)
{ tags: ['foo', 'bar'] }        → tags[2]: foo,bar

// Tabular array (uniform objects)
{ items: [                      → items[2]{id,qty}:
  { id: 1, qty: 5 },                1,5
  { id: 2, qty: 3 }                 2,3
]}

// Mixed / non-uniform (list)
{ items: [1, { a: 1 }, 'x'] }   → items[3]:
                                    - 1
                                    - a: 1
                                    - x

// Array of arrays
{ pairs: [[1, 2], [3, 4]] }     → pairs[2]:
                                    - [2]: 1,2
                                    - [2]: 3,4

// Root array
['x', 'y']                      → [2]: x,y

// Empty containers
{}                              → (empty output)
{ items: [] }                   → items[0]:

// Special quoting
{ note: 'hello, world' }        → note: "hello, world"
{ items: ['true', true] }       → items[2]: "true",true
```

</details>

## ⚡ Performance

TOON4J v1.0.0 delivers **production-grade performance** optimized for real-world workloads:

### Benchmark Results

**Real-world data encoding performance** (50 iterations, Java 17):

| Data Size | Average Time | Throughput | Use Case |
|-----------|--------------|------------|----------|
| 256KB | 4.9ms | 203 encodes/s | API responses, small datasets |
| 1MB | 9.4ms | 106 encodes/s | Medium datasets, batch processing |
| 5MB | 40.1ms | 25 encodes/s | Large datasets, data exports |

**Performance characteristics scale linearly** with data size, providing predictable behavior.

### Key Performance Features

- **Optimized Reflection:** Cached field/getter accessors eliminate repeated reflection overhead
- **Thread-Safe Caching:** `ConcurrentHashMap` for lock-free accessor reuse across threads
- **Direct Conversion:** No intermediate JSON serialization (POJO → Map directly)
- **Pre-sized Collections:** Minimizes array reallocation during encoding
- **Pooled StringBuilder:** ThreadLocal object pooling reduces GC pressure
- **Linear Scaling:** Performance scales predictably with data size

## API Reference

### Encoding

```java
// Encode with default options
String toon = Toon.encode(Object value);

// Encode with custom options
String toon = Toon.encode(Object value, EncodeOptions options);
```

**EncodeOptions:**
```java
EncodeOptions options = EncodeOptions.builder()
    .indent(4)                          // 4-space indentation (default: 2)
    .delimiter(Delimiter.PIPE)          // Use pipe delimiter (default: COMMA)
    .lengthMarker(true)                 // Add # to array lengths (default: false)
    .build();

// Presets
EncodeOptions.compact()   // Minimal output
EncodeOptions.verbose()   // Maximum readability
```

### Decoding

```java
// Decode to Map/List/primitive
Object data = Toon.decode(String input);
Object data = Toon.decode(String input, DecodeOptions options);

// Decode to POJO (automatic deserialization)
User user = Toon.decode(String input, Class<User> targetClass);
User user = Toon.decode(String input, Class<User> targetClass, DecodeOptions options);
```

**DecodeOptions:**
```java
DecodeOptions options = DecodeOptions.lenient();      // Lenient mode with 2-space indent
DecodeOptions options = DecodeOptions.lenient(4);     // Lenient mode with 4-space indent
DecodeOptions options = DecodeOptions.strict();       // Strict mode (default)
DecodeOptions options = DecodeOptions.strict(4);      // Strict mode with 4-space indent
```

## Building from Source

```bash
cd toon4j
mvn clean install
```

## Running Tests

```bash
mvn test
```

## Specification

For the complete TOON specification, see [SPEC.md](https://github.com/johannschopplich/toon/blob/main/SPEC.md) in the original repository.

## Related Projects

> When implementing TOON in other languages, please follow the [official SPEC.md](https://github.com/byjohann/toon/blob/main/SPEC.md) to ensure compatibility.

- **.NET:** [ToonSharp](https://github.com/0xZunia/ToonSharp)
- **Dart:** [toon](https://github.com/wisamidris77/toon)
- **Elixir:** [toon_ex](https://github.com/kentaro/toon_ex)
- **Gleam:** [toon_codec](https://github.com/axelbellec/toon_codec)
- **Go:** [gotoon](https://github.com/alpkeskin/gotoon)
- **Java:**
    - **toon4j** (this library) - A high-performance, zero-dependency implementation with a focus on speed, low memory overhead, and full round-trip decoding.
    - [JToon](https://github.com/felipestanzani/JToon)
- **PHP:** [toon-php](https://github.com/HelgeSverre/toon-php)
- **Python:** [python-toon](https://github.com/xaviviro/python-toon) or [pytoon](https://github.com/bpradana/pytoon)
- **Ruby:** [toon-ruby](https://github.com/andrepcg/toon-ruby)
- **Rust:** [rtoon](https://github.com/shreyasbhat0/rtoon)
- **Swift:** [TOONEncoder](https://github.com/mattt/TOONEncoder)

## License

MIT License - see the [original TOON repository](https://github.com/johannschopplich/toon) for details.

[![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=im.arun.toon4j)](https://sonarcloud.io/summary/new_code?id=im.arun.toon4j) [![SonarQube Cloud](https://sonarcloud.io/images/project_badges/sonarcloud-highlight.svg)](https://sonarcloud.io/summary/new_code?id=im.arun.toon4j)