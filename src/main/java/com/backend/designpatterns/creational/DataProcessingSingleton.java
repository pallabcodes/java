package com.backend.designpatterns.creational;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Netflix Production-Grade Singleton Pattern Implementation
 * 
 * Demonstrates Netflix SDE-2 design pattern expertise:
 * - Singleton pattern with thread safety
 * - Advanced HashMap operations and iterations
 * - Stream operations for data processing
 * - Optional usage for null safety
 * - Collection framework best practices
 * - Thread-safe collections for production
 * 
 * @author Netflix Backend Team
 * @version 1.0.0
 */
@Component
public class DataProcessingSingleton {

    // Production-grade: Thread-safe singleton instance with double-checked locking
    private static volatile DataProcessingSingleton instance;
    
    // Production-grade: Thread-safe collections for concurrent access
    private final Map<String, Object> dataCache = new ConcurrentHashMap<>();
    private final Map<String, Object> processingMetrics = new ConcurrentHashMap<>();
    private final AtomicInteger requestCounter = new AtomicInteger(0);
    
    // Production-grade: Singleton configuration
    private static final String SINGLETON_VERSION = "2.0.0";
    private static final int MAX_CACHE_SIZE = 2000;
    
    // Production-grade: Private constructor for singleton
    private DataProcessingSingleton() {
        initializeSingleton();
    }
    
    /**
     * Thread-safe singleton getInstance with double-checked locking
     * 
     * @return DataProcessingSingleton instance
     */
    public static DataProcessingSingleton getInstance() {
        if (instance == null) {
            synchronized (DataProcessingSingleton.class) {
                if (instance == null) {
                    instance = new DataProcessingSingleton();
                }
            }
        }
        return instance;
    }
    
    /**
     * Initialize singleton with HashMap operations
     */
    private void initializeSingleton() {
        // Production-grade: Initialization with HashMap
        processingMetrics.put("singletonCreated", LocalDateTime.now());
        processingMetrics.put("version", SINGLETON_VERSION);
        processingMetrics.put("maxCacheSize", MAX_CACHE_SIZE);
        processingMetrics.put("initializationComplete", true);
        
        // Initialize data cache with sample data
        Map<String, Object> initialData = new HashMap<>();
        initialData.put("singleton_id", UUID.randomUUID().toString());
        initialData.put("startup_time", System.currentTimeMillis());
        initialData.put("status", "INITIALIZED");
        
        // Using Streams for bulk initialization
        initialData.entrySet().stream()
                .forEach(entry -> dataCache.put(entry.getKey(), entry.getValue()));
    }
    
    /**
     * Process data using singleton with HashMap operations
     * 
     * @param data input data to process
     * @return processing results
     */
    public Map<String, Object> processData(List<String> data) {
        int requestId = requestCounter.incrementAndGet();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Production-grade: Data processing with HashMap operations
            Map<String, Object> processedData = performDataProcessing(data);
            
            // Method 1: Using entrySet iteration for result enhancement
            for (Map.Entry<String, Object> entry : processedData.entrySet()) {
                result.put("processed_" + entry.getKey(), entry.getValue());
            }
            
            // Method 2: Using Streams for data analysis
            Map<String, Long> frequencyAnalysis = data.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.groupingBy(
                        String::toLowerCase,
                        Collectors.counting()
                    ));
            
            result.put("frequencyAnalysis", frequencyAnalysis);
            result.put("uniqueItems", frequencyAnalysis.size());
            
            // Method 3: Using forEach with lambda for metadata
            Map<String, Object> metadata = new HashMap<>();
            data.forEach(item -> {
                if (item != null) {
                    metadata.put("item_" + item.hashCode(), 
                        Map.of("length", item.length(), "processed", true));
                }
            });
            
            result.put("itemMetadata", metadata);
            
            // Update processing metrics using HashMap operations
            updateProcessingMetrics(requestId, System.currentTimeMillis() - startTime, data.size());
            
            result.put("requestId", requestId);
            result.put("processingTime", System.currentTimeMillis() - startTime);
            result.put("timestamp", LocalDateTime.now());
            result.put("singletonInstance", this.hashCode());
            
        } catch (Exception e) {
            result.put("error", "Data processing failed: " + e.getMessage());
            result.put("status", "FAILED");
        }
        
        return result;
    }
    
    /**
     * Perform data processing using HashMap operations
     * 
     * @param data input data
     * @return processed results
     */
    private Map<String, Object> performDataProcessing(List<String> data) {
        Map<String, Object> processed = new HashMap<>();
        
        // Production-grade: Data processing with HashMap
        processed.put("totalItems", data.size());
        processed.put("processingMode", "SINGLETON");
        
        // Using Streams for data transformation
        Map<String, List<String>> categorizedData = data.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                    this::categorizeDataItem,
                    Collectors.toList()
                ));
        
        processed.put("categorizedData", categorizedData);
        
        // Using HashMap for item analysis
        Map<String, Object> itemAnalysis = new HashMap<>();
        data.forEach(item -> {
            if (item != null) {
                String category = categorizeDataItem(item);
                if (!itemAnalysis.containsKey(category)) {
                    itemAnalysis.put(category, new ArrayList<String>());
                }
                ((List<String>) itemAnalysis.get(category)).add(item);
            }
        });
        
        processed.put("itemAnalysis", itemAnalysis);
        
        return processed;
    }
    
    /**
     * Categorize data item using HashMap operations
     * 
     * @param item item to categorize
     * @return category
     */
    private String categorizeDataItem(String item) {
        // Production-grade: Item categorization with HashMap
        Map<String, String> categoryMappings = new HashMap<>();
        categoryMappings.put("netflix", "streaming");
        categoryMappings.put("production", "enterprise");
        categoryMappings.put("grade", "quality");
        categoryMappings.put("design", "architecture");
        categoryMappings.put("patterns", "structure");
        categoryMappings.put("singleton", "creational");
        categoryMappings.put("hashmap", "collections");
        categoryMappings.put("stream", "functional");
        categoryMappings.put("optional", "null_safety");
        
        // Method 1: Using entrySet iteration
        for (Map.Entry<String, String> entry : categoryMappings.entrySet()) {
            if (item.toLowerCase().contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        return "general";
    }
    
    /**
     * Update processing metrics using HashMap operations
     * 
     * @param requestId request identifier
     * @param processingTime processing time in milliseconds
     * @param itemCount number of items processed
     */
    private void updateProcessingMetrics(int requestId, long processingTime, int itemCount) {
        // Production-grade: Metrics tracking with HashMap
        String metricsKey = "request_" + requestId;
        Map<String, Object> requestMetrics = new HashMap<>();
        
        requestMetrics.put("processingTime", processingTime);
        requestMetrics.put("itemCount", itemCount);
        requestMetrics.put("timestamp", LocalDateTime.now());
        requestMetrics.put("cacheSize", dataCache.size());
        
        processingMetrics.put(metricsKey, requestMetrics);
        
        // Update aggregate metrics using HashMap operations
        updateAggregateMetrics(processingTime, itemCount);
        
        // Cache size management using Streams
        manageCacheSize();
    }
    
    /**
     * Update aggregate metrics using HashMap operations
     * 
     * @param processingTime processing time in milliseconds
     * @param itemCount number of items processed
     */
    private void updateAggregateMetrics(long processingTime, int itemCount) {
        // Production-grade: Aggregate metrics using HashMap
        Long totalProcessingTime = (Long) processingMetrics.getOrDefault("totalProcessingTime", 0L);
        Integer totalItemsProcessed = (Integer) processingMetrics.getOrDefault("totalItemsProcessed", 0);
        Integer totalRequests = (Integer) processingMetrics.getOrDefault("totalRequests", 0);
        
        processingMetrics.put("totalProcessingTime", totalProcessingTime + processingTime);
        processingMetrics.put("totalItemsProcessed", totalItemsProcessed + itemCount);
        processingMetrics.put("totalRequests", totalRequests + 1);
        processingMetrics.put("averageProcessingTime", 
            (double) (totalProcessingTime + processingTime) / (totalRequests + 1));
        processingMetrics.put("lastUpdate", LocalDateTime.now());
    }
    
    /**
     * Manage cache size using HashMap operations
     */
    private void manageCacheSize() {
        // Production-grade: Cache management with HashMap
        if (dataCache.size() > MAX_CACHE_SIZE) {
            // Remove oldest entries using Streams
            List<String> keysToRemove = dataCache.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .limit(dataCache.size() - MAX_CACHE_SIZE)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            
            keysToRemove.forEach(dataCache::remove);
            
            // Update metrics
            processingMetrics.put("cacheCleanup", LocalDateTime.now());
            processingMetrics.put("removedEntries", keysToRemove.size());
        }
    }
    
    /**
     * Get singleton statistics using HashMap operations
     * 
     * @return singleton statistics
     */
    public Map<String, Object> getSingletonStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Production-grade: Statistics using HashMap
        stats.put("singletonInstance", this.hashCode());
        stats.put("version", SINGLETON_VERSION);
        stats.put("dataCacheSize", dataCache.size());
        stats.put("maxCacheSize", MAX_CACHE_SIZE);
        stats.put("totalRequests", requestCounter.get());
        stats.put("processingMetrics", new HashMap<>(processingMetrics));
        
        // Using Streams for cache analysis
        Map<String, Object> cacheAnalysis = new HashMap<>();
        cacheAnalysis.put("cacheKeys", new ArrayList<>(dataCache.keySet()));
        cacheAnalysis.put("cacheValues", dataCache.values().stream()
                .limit(10)
                .collect(Collectors.toList()));
        
        stats.put("cacheAnalysis", cacheAnalysis);
        stats.put("timestamp", LocalDateTime.now());
        
        return stats;
    }
    
    /**
     * Get data cache using HashMap operations
     * 
     * @return data cache
     */
    public Map<String, Object> getDataCache() {
        return new HashMap<>(dataCache);
    }
    
    /**
     * Clear data cache
     */
    public void clearDataCache() {
        dataCache.clear();
        processingMetrics.put("cacheCleared", LocalDateTime.now());
    }
    
    /**
     * Reset singleton (for testing purposes)
     */
    public static void resetInstance() {
        synchronized (DataProcessingSingleton.class) {
            instance = null;
        }
    }
}
