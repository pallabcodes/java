package com.backend.architecture;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * L7 Design Primitives (Core Java Mastery)
 * 
 * L7 Principles:
 * 1. Resource Reuse: The Producer-Consumer pattern for decoupling throughput.
 * 2. Caching Strategy: LEast Recently Used (LRU) for memory-bounded local storage.
 * 3. Contract-First Design: Using standard Java primitives (BlockingQueue, LinkedHashMap) 
 *    before reaching for 3rd party libraries.
 */
public class DesignPrimitives {

    /**
     * L7 LRU Cache implementation using LinkedHashMap.
     * Inherently handles eviction based on access order and size limit.
     */
    public static class LruCache<K, V> extends LinkedHashMap<K, V> {
        private final int capacity;

        public LruCache(int capacity) {
            super(capacity, 0.75f, true); // true = access-order
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacity;
        }
    }

    public static void runProducerConsumerDemo() throws InterruptedException {
        System.out.println("--- Producer-Consumer Primitive ---");
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(5);

        // Simple Producer (Anonymous Runnable)
        Runnable producer = () -> {
            try {
                queue.put("EVENT_LOG_A");
                System.out.println("[Producer] Pushed event.");
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        };

        // Simple Consumer
        Runnable consumer = () -> {
            try {
                String event = queue.take();
                System.out.println("[Consumer] Processed: " + event);
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        };

        new Thread(producer).start();
        new Thread(consumer).start();
        
        Thread.sleep(100);
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== L7 Design Primitives (Systems Patterns) ===");

        // 1. LRU Demo
        LruCache<String, Integer> cache = new LruCache<>(2);
        cache.put("A", 1);
        cache.put("B", 2);
        cache.get("A"); // Touch A
        cache.put("C", 3); // B is eldest and should be evicted

        System.out.println("Cache Contents: " + cache);
        System.out.println("L5 Insight: 'B' was evicted because 'A' was accessed more recently.");

        // 2. Queue Demo
        runProducerConsumerDemo();
    }
}
