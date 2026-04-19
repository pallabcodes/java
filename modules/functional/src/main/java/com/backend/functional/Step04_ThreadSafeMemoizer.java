package com.backend.functional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Step 04: Thread-Safe Memoization
 * 
 * L5 Principles:
 * 1. Efficiency: Caches expensive computations for immediate reuse.
 * 2. Thread-safety: Uses ConcurrentHashMap to handle multi-threaded access.
 * 3. HOF (Higher Order Function): A function that takes a function and returns a cached version.
 */
public class Step04_ThreadSafeMemoizer {

    /**
     * L5 Generic Memoizer
     * Transforms any Function<T, R> into a cached version.
     */
    public static <T, R> Function<T, R> memoize(Function<T, R> function) {
        Map<T, R> cache = new ConcurrentHashMap<>();
        return input -> cache.computeIfAbsent(input, function);
    }

    public static void main(String[] args) {
        System.out.println("=== Step 04: Thread-Safe Memoization ===");

        // Expensive Google Cloud IAM check simulation
        Function<String, Boolean> expensiveIamCheck = userId -> {
            System.out.println("Cloud IAM: Verifying permissions for " + userId + " (takes 1s)...");
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            return userId.startsWith("ADMIN");
        };

        // Wrap with memoizer
        Function<String, Boolean> cachedIamCheck = memoize(expensiveIamCheck);

        // First call (Slow)
        long start = System.currentTimeMillis();
        System.out.println("Result 1: " + cachedIamCheck.apply("ADMIN_123"));
        System.out.println("Time taken: " + (System.currentTimeMillis() - start) + "ms");

        // Second call (Instant)
        start = System.currentTimeMillis();
        System.out.println("Result 2: " + cachedIamCheck.apply("ADMIN_123"));
        System.out.println("Time taken: " + (System.currentTimeMillis() - start) + "ms");

        // Different user (Slow again)
        start = System.currentTimeMillis();
        System.out.println("Result 3: " + cachedIamCheck.apply("USER_456"));
        System.out.println("Time taken: " + (System.currentTimeMillis() - start) + "ms");
    }
}
