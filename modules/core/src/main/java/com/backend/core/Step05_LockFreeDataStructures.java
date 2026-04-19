package com.backend.core;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Step 05: Lock-Free Data Structures (L7 Scalability)
 * 
 * L7 Principles:
 * 1. Lock Contention: Traditional 'synchronized' blocks fail under high concurreny.
 * 2. CPU Cache Lines: Understanding why 'LongAdder' is faster than 'AtomicLong' (Avoiding false sharing).
 * 3. Wait-Free Algorithms: Ensuring threads make progress without blocking each other.
 */
public class Step05_LockFreeDataStructures {

    public static void main(String[] args) {
        System.out.println("=== Step 05: Lock-Free Data Structures (Scaling Math) ===");

        // 1. AtomicLong: Good for low-to-medium contention.
        // Uses Compare-And-Swap (CAS) in a loop.
        AtomicLong counter = new AtomicLong(0);
        counter.incrementAndGet();
        System.out.println("AtomicLong value: " + counter.get());

        // 2. LongAdder: The L7 Industry Standard for high-write counters.
        // Maintains multiple cells to reduce contention, then sums them up on read.
        LongAdder highWriteCounter = new LongAdder();
        highWriteCounter.increment();
        highWriteCounter.add(10);
        
        System.out.println("LongAdder value: " + highWriteCounter.sum());
        
        System.out.println("\nL5 Insight: LongAdder avoids 'cache line bouncing' by striping the value.");
    }
}
