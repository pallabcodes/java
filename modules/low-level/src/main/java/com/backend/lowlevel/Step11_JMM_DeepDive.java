package com.backend.lowlevel;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Step 11: Java Memory Model (JMM) Deep Dive (L7 Physical reasoning)
 * 
 * L7 Principles:
 * 1. Happens-Before Relationship: Documentation of memory visibility across threads.
 * 2. Reordering: Why the CPU/Compiler may swap instructions if they appear independent.
 * 3. volatile vs atomic: volatile = visibility/ordering; atomic = visibility/ordering + atomicity.
 * 4. Lock Elision: How the JIT compiler removes unnecessary locks.
 */
public class Step11_JMM_DeepDive {

    private static volatile boolean ready = false;
    private static int data = 0;

    /**
     * L7 Simulation of JMM Guarantees.
     * Thread A writes 'data' then 'ready'.
     * Thread B reads 'ready' then 'data'.
     * 'volatile' on 'ready' creates a Happens-Before barrier.
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Step 11: JMM Deep Dive (Visibility & Barriers) ===");

        Thread writer = new Thread(() -> {
            data = 42;             // Step 1
            ready = true;          // Step 2 (Volatile Write - Happens-Before barrier)
            System.out.println("[Writer] Data set and ready Signal sent.");
        });

        Thread reader = new Thread(() -> {
            while (!ready) {       // Step 3 (Volatile Read - Wait for barrier)
                Thread.onSpinWait(); 
            }
            // Step 4: JMM Guarantees that because we saw 'ready=true', 
            // we MUST also see the update to 'data'.
            System.out.println("[Reader] Read Data: " + data);
        });

        reader.start();
        writer.start();

        reader.join();
        writer.join();

        System.out.println("\nL5 Insight: Without 'volatile', the Reader might spin forever or see 'data=0'.");
        System.out.println("L7 Tip: JMM prevents 'out-of-thin-air' values but doesn't prevent race conditions on non-atomic increments.");
    }
}
