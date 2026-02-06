package com.backend.designpatterns.behavioral.chain_of_responsibility;

public class ChainDemo {

    public static void main(String[] args) {
        System.out.println("--- Chain of Responsibility Demo ---");

        // Use Case: Use Chain of Responsibility when you want to pass a request along a chain of handlers. 
        // Each handler decides either to process the request or to pass it to the next handler.

        Handler chain = new LoggingHandler();
        chain.setNext(new AuthHandler())
             .setNext(new BusinessHandler());

        // 1. Successful Request
        System.out.println("\n[Request 1]");
        chain.handle(new Request("Alice", true, "Order #123"));

        // 2. Unauthorized Request
        System.out.println("\n[Request 2]");
        chain.handle(new Request("Attacker", false, "Malicious Data"));
    }
}
