1️⃣ Real-World Example — Order Service Events
Scenario

E-commerce order created → multiple systems react:

Email service

Inventory

Analytics

Shipping

Without Observer → hard-coded calls.

import java.util.*;

// ==========================
// Domain Model
// ==========================
class Order {
    String id;
    double amount;

    Order(String id, double amount) {
        this.id = id;
        this.amount = amount;
    }
}

// ==========================
// Observer Contract
// ==========================
interface OrderListener {
    void onOrderCreated(Order order);
}

// ==========================
// Publisher / Subject
// ==========================
class OrderPublisher {

    private final List<OrderListener> listeners = new ArrayList<>();

    public void subscribe(OrderListener listener) {
        listeners.add(listener);
    }

    public void unsubscribe(OrderListener listener) {
        listeners.remove(listener);
    }

    public void publishOrderCreated(Order order) {
        System.out.println("\nPublishing order: " + order.id);

        for (OrderListener l : listeners) {
            l.onOrderCreated(order);
        }
    }
}

// ==========================
// Concrete Observers
// ==========================

class EmailService implements OrderListener {

    @Override
    public void onOrderCreated(Order order) {
        sendEmail(order);
    }

    private void sendEmail(Order order) {
        System.out.println("📧 Email sent for order " + order.id);
    }
}

class InventoryService implements OrderListener {

    @Override
    public void onOrderCreated(Order order) {
        reserveStock(order);
    }

    private void reserveStock(Order order) {
        System.out.println("📦 Inventory reserved for " + order.id);
    }
}

class AnalyticsService implements OrderListener {

    @Override
    public void onOrderCreated(Order order) {
        track(order);
    }

    private void track(Order order) {
        System.out.println("📊 Analytics tracked order " + order.id);
    }
}

// ==========================
// App
// ==========================
public class App {

    public static void main(String[] args) {

        OrderPublisher publisher = new OrderPublisher();

        publisher.subscribe(new EmailService());
        publisher.subscribe(new InventoryService());
        publisher.subscribe(new AnalyticsService());

        publisher.publishOrderCreated(new Order("ORD-1", 5000));
    }
}


We added 3 subscribers to ArrayList
Publisher loops → calls same method
Works because of shared interface

This is the essence of Observer: One contract/interface, Many reactions