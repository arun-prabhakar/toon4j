package im.arun.toon4j;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for PojoDeserializer - Map to POJO conversion.
 */
class PojoDeserializerTest {

    // ==================== Test POJOs ====================

    // Simple POJO with setters (JavaBean style)
    public static class User {
        private int id;
        private String name;
        private String email;
        private boolean active;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }

    // POJO with public fields
    public static class Point {
        public int x;
        public int y;
        public String label;
    }

    // Java Record (requires Java 17+)
    public record Person(String name, int age, String city) {}

    // Nested POJO
    public static class Address {
        private String street;
        private String city;
        private String zipCode;

        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getZipCode() { return zipCode; }
        public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    }

    public static class Employee {
        private int id;
        private String name;
        private Address address;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Address getAddress() { return address; }
        public void setAddress(Address address) { this.address = address; }
    }

    // POJO with collections
    public static class Team {
        private String name;
        private List<String> members;
        private Set<String> tags;
        private int[] scores;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public List<String> getMembers() { return members; }
        public void setMembers(List<String> members) { this.members = members; }

        public Set<String> getTags() { return tags; }
        public void setTags(Set<String> tags) { this.tags = tags; }

        public int[] getScores() { return scores; }
        public void setScores(int[] scores) { this.scores = scores; }
    }

    // POJO with nested list of POJOs
    public static class Department {
        private String name;
        private List<Employee> employees;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public List<Employee> getEmployees() { return employees; }
        public void setEmployees(List<Employee> employees) { this.employees = employees; }
    }

    // Enum for testing
    public enum Role {
        ADMIN, USER, GUEST
    }

    public static class Account {
        private String username;
        private Role role;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }
    }

    // POJO with various numeric types
    public static class Numbers {
        private int intVal;
        private long longVal;
        private double doubleVal;
        private float floatVal;
        private short shortVal;
        private byte byteVal;
        private BigDecimal bigDecimalVal;
        private BigInteger bigIntegerVal;

        public int getIntVal() { return intVal; }
        public void setIntVal(int intVal) { this.intVal = intVal; }

        public long getLongVal() { return longVal; }
        public void setLongVal(long longVal) { this.longVal = longVal; }

        public double getDoubleVal() { return doubleVal; }
        public void setDoubleVal(double doubleVal) { this.doubleVal = doubleVal; }

        public float getFloatVal() { return floatVal; }
        public void setFloatVal(float floatVal) { this.floatVal = floatVal; }

        public short getShortVal() { return shortVal; }
        public void setShortVal(short shortVal) { this.shortVal = shortVal; }

        public byte getByteVal() { return byteVal; }
        public void setByteVal(byte byteVal) { this.byteVal = byteVal; }

        public BigDecimal getBigDecimalVal() { return bigDecimalVal; }
        public void setBigDecimalVal(BigDecimal bigDecimalVal) { this.bigDecimalVal = bigDecimalVal; }

        public BigInteger getBigIntegerVal() { return bigIntegerVal; }
        public void setBigIntegerVal(BigInteger bigIntegerVal) { this.bigIntegerVal = bigIntegerVal; }
    }

    // ==================== Tests ====================

    @Test
    void testDeserializeSimplePojoWithSetters() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", 123);
        map.put("name", "Alice");
        map.put("email", "alice@example.com");
        map.put("active", true);

        User user = PojoDeserializer.fromMap(map, User.class);

        assertNotNull(user);
        assertEquals(123, user.getId());
        assertEquals("Alice", user.getName());
        assertEquals("alice@example.com", user.getEmail());
        assertTrue(user.isActive());
    }

    @Test
    void testDeserializePojoWithPublicFields() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("x", 10);
        map.put("y", 20);
        map.put("label", "Point A");

        Point point = PojoDeserializer.fromMap(map, Point.class);

        assertNotNull(point);
        assertEquals(10, point.x);
        assertEquals(20, point.y);
        assertEquals("Point A", point.label);
    }

    @Test
    void testDeserializeRecord() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", "Bob");
        map.put("age", 30);
        map.put("city", "New York");

        Person person = PojoDeserializer.fromMap(map, Person.class);

        assertNotNull(person);
        assertEquals("Bob", person.name());
        assertEquals(30, person.age());
        assertEquals("New York", person.city());
    }

    @Test
    void testDeserializeNestedPojo() {
        Map<String, Object> addressMap = new LinkedHashMap<>();
        addressMap.put("street", "123 Main St");
        addressMap.put("city", "Boston");
        addressMap.put("zipCode", "02101");

        Map<String, Object> employeeMap = new LinkedHashMap<>();
        employeeMap.put("id", 456);
        employeeMap.put("name", "Charlie");
        employeeMap.put("address", addressMap);

        Employee employee = PojoDeserializer.fromMap(employeeMap, Employee.class);

        assertNotNull(employee);
        assertEquals(456, employee.getId());
        assertEquals("Charlie", employee.getName());

        assertNotNull(employee.getAddress());
        assertEquals("123 Main St", employee.getAddress().getStreet());
        assertEquals("Boston", employee.getAddress().getCity());
        assertEquals("02101", employee.getAddress().getZipCode());
    }

    @Test
    void testDeserializePojoWithList() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", "Team A");
        map.put("members", List.of("Alice", "Bob", "Charlie"));
        map.put("tags", List.of("active", "featured"));
        map.put("scores", List.of(95, 87, 92));

        Team team = PojoDeserializer.fromMap(map, Team.class);

        assertNotNull(team);
        assertEquals("Team A", team.getName());
        assertEquals(List.of("Alice", "Bob", "Charlie"), team.getMembers());
        assertEquals(new HashSet<>(List.of("active", "featured")), team.getTags());
        assertArrayEquals(new int[]{95, 87, 92}, team.getScores());
    }

    @Test
    void testDeserializeNestedListOfPojos() {
        Map<String, Object> emp1Map = new LinkedHashMap<>();
        emp1Map.put("id", 1);
        emp1Map.put("name", "Alice");

        Map<String, Object> emp2Map = new LinkedHashMap<>();
        emp2Map.put("id", 2);
        emp2Map.put("name", "Bob");

        Map<String, Object> deptMap = new LinkedHashMap<>();
        deptMap.put("name", "Engineering");
        deptMap.put("employees", List.of(emp1Map, emp2Map));

        Department dept = PojoDeserializer.fromMap(deptMap, Department.class);

        assertNotNull(dept);
        assertEquals("Engineering", dept.getName());
        assertEquals(2, dept.getEmployees().size());
        assertEquals(1, dept.getEmployees().get(0).getId());
        assertEquals("Alice", dept.getEmployees().get(0).getName());
        assertEquals(2, dept.getEmployees().get(1).getId());
        assertEquals("Bob", dept.getEmployees().get(1).getName());
    }

    @Test
    void testDeserializeEnum() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("username", "admin");
        map.put("role", "ADMIN");

        Account account = PojoDeserializer.fromMap(map, Account.class);

        assertNotNull(account);
        assertEquals("admin", account.getUsername());
        assertEquals(Role.ADMIN, account.getRole());
    }

    @Test
    void testDeserializeNumericTypes() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("intVal", 42);
        map.put("longVal", 123456789L);
        map.put("doubleVal", 3.14159);
        map.put("floatVal", 2.71828f);
        map.put("shortVal", 100);
        map.put("byteVal", 10);
        map.put("bigDecimalVal", 999.99);
        map.put("bigIntegerVal", 123456);

        Numbers numbers = PojoDeserializer.fromMap(map, Numbers.class);

        assertNotNull(numbers);
        assertEquals(42, numbers.getIntVal());
        assertEquals(123456789L, numbers.getLongVal());
        assertEquals(3.14159, numbers.getDoubleVal(), 0.0001);
        assertEquals(2.71828f, numbers.getFloatVal(), 0.0001);
        assertEquals(100, numbers.getShortVal());
        assertEquals(10, numbers.getByteVal());
        assertEquals(new BigDecimal("999.99"), numbers.getBigDecimalVal());
        assertEquals(BigInteger.valueOf(123456), numbers.getBigIntegerVal());
    }

    @Test
    void testDeserializeWithTypeConversion() {
        // Test converting Integer to Long
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("longVal", 42); // Integer instead of Long

        Numbers numbers = PojoDeserializer.fromMap(map, Numbers.class);

        assertEquals(42L, numbers.getLongVal());
    }

    @Test
    void testDeserializeNullMap() {
        User user = PojoDeserializer.fromMap(null, User.class);
        assertNull(user);
    }

    @Test
    void testDeserializeEmptyMap() {
        User user = PojoDeserializer.fromMap(new LinkedHashMap<>(), User.class);
        assertNotNull(user);
        assertEquals(0, user.getId());
        assertNull(user.getName());
    }

    @Test
    void testDeserializeNullTargetClassThrows() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", 123);

        assertThrows(IllegalArgumentException.class, () ->
            PojoDeserializer.fromMap(map, null)
        );
    }

    @Test
    void testDeserializePartialData() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", 123);
        map.put("name", "Alice");
        // email and active are missing

        User user = PojoDeserializer.fromMap(map, User.class);

        assertNotNull(user);
        assertEquals(123, user.getId());
        assertEquals("Alice", user.getName());
        assertNull(user.getEmail());
        assertFalse(user.isActive());
    }

    @Test
    void testDeserializeExtraFields() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", 123);
        map.put("name", "Alice");
        map.put("extraField", "ignored"); // This should be ignored

        User user = PojoDeserializer.fromMap(map, User.class);

        assertNotNull(user);
        assertEquals(123, user.getId());
        assertEquals("Alice", user.getName());
    }

    @Test
    void testDeserializeValuePrimitive() {
        Object result = PojoDeserializer.deserializeValue(42, Integer.class);
        assertEquals(42, result);
    }

    @Test
    void testDeserializeValueString() {
        Object result = PojoDeserializer.deserializeValue("hello", String.class);
        assertEquals("hello", result);
    }

    @Test
    void testDeserializeValueNull() {
        Object result = PojoDeserializer.deserializeValue(null, String.class);
        assertNull(result);
    }

    @Test
    void testDeserializeValueIntegerToLong() {
        Object result = PojoDeserializer.deserializeValue(42, Long.class);
        assertEquals(42L, result);
    }

    @Test
    void testDeserializeValueDoubleToFloat() {
        Object result = PojoDeserializer.deserializeValue(3.14, Float.class);
        assertEquals(3.14f, (Float) result, 0.01);
    }

    @Test
    void testDeserializeValueBooleanFromString() {
        Object result = PojoDeserializer.deserializeValue("true", Boolean.class);
        assertEquals(true, result);
    }

    @Test
    void testDeserializeValueListToArray() {
        List<Integer> list = List.of(1, 2, 3);
        Object result = PojoDeserializer.deserializeValue(list, int[].class);

        assertTrue(result instanceof int[]);
        assertArrayEquals(new int[]{1, 2, 3}, (int[]) result);
    }

    @Test
    void testDeserializeValueListToSet() {
        List<String> list = List.of("a", "b", "c");
        Object result = PojoDeserializer.deserializeValue(list, Set.class);

        assertTrue(result instanceof Set);
        assertEquals(new HashSet<>(list), result);
    }

    @Test
    void testDeserializeValueMapToPojo() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("x", 10);
        map.put("y", 20);

        Object result = PojoDeserializer.deserializeValue(map, Point.class);

        assertTrue(result instanceof Point);
        Point point = (Point) result;
        assertEquals(10, point.x);
        assertEquals(20, point.y);
    }

    @Test
    void testDeserializeValueEnum() {
        Object result = PojoDeserializer.deserializeValue("ADMIN", Role.class);
        assertEquals(Role.ADMIN, result);
    }

    @Test
    void testDeserializeValueBigDecimalFromDouble() {
        Object result = PojoDeserializer.deserializeValue(123.45, BigDecimal.class);
        assertEquals(new BigDecimal("123.45"), result);
    }

    @Test
    void testDeserializeValueBigIntegerFromLong() {
        Object result = PojoDeserializer.deserializeValue(12345L, BigInteger.class);
        assertEquals(BigInteger.valueOf(12345), result);
    }

    @Test
    void testDeserializeRecordWithMissingField() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", "Bob");
        map.put("age", 30);
        // city is missing - should be null

        Person person = PojoDeserializer.fromMap(map, Person.class);

        assertNotNull(person);
        assertEquals("Bob", person.name());
        assertEquals(30, person.age());
        assertNull(person.city());
    }

    @Test
    void testCachingBehavior() {
        // First deserialization
        Map<String, Object> map1 = new LinkedHashMap<>();
        map1.put("id", 1);
        map1.put("name", "User1");

        User user1 = PojoDeserializer.fromMap(map1, User.class);
        assertEquals(1, user1.getId());

        // Second deserialization of same type should use cached metadata
        Map<String, Object> map2 = new LinkedHashMap<>();
        map2.put("id", 2);
        map2.put("name", "User2");

        User user2 = PojoDeserializer.fromMap(map2, User.class);
        assertEquals(2, user2.getId());

        // Both should succeed, demonstrating cache works
        assertNotEquals(user1.getId(), user2.getId());
    }

    @Test
    void testDeserializeStringArray() {
        List<String> list = List.of("apple", "banana", "cherry");
        Object result = PojoDeserializer.deserializeValue(list, String[].class);

        assertTrue(result instanceof String[]);
        assertArrayEquals(new String[]{"apple", "banana", "cherry"}, (String[]) result);
    }

    @Test
    void testDeserializeLinkedHashSet() {
        List<String> list = List.of("a", "b", "c");
        Object result = PojoDeserializer.deserializeValue(list, LinkedHashSet.class);

        assertTrue(result instanceof LinkedHashSet);
        assertEquals(new LinkedHashSet<>(list), result);
    }

    @Test
    void testDeserializeCharFromString() {
        Object result = PojoDeserializer.deserializeValue("A", Character.class);
        assertEquals('A', result);
    }

    @Test
    void testDeserializeEmptyStringToChar() {
        Object result = PojoDeserializer.deserializeValue("", Character.class);
        assertEquals('\0', result);
    }
}
