package im.arun.toon4j;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Toon.decode() with POJO type parameter.
 */
class ToonPojoDecodingTest {

    // ==================== Test POJOs ====================

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

    public static class Product {
        private String sku;
        private String name;
        private double price;

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
    }

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

    public static class Team {
        private String name;
        private List<String> members;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public List<String> getMembers() { return members; }
        public void setMembers(List<String> members) { this.members = members; }
    }

    public record Person(String name, int age, String city) {}

    public enum Status {
        ACTIVE, INACTIVE, PENDING
    }

    public static class Order {
        private String orderId;
        private Status status;

        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }

        public Status getStatus() { return status; }
        public void setStatus(Status status) { this.status = status; }
    }

    // ==================== Tests ====================

    @Test
    void testDecodeSimplePojo() {
        String toon = """
            id: 123
            name: Alice
            email: alice@example.com
            active: true
            """.trim();

        User user = Toon.decode(toon, User.class);

        assertNotNull(user);
        assertEquals(123, user.getId());
        assertEquals("Alice", user.getName());
        assertEquals("alice@example.com", user.getEmail());
        assertTrue(user.isActive());
    }

    @Test
    void testDecodePojoWithDefaultOptions() {
        String toon = """
            sku: SKU-001
            name: Laptop
            price: 999.99
            """.trim();

        Product product = Toon.decode(toon, Product.class);

        assertNotNull(product);
        assertEquals("SKU-001", product.getSku());
        assertEquals("Laptop", product.getName());
        assertEquals(999.99, product.getPrice(), 0.01);
    }

    @Test
    void testDecodePojoWithCustomOptions() {
        String toon = """
            id: 456
            name: Bob
            """.trim();

        DecodeOptions options = DecodeOptions.lenient();
        User user = Toon.decode(toon, User.class, options);

        assertNotNull(user);
        assertEquals(456, user.getId());
        assertEquals("Bob", user.getName());
    }

    @Test
    void testDecodeNestedPojo() {
        String toon = """
            id: 789
            name: Charlie
            address:
              street: 123 Main St
              city: Boston
              zipCode: 02101
            """.trim();

        Employee employee = Toon.decode(toon, Employee.class);

        assertNotNull(employee);
        assertEquals(789, employee.getId());
        assertEquals("Charlie", employee.getName());

        assertNotNull(employee.getAddress());
        assertEquals("123 Main St", employee.getAddress().getStreet());
        assertEquals("Boston", employee.getAddress().getCity());
        assertEquals("02101", employee.getAddress().getZipCode());
    }

    @Test
    void testDecodePojoWithArray() {
        String toon = """
            name: Team A
            members[3]: Alice,Bob,Charlie
            """.trim();

        Team team = Toon.decode(toon, Team.class);

        assertNotNull(team);
        assertEquals("Team A", team.getName());
        assertEquals(List.of("Alice", "Bob", "Charlie"), team.getMembers());
    }

    @Test
    void testDecodeRecord() {
        String toon = """
            name: David
            age: 35
            city: Seattle
            """.trim();

        Person person = Toon.decode(toon, Person.class);

        assertNotNull(person);
        assertEquals("David", person.name());
        assertEquals(35, person.age());
        assertEquals("Seattle", person.city());
    }

    @Test
    void testDecodePojoWithEnum() {
        String toon = """
            orderId: ORD-123
            status: ACTIVE
            """.trim();

        Order order = Toon.decode(toon, Order.class);

        assertNotNull(order);
        assertEquals("ORD-123", order.getOrderId());
        assertEquals(Status.ACTIVE, order.getStatus());
    }

    @Test
    void testDecodeNullTargetClassThrows() {
        String toon = "id: 123";

        assertThrows(IllegalArgumentException.class, () ->
            Toon.decode(toon, (Class<?>) null)
        );
    }

    @Test
    void testDecodeEmptyInputThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            Toon.decode("", User.class)
        );
    }

    @Test
    void testDecodeWhitespaceOnlyThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            Toon.decode("   ", User.class)
        );
    }

    @Test
    void testDecodeToObjectClass() {
        String toon = """
            id: 123
            name: Test
            """.trim();

        Object result = Toon.decode(toon, Object.class);

        assertNotNull(result);
        assertTrue(result instanceof java.util.Map);
    }

    @Test
    void testDecodeToStringClass() {
        String toon = "hello";

        String result = Toon.decode(toon, String.class);

        assertEquals("hello", result);
    }

    @Test
    void testDecodeToIntegerClass() {
        String toon = "42";

        Integer result = Toon.decode(toon, Integer.class);

        assertEquals(42, result);
    }

    @Test
    void testRoundTripEncodeDecode() {
        User original = new User();
        original.setId(999);
        original.setName("Eve");
        original.setEmail("eve@example.com");
        original.setActive(false);

        String toon = Toon.encode(original);
        User decoded = Toon.decode(toon, User.class);

        assertEquals(original.getId(), decoded.getId());
        assertEquals(original.getName(), decoded.getName());
        assertEquals(original.getEmail(), decoded.getEmail());
        assertEquals(original.isActive(), decoded.isActive());
    }

    @Test
    void testRoundTripNestedPojo() {
        Employee original = new Employee();
        original.setId(100);
        original.setName("Frank");

        Address address = new Address();
        address.setStreet("456 Oak Ave");
        address.setCity("Portland");
        address.setZipCode("97201");
        original.setAddress(address);

        String toon = Toon.encode(original);
        Employee decoded = Toon.decode(toon, Employee.class);

        assertEquals(original.getId(), decoded.getId());
        assertEquals(original.getName(), decoded.getName());
        assertEquals(original.getAddress().getStreet(), decoded.getAddress().getStreet());
        assertEquals(original.getAddress().getCity(), decoded.getAddress().getCity());
        assertEquals(original.getAddress().getZipCode(), decoded.getAddress().getZipCode());
    }

    @Test
    void testRoundTripRecord() {
        Person original = new Person("Grace", 28, "Denver");

        String toon = Toon.encode(original);
        Person decoded = Toon.decode(toon, Person.class);

        assertEquals(original.name(), decoded.name());
        assertEquals(original.age(), decoded.age());
        assertEquals(original.city(), decoded.city());
    }

    @Test
    void testDecodePartialData() {
        String toon = """
            id: 555
            name: Helen
            """.trim();

        User user = Toon.decode(toon, User.class);

        assertNotNull(user);
        assertEquals(555, user.getId());
        assertEquals("Helen", user.getName());
        assertNull(user.getEmail());
        assertFalse(user.isActive());
    }

    @Test
    void testDecodeWithExtraFields() {
        String toon = """
            id: 666
            name: Ivan
            extraField: ignored
            anotherExtra: also ignored
            """.trim();

        User user = Toon.decode(toon, User.class);

        assertNotNull(user);
        assertEquals(666, user.getId());
        assertEquals("Ivan", user.getName());
    }

    @Test
    void testDecodePrimitiveInteger() {
        String toon = "42";
        Integer result = Toon.decode(toon, Integer.class);
        assertEquals(42, result);
    }

    @Test
    void testDecodePrimitiveBoolean() {
        String toon = "true";
        Boolean result = Toon.decode(toon, Boolean.class);
        assertEquals(true, result);
    }

    @Test
    void testDecodePrimitiveDouble() {
        String toon = "3.14159";
        Double result = Toon.decode(toon, Double.class);
        assertEquals(3.14159, result, 0.0001);
    }

    @Test
    void testDecodeArrayToArray() {
        String toon = "items[3]: apple,banana,cherry";

        // Decode as Map first to get the array
        Object decoded = Toon.decode(toon);
        assertTrue(decoded instanceof java.util.Map);

        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> map = (java.util.Map<String, Object>) decoded;
        Object items = map.get("items");
        assertTrue(items instanceof List);
    }

    @Test
    void testDecodeWithIndentedContent() {
        String toon = """
            id: 111
            name: Julia
            address:
              street: 789 Pine St
              city: Austin
              zipCode: 78701
            """.trim();

        Employee employee = Toon.decode(toon, Employee.class);

        assertNotNull(employee);
        assertEquals(111, employee.getId());
        assertEquals("Julia", employee.getName());
        assertEquals("789 Pine St", employee.getAddress().getStreet());
        assertEquals("Austin", employee.getAddress().getCity());
        assertEquals("78701", employee.getAddress().getZipCode());
    }

    @Test
    void testDecodeComplexStructure() {
        String toon = """
            id: 222
            name: Kevin
            email: kevin@example.com
            active: true
            address:
              street: 100 Broadway
              city: New York
              zipCode: 10001
            """.trim();

        Employee employee = Toon.decode(toon, Employee.class);

        assertNotNull(employee);
        assertEquals(222, employee.getId());
        assertEquals("Kevin", employee.getName());
        assertNotNull(employee.getAddress());
        assertEquals("100 Broadway", employee.getAddress().getStreet());
        assertEquals("New York", employee.getAddress().getCity());
        assertEquals("10001", employee.getAddress().getZipCode());
    }
}
