package im.arun.toon4j;

import java.time.LocalDate;
import java.util.List;

/**
 * Examples of encoding POJOs with toon4j.
 * Demonstrates automatic POJO serialization via Gson.
 */
public class PojoExample {

    // Simple POJO
    static class User {
        private int id;
        private String name;
        private boolean active;

        public User(int id, String name, boolean active) {
            this.id = id;
            this.name = name;
            this.active = active;
        }
    }

    // Nested POJO
    static class Address {
        private String street;
        private String city;
        private String country;
        private String zipCode;

        public Address(String street, String city, String country, String zipCode) {
            this.street = street;
            this.city = city;
            this.country = country;
            this.zipCode = zipCode;
        }
    }

    static class Person {
        private String name;
        private int age;
        private Address address;
        private List<String> hobbies;

        public Person(String name, int age, Address address, List<String> hobbies) {
            this.name = name;
            this.age = age;
            this.address = address;
            this.hobbies = hobbies;
        }
    }

    // Product POJO for lists
    static class Product {
        private String sku;
        private String name;
        private int quantity;
        private double price;
        private LocalDate restockDate;

        public Product(String sku, String name, int quantity, double price, LocalDate restockDate) {
            this.sku = sku;
            this.name = name;
            this.quantity = quantity;
            this.price = price;
            this.restockDate = restockDate;
        }
    }

    static class Order {
        private int orderId;
        private String customerName;
        private List<Product> products;
        private double total;

        public Order(int orderId, String customerName, List<Product> products, double total) {
            this.orderId = orderId;
            this.customerName = customerName;
            this.products = products;
            this.total = total;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== POJO Examples ===\n");

        // Example 1: Simple POJO
        example1SimplePOJO();

        // Example 2: Nested POJOs
        example2NestedPOJO();

        // Example 3: List of POJOs (tabular format)
        example3ListOfPOJOs();

        // Example 4: Complex nested structure
        example4ComplexStructure();
    }

    private static void example1SimplePOJO() {
        System.out.println("Example 1: Simple POJO");
        System.out.println("======================");

        User user = new User(123, "Ada Lovelace", true);
        String toon = Toon.encode(user);

        System.out.println(toon);
        System.out.println();
    }

    private static void example2NestedPOJO() {
        System.out.println("Example 2: Nested POJO");
        System.out.println("======================");

        Address address = new Address(
            "42 Wallaby Way",
            "Sydney",
            "Australia",
            "2000"
        );

        Person person = new Person(
            "Alice Smith",
            30,
            address,
            List.of("reading", "hiking", "photography")
        );

        String toon = Toon.encode(person);
        System.out.println(toon);
        System.out.println();
    }

    private static void example3ListOfPOJOs() {
        System.out.println("Example 3: List of POJOs (Tabular Format)");
        System.out.println("==========================================");

        List<Product> products = List.of(
            new Product("SKU-001", "Widget", 150, 9.99, LocalDate.of(2025, 2, 15)),
            new Product("SKU-002", "Gadget", 75, 24.50, LocalDate.of(2025, 3, 1)),
            new Product("SKU-003", "Doohickey", 200, 5.75, LocalDate.of(2025, 2, 20))
        );

        String toon = Toon.encode(java.util.Map.of("products", products));
        System.out.println(toon);
        System.out.println();
    }

    private static void example4ComplexStructure() {
        System.out.println("Example 4: Complex Nested Structure");
        System.out.println("====================================");

        List<Product> orderProducts = List.of(
            new Product("A1", "Laptop", 1, 899.99, LocalDate.of(2025, 2, 10)),
            new Product("B2", "Mouse", 2, 29.99, LocalDate.of(2025, 2, 12)),
            new Product("C3", "Keyboard", 1, 79.99, LocalDate.of(2025, 2, 11))
        );

        Order order = new Order(
            1001,
            "Bob Johnson",
            orderProducts,
            1039.96
        );

        String toon = Toon.encode(order, EncodeOptions.verbose());
        System.out.println(toon);
        System.out.println();
    }
}
