package com.backend.lowlevel;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * Step 02: High-Performance Concurrency (Virtual Threads & Pining)
 * 
 * L7 Mastery:
 * 1. Virtual Threads: Lightweight threads that aren't tied 1:1 to OS threads.
 * 2. Scalability: Running 1,000,000 threads with MBs of RAM, not GBs.
 * 3. Thread Pining: When a Virtual Thread hits a 'synchronized' block, 
 *    it "pins" the carrier OS thread, preventing other virtual threads from running.
 */
public class Step02_VirtualThreads {

    public static void main(String[] args) {
        System.out.println("=== Step 02: Virtual Threads & Thread Pining (Java 21) ===");

        long startTime = System.currentTimeMillis();

        // Using Virtual Thread Executor
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, 10_000).forEach(i -> {
                executor.submit(() -> {
                    // Simulating a network call (I/O)
                    try {
                        Thread.sleep(Duration.ofMillis(100));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            });
        } // Executor close() waits for all threads to finish

        long duration = System.currentTimeMillis() - startTime;
        System.out.printf("Processed 10,000 requests in: %d ms\n", duration);
        
        System.out.println("\nL7 Caution: Thread Pining occurs in 'synchronized' blocks.");
        System.out.println("L5 Fix: Replace 'synchronized' with 'ReentrantLock' to allow unmounting.");
    }
}
