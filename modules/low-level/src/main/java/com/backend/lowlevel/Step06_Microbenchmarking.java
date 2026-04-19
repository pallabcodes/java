package com.backend.lowlevel;

/**
 * Step 06: Microbenchmarking & Performance Proof (L7 Standards)
 * 
 * L7 Principles:
 * 1. Benchmark Rigor: Why 'System.nanoTime()' loops are often wrong (JIT warm-up, dead code elimination).
 * 2. JMH (Java Microbenchmark Harness): The oracle of JVM performance.
 * 3. Warm-up vs Measurements: Separating steady-state performance from startup noise.
 */
public class Step06_Microbenchmarking {

    /**
     * Simulation of how an L7 engineer thinks about benchmarks.
     */
    public static void main(String[] args) {
        System.out.println("=== Step 06: Microbenchmarking (Performance Proofs) ===");

        long startTime = System.nanoTime();
        
        // Operation to measure
        long result = performComplexMath(10_000_000);
        
        long endTime = System.nanoTime();
        
        System.out.println("Result: " + result);
        System.out.println("Naive Measurement: " + (endTime - startTime) + " ns");

        System.out.println("\nL5 Caution: This naive benchmark ignores:");
        System.out.println("1. JIT Compilation: Code might be interpreted for the first 10k calls.");
        System.out.println("2. Dead Code Elimination: If 'result' isn't used, JVM might delete the loop.");
        System.out.println("3. Cache State: Cold vs Warm instruction caches.");
        
        System.out.println("\nL7 Tip: Always use JMH for critical performance paths at Google/Netflix.");
    }

    private static long performComplexMath(int iterations) {
        long sum = 0;
        for (int i = 0; i < iterations; i++) {
            sum += (i * 31);
        }
        return sum;
    }
}
