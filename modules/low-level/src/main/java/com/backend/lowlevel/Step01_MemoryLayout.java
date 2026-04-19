package com.backend.lowlevel;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Step 01: CPU Cache & False Sharing (Contended API)
 * 
 * L7 Mastery:
 * 1. CPU Cache Lines: Processors fetch data in 64-byte chunks (cache lines).
 * 2. False Sharing: Two threads updating different variables on the same cache line 
 *    cause "Cache Line Invalidation," killing performance.
 * 3. @Contended: A JVM hint to pad variables to prevent false sharing.
 */
public class Step01_MemoryLayout {

    // ⛔ Performance Killer: These two variables will likely share the same cache line.
    // When thread A updates 'a', thread B's cache copy of 'b' is invalidated.
    public static class ContendedData {
        public volatile long a = 0;
        public volatile long b = 0;
    }

    // ✅ L7 / High Throughput Way: Use padding or @Contended (requires JVM flag -XX:-RestrictContended)
    // For this demo, we'll simulate padding by adding dummy 'long' fields to separate 'a' and 'b'.
    public static class PaddedData {
        public volatile long a = 0;
        public long p1, p2, p3, p4, p5, p6, p7, p8; // Padding to fill 64 bytes
        public volatile long b = 0;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Step 01: Memory Layout & False Sharing (L7 Benchmarking) ===");

        long iterations = 50_000_000L;

        // Test Contended
        ContendedData contended = new ContendedData();
        runBenchmark(iterations, () -> contended.a++, () -> contended.b++, "UNPADDED (False Sharing)");

        // Test Padded
        PaddedData padded = new PaddedData();
        runBenchmark(iterations, () -> padded.a++, () -> padded.b++, "PADDED (Isolated)");
        
        System.out.println("\nL7 Tip: In Java 8+, use @jdk.internal.vm.annotation.Contended to automate this.");
    }

    private static void runBenchmark(long iterations, Runnable r1, Runnable r2, String label) throws InterruptedException {
        Thread t1 = new Thread(() -> { for (long i = 0; i < iterations; i++) r1.run(); });
        Thread t2 = new Thread(() -> { for (long i = 0; i < iterations; i++) r2.run(); });

        long start = System.nanoTime();
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        long end = System.nanoTime();

        System.out.printf("%s took: %.2f ms\n", label, (end - start) / 1_000_000.0);
    }
}
