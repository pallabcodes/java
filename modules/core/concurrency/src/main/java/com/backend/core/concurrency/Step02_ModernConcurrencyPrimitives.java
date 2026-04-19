package com.backend.core.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Step 02: Modern Concurrency Primitives (L7 Mastery)
 * 
 * CONCEPT:
 * As we scale out with Virtual Threads, blocking becomes cheap, but contention (fighting over the same memory) 
 * becomes the new bottleneck.
 * 
 * L7 AWARENESS:
 * 1. Synchronized vs ReentrantLock: With Loom, `ReentrantLock` allows unmounting, `synchronized` creates JVM pinning.
 * 2. AtomicLong vs LongAdder: Under heavy thread contention, `AtomicLong` causes CPU cache-line bouncing (CAS loop spinning).
 *    `LongAdder` distributes the counter across an array of cells, massively increasing throughput.
 */
public class Step02_ModernConcurrencyPrimitives {

    // L7 Anti-pattern for extreme concurrency: AtomicLong or Synchronized block
    // private AtomicLong legacyCounter = new AtomicLong(0);
    
    // L7 Standard: Scales linearly with threads because it avoids CAS contention
    private final LongAdder highThroughputCounter = new LongAdder();

    // L7 Standard for Virtual Threads: ReentrantLock
    private final ReentrantLock loomFriendlyLock = new ReentrantLock();

    public void recordHit() {
        // Highly scalable increment
        highThroughputCounter.increment();
    }

    public void processCriticalSection() {
        // Instead of `synchronized(this)`, we use explicit locking
        loomFriendlyLock.lock();
        try {
            // Simulated deep work that blocks (DB save, etc.)
            // Because we use ReentrantLock, if this is a Virtual Thread, it will 
            // gracefully park and unmount, allowing other threads to work.
            Thread.sleep(1); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // Crucial: Always unlock in a finally block
            loomFriendlyLock.unlock();
        }
    }

    public static void runSimulation() throws InterruptedException {
        System.out.println("=== Step 02: Modern Scalable Primitives ===");
        
        Step02_ModernConcurrencyPrimitives server = new Step02_ModernConcurrencyPrimitives();
        
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            System.out.println("Launching 50,000 requests to hit the LongAdder counter...");
            for (int i = 0; i < 50_000; i++) {
                executor.submit(server::recordHit);
            }
        } // Implicitly awaits termination
        
        System.out.println("Final Counter Value: " + server.highThroughputCounter.sum());
        System.out.println("[L7 ACHIEVEMENT]: LongAdder prevented cache-line invalidation storms across CPU cores.");
    }

    public static void main(String[] args) throws InterruptedException {
        runSimulation();
    }
}
