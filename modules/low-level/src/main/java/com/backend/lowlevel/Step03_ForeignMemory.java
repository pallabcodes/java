package com.backend.lowlevel;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

/**
 * Step 03: Foreign Memory (Panama API & Off-heap)
 * 
 * L7 Mastery:
 * 1. Off-heap Memory: Memory managed outside the JVM Heap (no GC tax).
 * 2. Arena: A deterministic lifecycle for memory segments (no manual 'free' errors).
 * 3. Modern Safety: Faster and safer than 'DirectByteBuffer' or 'Unsafe'.
 */
public class Step03_ForeignMemory {

    public static void main(String[] args) {
        System.out.println("=== Step 03: Foreign Memory (Panama API - Java 21) ===");

        // Using 'Arena.ofConfined()' ensure memory is freed when the block ends
        try (Arena arena = Arena.ofConfined()) {
            
            // Allocate 1GB off-heap
            long oneGB = 1024 * 1024 * 1024;
            MemorySegment segment = arena.allocate(oneGB);

            System.out.println("Allocated 1GB off-heap memory.");

            // Writing and reading values
            long offset = 0;
            segment.set(ValueLayout.JAVA_INT, offset, 777);
            
            int value = segment.get(ValueLayout.JAVA_INT, offset);
            System.out.println("Retrieved off-heap value: " + value);

        } // Memory is automatically freed here!

        System.out.println("Off-heap memory freed successfully.");
        System.out.println("\nL7 Tip: This is how infrastructure like Netflix Hollow managed massive in-memory datasets.");
    }
}
