package com.backend.core.concurrency;

import java.util.ArrayList;
import java.util.List;

/**
 * Step 03: Memory Pressure & Contention (L7 Mastery)
 * 
 * CONCEPT:
 * In a high-throughput Java system, CPU isn't always the bottleneck—Memory Allocation is.
 * High allocation rates force the JVM's Garbage Collector (G1 GC/ZGC) to run constantly.
 * This triggers "Stop-The-World" pauses, resulting in unpredictable p99 tail latency spikes.
 * 
 * L7 AWARENESS:
 * 1. Object Lifetimes: Short-lived objects die in the Young Generation (cheap).
 *    Long-lived objects get promoted to the Old Generation (expensive to clean).
 * 2. Allocation Hotspots: Tight loops creating temporary arrays/strings are latency killers.
 */
public class Step03_MemoryPressureAndContention {

    /**
     * SIMULATION: A simple method that calculates something, but is incredibly wasteful with memory.
     * In a real app, this might be JSON parsing parsing or repetitive String concatenation.
     */
    public static void simulateAllocationSpike() {
        System.out.println("--- Simulating High Allocation Burst (GC Pressure) ---");
        long start = System.currentTimeMillis();

        // L7 ANTIPATTERN: Creating millions of temporary objects in a hot path.
        // This instantly fills the G1 Eden Space, forcing a Minor GC pause.
        for (int i = 0; i < 1_000_000; i++) {
            List<String> temporaryGarbage = new ArrayList<>(10);
            temporaryGarbage.add("Data-" + i);
            temporaryGarbage.add("MoreData-" + i);
        }

        long duration = System.currentTimeMillis() - start;
        System.out.println("Wasteful Allocation took: " + duration + "ms due to GC churn.");
    }

    /**
     * SIMULATION: The exact same logical outcome, but highly optimized to reuse memory.
     */
    public static void steadyStateExecution() {
        System.out.println("\n--- Simulating Steady State Execution ---");
        long start = System.currentTimeMillis();

        // L7 OPTIMIZATION: Object pooling or sizing arrays correctly.
        // Alternatively, using primitive arrays or avoiding instantiation entirely.
        String[] reusedArray = new String[2];
        for (int i = 0; i < 1_000_000; i++) {
            reusedArray[0] = "Data-" + i;
            reusedArray[1] = "MoreData-" + i;
            // Process data here...
        }

        long duration = System.currentTimeMillis() - start;
        System.out.println("Optimized Steady-State took: " + duration + "ms.");
    }

    public static void main(String[] args) {
        System.out.println("=== Step 03: Memory Contention & Allocation Rates ===");
        simulateAllocationSpike();
        steadyStateExecution();
        
        System.out.println("\n[L7 INSIGHT]: While Virtual Threads solve I/O blocking, they EXACERBATE Memory Pressure.");
        System.out.println("If 10,000 virtual threads all reach a memory-wasteful hot path simultaneously, ");
        System.out.println("the JVM will experience massive Allocation Rate spikes, leading to tail latency death.");
    }
}
