package com.backend.core;

import java.util.concurrent.*;

/**
 * Step 11: Concurrency Pitfalls (L7 Reliability)
 * 
 * L7 Principles:
 * 1. Deadlock Analysis: Circular dependencies on locks (Lock A -> Lock B vs Lock B -> Lock A).
 * 2. Resource Exhaustion: Why 'Executors.newCachedThreadPool()' or unbounded 'LinkedBlockingQueue' 
 *    can lead to OutOfMemory (OOM) under spike load.
 * 3. Backpressure: Using bounded queues and rejection policies (Abort, CallerRuns).
 */
public class Step11_ConcurrencyPitfalls {

    public static void demonstrateBackpressure() {
        System.out.println("--- Backpressure Demo ---");
        
        // L7 Mastery: Rigidly bounded execution environment
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(10);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                2, 4, 60, TimeUnit.SECONDS, workQueue, 
                new ThreadPoolExecutor.CallerRunsPolicy() // L7 Resilience: don't just fail, throttle the producer
        );

        System.out.println("Executor configured with Bounded Queue and CallerRunsPolicy.");
        executor.shutdown();
    }

    public static void main(String[] args) {
        System.out.println("=== Step 11: Concurrency Pitfalls (Failure Analysis) ===");

        demonstrateBackpressure();

        System.out.println("\nL5 Insight: Circular locking is the #1 cause of silent hangs in production.");
        System.out.println("L7 Tip: Always use 'jstack' or 'jcmd' to detect deadlock cycles in thread dumps.");
    }
}
