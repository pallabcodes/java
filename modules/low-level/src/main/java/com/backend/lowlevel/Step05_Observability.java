package com.backend.lowlevel;

import jdk.jfr.*;
import java.time.Duration;

/**
 * Step 05: Observability & Profiling (Custom JFR Events)
 * 
 * L7 Mastery:
 * 1. Zero-Overhead Telemetry: JFR events are faster than logging and can be recorded 24/7.
 * 2. Structured Diagnostics: Events contain typed data (OrderID, Latency) for deep analysis.
 * 3. Domain-Aware: Connecting business logic to JVM internals (GC, Safepoints).
 */
public class Step05_Observability {

    // L7: Define a custom event for the business domain
    @Label("Order Transaction")
    @Description("Tracks the latency and success of financial orders")
    @Category({"Finance", "Transactions"})
    @StackTrace(false) // Stack traces are expensive; disable for high-frequency events
    static class TransactionEvent extends Event {
        @Label("Order ID")
        String orderId;

        @Label("Amount")
        double amount;

        @Label("Currency")
        String currency;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Step 05: Observability (Custom JFR Events) ===");

        // Simulate a system with continuous profiling
        System.out.println("Recording custom business events...");

        for (int i = 0; i < 5; i++) {
            processOrder("ORD-" + i, Math.random() * 1000);
        }

        System.out.println("Work complete. Analysis would typically happen in JDK Mission Control.");
        System.out.println("\nL7 Tip: JFR events are binary and structured. They beat text logs for p99 analysis.");
    }

    private static void processOrder(String id, double amount) {
        TransactionEvent event = new TransactionEvent();
        event.orderId = id;
        event.amount = amount;
        event.currency = "USD";

        event.begin(); // Mark start time
        try {
            // Simulate processing time
            Thread.sleep(Duration.ofMillis((long) (Math.random() * 50)));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            event.end(); // Mark end time
            event.commit(); // Write to the buffer
            System.out.printf("Processed %s ($%.2f)\n", id, amount);
        }
    }
}
