package com.backend.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Production-Grade Data Processing Service
 * 
 * Demonstrates Netflix SDE-2 collection framework expertise:
 * - Advanced HashMap operations and iterations
 * - Optional usage patterns for null safety
 * - Stream operations and functional programming
 * - Collection framework best practices
 * - Thread-safe collections for production
 */
@Service
public class DataProcessingService {

    // Production-grade: Thread-safe collections for concurrent access
    private final Map<String, List<Integer>> dataCache = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> uniqueValuesCache = new ConcurrentHashMap<>();
    private final Map<String, Queue<String>> processingQueue = new ConcurrentHashMap<>();

    public DataProcessingService() {
        initializeDataStructures();
    }

    /**
     * Process data using HashMap operations and Streams
     * 
     * @param data input data to process
     * @return processed results with collection operations
     */
    public Map<String, Object> processDataWithCollections(List<String> data) {
        // Production-grade: HashMap operations
        Map<String, Object> results = new HashMap<>();
        
        if (data == null || data.isEmpty()) {
            return results;
        }

        // Demonstrate HashMap iteration patterns
        data.forEach(item -> {
            String key = generateKey(item);
            results.put(key, processItem(item));
        });

        // Using Streams for data transformation
        Map<String, Long> frequencyMap = data.stream()
                .collect(Collectors.groupingBy(
                    Function.identity(),
                    Collectors.counting()
                ));

        // HashMap iteration using entrySet
        frequencyMap.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .forEach(entry -> 
                    results.put("duplicate_" + entry.getKey(), entry.getValue()));

        return results;
    }

    /**
     * Advanced HashMap operations with Optional patterns
     * 
     * @param key the key to process
     * @return processed result using Optional
     */
    public Optional<Object> getProcessedDataWithOptional(String key) {
        // Production-grade: Optional usage for null safety
        return Optional.ofNullable(dataCache.get(key))
                .map(this::processListWithStreams)
                .map(result -> {
                    Map<String, Object> processed = new HashMap<>();
                    processed.put("key", key);
                    processed.put("result", result);
                    processed.put("timestamp", System.currentTimeMillis());
                    return processed;
                });
    }

    /**
     * Process list using Streams and collection operations
     * 
     * @param data list to process
     * @return processed result
     */
    private Object processListWithStreams(List<Integer> data) {
        if (data == null || data.isEmpty()) {
            return Collections.emptyList();
        }

        // Production-grade: Stream operations on collections
        return data.stream()
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .summaryStatistics();
    }

    /**
     * Demonstrate HashMap iteration patterns
     * 
     * @return HashMap iteration examples
     */
    public Map<String, String> demonstrateHashMapIterations() {
        Map<String, String> examples = new HashMap<>();
        
        // Method 1: Traditional for-each with entrySet
        for (Map.Entry<String, List<Integer>> entry : dataCache.entrySet()) {
            examples.put("method1_" + entry.getKey(), 
                String.valueOf(entry.getValue().size()));
        }

        // Method 2: Using forEach with lambda
        dataCache.forEach((key, value) -> 
            examples.put("method2_" + key, String.valueOf(value.size())));

        // Method 3: Using keySet iteration
        for (String key : dataCache.keySet()) {
            examples.put("method3_" + key, 
                String.valueOf(dataCache.get(key).size()));
        }

        // Method 4: Using values iteration
        dataCache.values().stream()
                .mapToInt(List::size)
                .forEach(size -> 
                    examples.put("method4_size_" + size, String.valueOf(size)));

        return examples;
    }

    /**
     * Advanced collection operations with Streams
     * 
     * @return advanced collection operations
     */
    public Map<String, Object> demonstrateAdvancedCollections() {
        Map<String, Object> results = new HashMap<>();

        // Using Streams for data aggregation
        IntSummaryStatistics stats = dataCache.values().stream()
                .flatMap(List::stream)
                .mapToInt(Integer::intValue)
                .summaryStatistics();

        results.put("totalCount", stats.getCount());
        results.put("sum", stats.getSum());
        results.put("average", stats.getAverage());
        results.put("min", stats.getMin());
        results.put("max", stats.getMax());

        // Using Optional for safe operations
        Optional<Map.Entry<String, List<Integer>>> maxEntry = dataCache.entrySet().stream()
                .max(Comparator.comparing(entry -> entry.getValue().size()));

        maxEntry.ifPresent(entry -> {
            results.put("largestDataset", entry.getKey());
            results.put("largestDatasetSize", entry.getValue().size());
        });

        // Collection framework operations
        Set<String> allKeys = new HashSet<>(dataCache.keySet());
        results.put("uniqueKeys", allKeys.size());
        results.put("allKeys", allKeys);

        return results;
    }

    /**
     * Queue operations with collection framework
     * 
     * @param queueName name of the queue
     * @param items items to add
     * @return queue status
     */
    public Map<String, Object> processQueueWithCollections(String queueName, List<String> items) {
        Map<String, Object> result = new HashMap<>();
        
        // Production-grade: Queue operations
        Queue<String> queue = processingQueue.computeIfAbsent(queueName, k -> new LinkedList<>());
        
        // Using Streams for bulk operations
        items.stream()
                .filter(Objects::nonNull)
                .forEach(queue::offer);

        result.put("queueName", queueName);
        result.put("queueSize", queue.size());
        result.put("itemsAdded", items.size());
        result.put("timestamp", System.currentTimeMillis());

        return result;
    }

    /**
     * Set operations with collection framework
     * 
     * @param setKey key for the set
     * @param values values to add
     * @return set operations result
     */
    public Map<String, Object> processSetWithCollections(String setKey, Collection<String> values) {
        Map<String, Object> result = new HashMap<>();
        
        // Production-grade: Set operations
        Set<String> set = uniqueValuesCache.computeIfAbsent(setKey, k -> new HashSet<>());
        
        int beforeSize = set.size();
        set.addAll(values);
        int afterSize = set.size();
        int newItems = afterSize - beforeSize;

        result.put("setKey", setKey);
        result.put("beforeSize", beforeSize);
        result.put("afterSize", afterSize);
        result.put("newItems", newItems);
        result.put("totalUniqueItems", set.size());

        return result;
    }

    /**
     * Initialize data structures with collection operations
     */
    private void initializeDataStructures() {
        // Production-grade: HashMap initialization
        Map<String, List<Integer>> initialData = new HashMap<>();
        initialData.put("dataset1", Arrays.asList(1, 2, 3, 4, 5));
        initialData.put("dataset2", Arrays.asList(10, 20, 30, 40, 50));
        initialData.put("dataset3", Arrays.asList(100, 200, 300, 400, 500));

        // Using Streams for bulk operations
        initialData.entrySet().stream()
                .forEach(entry -> dataCache.put(entry.getKey(), entry.getValue()));

        // Initialize unique values cache
        Set<String> uniqueValues = new HashSet<>();
        uniqueValues.add("value1");
        uniqueValues.add("value2");
        uniqueValues.add("value3");
        uniqueValuesCache.put("default", uniqueValues);
    }

    /**
     * Generate key for data processing
     * 
     * @param item input item
     * @return generated key
     */
    private String generateKey(String item) {
        return Optional.ofNullable(item)
                .map(String::toLowerCase)
                .map(s -> s.replaceAll("\\s+", "_"))
                .orElse("unknown");
    }

    /**
     * Process individual item
     * 
     * @param item item to process
     * @return processed result
     */
    private Object processItem(String item) {
        return Optional.ofNullable(item)
                .map(String::length)
                .map(length -> length * 2)
                .orElse(0);
    }
}
