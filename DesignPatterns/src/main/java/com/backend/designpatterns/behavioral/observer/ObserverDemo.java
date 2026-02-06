package com.backend.designpatterns.behavioral.observer;

public class ObserverDemo {

    public static void main(String[] args) {
        System.out.println("--- Observer Pattern Demo ---");

        // Use Case: Use Observer when changes to the state of one object may require 
        // changing other objects, and the actual set of objects is unknown or changes dynamically.

        OrderPublisher publisher = new OrderPublisher();
        
        publisher.subscribe(new EmailListener());
        publisher.subscribe(new InventoryListener());

        publisher.publishOrderCreated(new Order("ORD-001", 100.0));
    }
}
