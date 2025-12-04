# TOON4J Examples (local copy)

Comprehensive examples demonstrating how to use the TOON4J library for encoding and decoding Token-Oriented Object Notation (TOON).

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- TOON4J library (pulled from Maven Central)

## Project Structure

```
toon4j-example/
├── pom.xml
├── README.md
└── src/main/java/im/arun/toon4j/example/
    ├── AdvancedExample.java
    ├── DecoderExample.java
    ├── EncoderExample.java
    └── PojoExample.java
```

## Running the Examples

```bash
cd toon4j-example
mvn clean compile

# Run individual examples
mvn exec:java -Dexec.mainClass="im.arun.toon4j.example.EncoderExample"
mvn exec:java -Dexec.mainClass="im.arun.toon4j.example.DecoderExample"
mvn exec:java -Dexec.mainClass="im.arun.toon4j.example.AdvancedExample"
mvn exec:java -Dexec.mainClass="im.arun.toon4j.example.PojoExample"
```

## Example Overview

- EncoderExample.java
  1. Simple primitives
  2. Simple objects
  3. Nested objects
  4. Primitive arrays
  5. Tabular arrays
  6. Complex structures
  7. Custom options (indent, delimiters, key folding, replacers)
  8. Different delimiters
  9. Key folding & replacers
  10. Streaming lines via `encodeLines`
  11. Real-world example
- DecoderExample.java: primitives, objects, arrays, error handling, round-trip
- AdvancedExample.java: LLM optimization, token savings, large datasets, specials, delimiters
- PojoExample.java: POJO round-trip, nested POJOs, collections, records, enums, real-world

## Quick Start Code

### Encoding

```java
import im.arun.toon4j.Toon;
import java.util.*;

Map<String, Object> user = new LinkedHashMap<>();
user.put("id", 123);
user.put("name", "Ada");
user.put("tags", List.of("admin", "dev"));

String toon = Toon.encode(user);
System.out.println(toon);
// id: 123
// name: Ada
// tags[2]: admin,dev
```

### Decoding

```java
String toon = """
    id: 123
    name: Ada
    tags[2]: admin,dev
    """.trim();

@SuppressWarnings("unchecked")
Map<String, Object> decoded = (Map<String, Object>) Toon.decode(toon);
System.out.println("Name: " + decoded.get("name"));
```

### POJO Deserialization

```java
public class User {
    private int id;
    private String name;
    private String email;
    private boolean active;
    // getters/setters...
}

String toon = """
    id: 123
    name: Ada
    email: ada@example.com
    active: true
    """.trim();

User user = Toon.decode(toon, User.class);
System.out.println("Name: " + user.getName());
```

## Custom Options

```java
import im.arun.toon4j.*;

EncodeOptions options = EncodeOptions.builder()
    .indent(4)
    .delimiter(Delimiter.PIPE)
    .keyFolding(KeyFolding.SAFE)
    .replacer((key, value, path) -> "secret".equals(key) ? EncodeReplacer.OMIT : value)
    .build();

String toon = Toon.encode(data, options);

DecodeOptions decodeOptions = new DecodeOptions(2, true, PathExpansion.SAFE);
Object decoded = Toon.decode(toon, decodeOptions);
```

## Token Savings Example

**JSON (165 chars):**

```json
{"products":[{"id":"A1","name":"Laptop","price":999.99},{"id":"A2","name":"Mouse","price":29.99}],"total":2}
```

**TOON (98 chars — ~40% savings):**

```
products[2]{id,name,price}:
  A1,Laptop,999.99
  A2,Mouse,29.99
total: 2
```

## Documentation

- TOON4J repo: https://github.com/arun-prabhakar/toon4j
- TOON4J examples: https://github.com/arun-prabhakar/toon4j-example
- API docs: https://javadoc.io/doc/im.arun/toon4j

## License

MIT (same as TOON4J)
