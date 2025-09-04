package com.backend.designpatterns.strategy.impl;

import com.backend.designpatterns.strategy.DataProcessingStrategy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Netflix Production-Grade Batch Processing Strategy Implementation
 * 
 * Demonstrates Netflix SDE-2 design pattern expertise:
 * - Strategy pattern implementation
 * - Advanced HashMap operations and iterations
 * - Stream operations for batch processing
 * - Optional usage for null safety
 * - Collection framework best practices
 * - Thread-safe collections for production
 * 
 * @author Netflix Backend Team
 * @version 1.0.0
 */
@Component
public class BatchProcessingStrategy implements DataProcessingStrategy {

    // Production-grade: Thread-safe collections for concurrent access
    private final Map<String, List<String>> batchCache = new ConcurrentHashMap<>();
    private final Map<String, Object> performanceMetrics = new ConcurrentHashMap<>();
    
    // Production-grade: Strategy configuration
    private static final int DEFAULT_BATCH_SIZE = 100;
    private static final String STRATEGY_NAME = "BATCH_PROCESSING";
    
    public BatchProcessingStrategy() {
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
            // Production-grade: HashMap operations with Streams
            Map<String, List<String>> batchedData = createBatches(data);
            
            // Demonstrate HashMap iteration patterns
            results.put("totalBatches", batchedData.size());
            results.put("totalItems", data.size());
            results.put("batchSize", DEFAULT_BATCH_SIZE);
            
            // Using Streams for data transformation
            Map<String, Object> batchStats = batchedData.entrySet().stream()
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> processBatch(entry.getValue())
                    ));
            
            results.put("batchResults", batchStats);
            
            // Production-grade: Performance metrics
            long processingTime = System.currentTimeMillis() - startTime;
            updatePerformanceMetrics(processingTime, data.size());
            
            results.put("processingTime", processingTime);
            results.put("timestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            results.put("error", "Batch processing failed: " + e.getMessage());
            results.put("status", "FAILED");
        }
        
        return results;
    }
    
    @Override
    public Map<String, Object> getStrategyInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("strategyName", STRATEGY_NAME);
        info.put("description", "High-performance batch processing strategy for large datasets");
        info.put("batchSize", DEFAULT_BATCH_SIZE);
        info.put("threadSafe", true);
        info.put("supportsStreaming", false);
        info.put("optimizedFor", "Large datasets with predictable patterns");
        
        return info;
    }
    
    @Override
    public boolean canHandle(List<String> data) {
        // Production-grade: Strategy validation logic
        return Optional.ofNullable(data)
                .map(list -> list.size() >= DEFAULT_BATCH_SIZE)
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
     * Create batches using Stream operations and HashMap
     * 
     * @param data input data
     * @return Map of batch ID to batch data
     */
    private Map<String, List<String>> createBatches(List<String> data) {
        // Production-grade: Using Streams for batch creation
        return IntStream.range(0, (data.size() + DEFAULT_BATCH_SIZE - 1) / DEFAULT_BATCH_SIZE)
                .mapToObj(batchIndex -> {
                    int startIndex = batchIndex * DEFAULT_BATCH_SIZE;
                    int endIndex = Math.min(startIndex + DEFAULT_BATCH_SIZE, data.size());
                    return new AbstractMap.SimpleEntry<>(
                        "batch_" + batchIndex,
                        data.subList(startIndex, endIndex)
                    );
                })
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue
                ));
    }
    
    /**
     * Process individual batch with HashMap operations
     * 
     * @param batchData batch data to process
     * @return processing results
     */
    private Map<String, Object> processBatch(List<String> batchData) {
        Map<String, Object> batchResult = new HashMap<>();
        
        // Production-grade: HashMap iteration patterns
        batchResult.put("batchSize", batchData.size());
        
        // Method 1: Using entrySet iteration
        Map<String, Integer> itemLengths = new HashMap<>();
        for (String item : batchData) {
            itemLengths.put(item, item.length());
        }
        
        // Method 2: Using Streams for aggregation
        IntSummaryStatistics lengthStats = itemLengths.values().stream()
                .mapToInt(Integer::intValue)
                .summaryStatistics();
        
        batchResult.put("lengthStats", lengthStats);
        batchResult.put("uniqueItems", new HashSet<>(batchData).size());
        
        // Method 3: Using forEach with lambda
        Map<String, String> processedItems = new HashMap<>();
        batchData.forEach(item -> 
            processedItems.put(item, "processed_" + item.toUpperCase()));
        
        batchResult.put("processedItems", processedItems);
        
        return batchResult;
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
        
        // Calculate average processing time
        Optional.ofNullable(performanceMetrics.get("totalProcessingTime"))
                .map(total -> (Long) total + processingTime)
                .ifPresent(total -> performanceMetrics.put("totalProcessingTime", total));
        
        Optional.ofNullable(performanceMetrics.get("totalItemsProcessed"))
                .map(total -> (Integer) total + itemCount)
                .ifPresent(total -> performanceMetrics.put("totalItemsProcessed", total));
    }
    
    /**
     * Initialize performance metrics with HashMap
     */
    private void initializePerformanceMetrics() {
        performanceMetrics.put("totalProcessingTime", 0L);
        performanceMetrics.put("totalItemsProcessed", 0);
        performanceMetrics.put("strategyCreated", LocalDateTime.now());
        performanceMetrics.put("version", "1.0.0");
    }
}
