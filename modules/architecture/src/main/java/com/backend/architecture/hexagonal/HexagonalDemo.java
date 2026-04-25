package com.backend.architecture.hexagonal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * L7 Mastery: Hexagonal Architecture (Ports & Adapters)
 * 
 * Goal: Decouple the Core Domain from external side effects (DB, UI, API).
 */

// --- 1. CORE DOMAIN (The "Hexagon" Center) ---
record Order(String id, String product, double amount, String status) {}

// --- 2. OUTPUT PORTS (Interfaces for Side Effects) ---
interface OrderRepository {
    void save(Order order);
    Optional<Order> findById(String id);
}

// --- 3. APPLICATION SERVICE (The Interactor / Input Port) ---
class OrderService {
    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public Order createOrder(String product, double amount) {
        Order order = new Order(UUID.randomUUID().toString(), product, amount, "CREATED");
        repository.save(order);
        System.out.println("[Domain] Order created: " + order.id());
        return order;
    }
}

// --- 4. ADAPTER (Implementation of an Output Port) ---
class InMemoryOrderRepository implements OrderRepository {
    private final List<Order> store = new ArrayList<>();

    @Override
    public void save(Order order) {
        store.add(order);
        System.out.println("[Adapter] Order saved to In-Memory store.");
    }

    @Override
    public Optional<Order> findById(String id) {
        return store.stream().filter(o -> o.id().equals(id)).findFirst();
    }
}

// --- 5. DEMO (The Driver) ---
public class HexagonalDemo {
    public static void main(String[] args) {
        System.out.println("=== Hexagonal Architecture Demo (Ports & Adapters) ===");

        // Dependency Injection (Wiring the Adapter to the Port)
        OrderRepository repository = new InMemoryOrderRepository();
        OrderService orderService = new OrderService(repository);

        // Application logic in action
        orderService.createOrder("L7_SYSTEM_ENGINEER_CERT", 499.0);

        System.out.println("\nL7 Mastery: Notice how OrderService only knows about the 'OrderRepository' interface.");
        System.out.println("We can swap InMemory with PostgreSQL or a FileSystem adapter without touching the business logic.");
    }
}
