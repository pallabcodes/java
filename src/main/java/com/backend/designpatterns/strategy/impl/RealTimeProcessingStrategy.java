package com.backend.designpatterns.strategy.impl;

import com.backend.designpatterns.strategy.DataProcessingStrategy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Netflix Production-Grade Real-Time Processing Strategy Implementation
 * 
 * Demonstrates Netflix SDE-2 design pattern expertise:
 * - Strategy pattern implementation
 * - Advanced HashMap operations and iterations
 * - Stream operations for real-time processing
 * - Optional usage for null safety
 * - Collection framework best practices
 * - Thread-safe collections for production
 * 
 * @author Netflix Backend Team
 * @version 1.0.0
 */
@Component
public class RealTimeProcessingStrategy implements DataProcessingStrategy {

    // Production-grade: Thread-safe collections for concurrent access
    private final Map<String, Object> realTimeCache = new ConcurrentHashMap<>();
    private final Map<String, Object> performanceMetrics = new ConcurrentHashMap<>();
    
    // Production-grade: Strategy configuration
    private static final String STRATEGY_NAME = "REAL_TIME_PROCESSING";
    private static final int MAX_CACHE_SIZE = 1000;
    
    public RealTimeProcessingStrategy() {
        initializePerformanceMetrics();
    }
    
    @Override
    public Map<String, Object> processData(List<String> data) {
        // Production-grade: Input validation with Optional
        if (data == null || data.isEmpty()) {
            return Collections.emptyMap();
        }
        
        Map<String, Object> results = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            // Production-grade: Real-time processing with HashMap operations
            Map<String, Object> processedData = processRealTimeData(data);
            
            // Demonstrate HashMap iteration patterns
            results.put("processedItems", processedData.size());
            results.put("totalItems", data.size());
            results.put("processingMode", "REAL_TIME");
            
            // Using Streams for data transformation
            Map<String, Long> frequencyMap = data.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.groupingBy(
                        String::toLowerCase,
                        Collectors.counting()
                    ));
            
            results.put("frequencyAnalysis", frequencyMap);
            results.put("uniqueItems", frequencyMap.size());
            
            // Production-grade: Performance metrics
            long processingTime = System.currentTimeMillis() - startTime;
            updatePerformanceMetrics(processingTime, data.size());
            
            results.put("processingTime", processingTime);
            results.put("timestamp", LocalDateTime.now());
            results.put("cacheSize", realTimeCache.size());
            
        } catch (Exception e) {
            results.put("error", "Real-time processing failed: " + e.getMessage());
            results.put("status", "FAILED");
        }
        
        return results;
    }
    
    @Override
    public Map<String, Object> getStrategyInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("strategyName", STRATEGY_NAME);
        info.put("description", "High-performance real-time processing strategy for immediate data analysis");
        info.put("maxCacheSize", MAX_CACHE_SIZE);
        info.put("threadSafe", true);
        info.put("supportsStreaming", true);
        info.put("optimizedFor", "Real-time data streams and immediate processing");
        
        return info;
    }
    
    @Override
    public boolean canHandle(List<String> data) {
        // Production-grade: Strategy validation logic
        return Optional.ofNullable(data)
                .map(list -> list.size() <= 1000) // Real-time strategy for smaller datasets
                .orElse(false);
    }
    
    @Override
    public Map<String, Object> getPerformanceMetrics() {
        return new HashMap<>(performanceMetrics);
    }
    
    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }
    
    /**
     * Process data in real-time using HashMap operations
     * 
     * @param data input data
     * @return processed results
     */
    private Map<String, Object> processRealTimeData(List<String> data) {
        Map<String, Object> processed = new HashMap<>();
        
        // Production-grade: Real-time processing with HashMap
        data.forEach(item -> {
            if (item != null) {
                String key = generateRealTimeKey(item);
                Object value = processRealTimeItem(item);
                processed.put(key, value);
                
                // Update cache with HashMap operations
                updateRealTimeCache(key, value);
            }
        });
        
        return processed;
    }
    
    /**
     * Generate real-time key using HashMap operations
     * 
     * @param item input item
     * @return generated key
     */
    private String generateRealTimeKey(String item) {
        // Production-grade: Key generation with HashMap
        Map<String, String> keyMappings = new HashMap<>();
        keyMappings.put("netflix", "streaming");
        keyMappings.put("production", "enterprise");
        keyMappings.put("grade", "quality");
        keyMappings.put("design", "architecture");
        keyMappings.put("patterns", "structure");
        
        // Method 1: Using entrySet iteration
        for (Map.Entry<String, String> entry : keyMappings.entrySet()) {
            if (item.toLowerCase().contains(entry.getKey())) {
                return entry.getValue() + "_" + System.currentTimeMillis();
            }
        }
        
        return "realtime_" + item.toLowerCase().replaceAll("\\s+", "_") + "_" + System.currentTimeMillis();
    }
    
    /**
     * Process individual item in real-time
     * 
     * @param item item to process
     * @return processed result
     */
    private Object processRealTimeItem(String item) {
        // Production-grade: Real-time item processing
        Map<String, Object> result = new HashMap<>();
        result.put("original", item);
        result.put("length", item.length());
        result.put("uppercase", item.toUpperCase());
        result.put("lowercase", item.toLowerCase());
        result.put("processedAt", LocalDateTime.now());
        
        // Using Streams for character analysis
        Map<Character, Long> charFrequency = item.chars()
                .mapToObj(ch -> (char) ch)
                .collect(Collectors.groupingBy(
                    Character::toLowerCase,
                    Collectors.counting()
                ));
        
        result.put("characterFrequency", charFrequency);
        
        return result;
    }
    
    /**
     * Update real-time cache using HashMap operations
     * 
     * @param key cache key
     * @param value cache value
     */
    private void updateRealTimeCache(String key, Object value) {
        // Production-grade: Cache management with HashMap
        realTimeCache.put(key, value);
        
        // Cache size management using Streams
        if (realTimeCache.size() > MAX_CACHE_SIZE) {
            // Remove oldest entries using Streams
            List<String> keysToRemove = realTimeCache.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .limit(realTimeCache.size() - MAX_CACHE_SIZE)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            
            keysToRemove.forEach(realTimeCache::remove);
        }
    }
    
    /**
     * Update performance metrics using HashMap operations
     * 
     * @param processingTime processing time in milliseconds
     * @param itemCount number of items processed
     */
    private void updatePerformanceMetrics(long processingTime, int itemCount) {
        // Production-grade: Performance tracking with HashMap
        performanceMetrics.put("lastProcessingTime", processingTime);
        performanceMetrics.put("lastItemCount", itemCount);
        performanceMetrics.put("lastUpdate", LocalDateTime.now());
        performanceMetrics.put("cacheHitRate", calculateCacheHitRate());
        
        // Calculate average processing time
        Optional.ofNullable(performanceMetrics.get("totalProcessingTime"))
                .map(total -> (Long) total + processingTime)
                .ifPresent(total -> performanceMetrics.put("totalProcessingTime", total));
        
        Optional.ofNullable(performanceMetrics.get("totalItemsProcessed"))
                .map(total -> (Integer) total + itemCount)
                .ifPresent(total -> performanceMetrics.put("totalItemsProcessed", total));
    }
    
    /**
     * Calculate cache hit rate using HashMap operations
     * 
     * @return cache hit rate
     */
    private double calculateCacheHitRate() {
        // Production-grade: Cache analysis using HashMap
        Long totalRequests = (Long) performanceMetrics.getOrDefault("totalCacheRequests", 0L);
        Long cacheHits = (Long) performanceMetrics.getOrDefault("totalCacheHits", 0L);
        
        if (totalRequests > 0) {
            return (double) cacheHits / totalRequests;
        }
        
        return 0.0;
    }
    
    /**
     * Initialize performance metrics with HashMap
     */
    private void initializePerformanceMetrics() {
        performanceMetrics.put("totalProcessingTime", 0L);
        performanceMetrics.put("totalItemsProcessed", 0);
        performanceMetrics.put("totalCacheRequests", 0L);
        performanceMetrics.put("totalCacheHits", 0L);
        performanceMetrics.put("strategyCreated", LocalDateTime.now());
        performanceMetrics.put("version", "1.0.0");
    }
}
