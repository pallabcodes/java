package com.backend.core.concurrency;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Step 01: Virtual Thread Mechanics & Pinning (L7 Mastery)
 * 
 * CONCEPT:
 * Java 21 LTS brings Project Loom. Virtual threads are cheap, M:N mapped threads.
 * Millions of virtual threads run on top of a few OS "Carrier Threads".
 * 
 * L7 AWARENESS (The "Gotchas"):
 * 1. Blocking I/O is cooperative. The virtual thread "unmounts" from the carrier thread while waiting.
 * 2. PINNING: If a virtual thread blocks while inside a `synchronized` block or a native C call,
 *    it CANNOT unmount. It "pins" the carrier thread, severely degrading scalability.
 */
public class Step01_VirtualThreadMechanics {

    public static void demonstrateCooperativeBlocking() {
        System.out.println("--- Demonstrating Cooperative Blocking ---");
        
        // We create an executor that uses virtual threads exclusively
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            AtomicInteger completed = new AtomicInteger();
            
            // Launch 10,000 tasks. Doing this with cached OS threads would likely crash the JVM or OS.
            for (int i = 0; i < 10_000; i++) {
                executor.submit(() -> {
                    try {
                        // Sleeping/Blocking I/O allows the thread to UNMOUNT.
                        // The underlying OS Carrier thread is instantly free to do other work.
                        Thread.sleep(Duration.ofMillis(10));
                        completed.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        } // Executor blocks here until all 10,000 tasks complete natively
        
        System.out.println("Successfully ran 10,000 virtual threads concurrently.");
    }

    public static void demonstrateThreadPinningRisk() {
        System.out.println("\n--- Demonstrating Thread Pinning Risk (L7 Architecture) ---");
        System.out.println("[WARNING] Avoid 'synchronized' blocks around network/disk I/O when using Virtual Threads.");
        
        Object legacyLock = new Object();
        
        Runnable pinnedTask = () -> {
            // L7 ANTIPATTERN: Synchronizing across a blocking call pins the carrier thread!
            synchronized (legacyLock) {
                try {
                    // Because we hold a monitor lock, the JVM cannot unmount this virtual thread.
                    // The underlying OS thread is now BLOCKED. If you do this across 100 threads,
                    // your entire thread pool starves and the system grinds to a halt.
                    Thread.sleep(Duration.ofMillis(20));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            // Firing a few to demonstrate intent (won't crash here since it's sequential-ish by the loop, 
            // but under heavy parallel load, this is a disaster).
            for (int i = 0; i < 5; i++) {
                executor.submit(pinnedTask);
            }
        }
        
        System.out.println("L7 FIX: Replace `synchronized` with `java.util.concurrent.locks.ReentrantLock`.");
        System.out.println("ReentrantLock allows the virtual thread to gracefully unmount when contested.");
    }

    public static void main(String[] args) {
        demonstrateCooperativeBlocking();
        demonstrateThreadPinningRisk();
    }
}
