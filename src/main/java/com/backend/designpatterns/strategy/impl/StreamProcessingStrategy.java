package com.backend.designpatterns.strategy.impl;

import com.backend.designpatterns.strategy.DataProcessingStrategy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Netflix Production-Grade Stream Processing Strategy Implementation
 * 
 * Demonstrates Netflix SDE-2 design pattern expertise:
 * - Strategy pattern implementation
 * - Advanced HashMap operations and iterations
 * - Stream operations for stream processing
 * - Optional usage for null safety
 * - Collection framework best practices
 * - Thread-safe collections for production
 * 
 * @author Netflix Backend Team
 * @version 1.0.0
 */
@Component
public class StreamProcessingStrategy implements DataProcessingStrategy {

    // Production-grade: Thread-safe collections for concurrent access
    private final Map<String, Object> streamCache = new ConcurrentHashMap<>();
    private final Map<String, Object> performanceMetrics = new ConcurrentHashMap<>();
    
    // Production-grade: Strategy configuration
    private static final String STRATEGY_NAME = "STREAM_PROCESSING";
    private static final int STREAM_BUFFER_SIZE = 500;
    
    public StreamProcessingStrategy() {
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
            // Production-grade: Stream processing with HashMap operations
            Map<String, Object> streamResults = processStreamData(data);
            
            // Demonstrate HashMap iteration patterns
            results.put("streamResults", streamResults.size());
            results.put("totalItems", data.size());
            results.put("processingMode", "STREAM");
            
            // Using Streams for advanced data transformation
            Map<String, Object> streamAnalysis = performStreamAnalysis(data);
            results.put("streamAnalysis", streamAnalysis);
            
            // Production-grade: Performance metrics
            long processingTime = System.currentTimeMillis() - startTime;
            updatePerformanceMetrics(processingTime, data.size());
            
            results.put("processingTime", processingTime);
            results.put("timestamp", LocalDateTime.now());
            results.put("streamBufferSize", STREAM_BUFFER_SIZE);
            
        } catch (Exception e) {
            results.put("error", "Stream processing failed: " + e.getMessage());
            results.put("status", "FAILED");
        }
        
        return results;
    }
    
    @Override
    public Map<String, Object> getStrategyInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("strategyName", STRATEGY_NAME);
        info.put("description", "High-performance stream processing strategy for continuous data flows");
        info.put("streamBufferSize", STREAM_BUFFER_SIZE);
        info.put("threadSafe", true);
        info.put("supportsStreaming", true);
        info.put("optimizedFor", "Continuous data streams and real-time analytics");
        
        return info;
    }
    
    @Override
    public boolean canHandle(List<String> data) {
        // Production-grade: Strategy validation logic
        return Optional.ofNullable(data)
                .map(list -> list.size() > 100) // Stream strategy for larger datasets
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
     * Process data using stream operations
     * 
     * @param data input data
     * @return processed results
     */
    private Map<String, Object> processStreamData(List<String> data) {
        Map<String, Object> processed = new HashMap<>();
        
        // Production-grade: Stream processing with HashMap
        Stream<String> dataStream = data.stream();
        
        // Process data in chunks using Streams
        List<List<String>> chunks = dataStream
                .collect(Collectors.groupingBy(
                    item -> (data.indexOf(item) / STREAM_BUFFER_SIZE),
                    Collectors.toList()
                ))
                .values()
                .stream()
                .collect(Collectors.toList());
        
        // Process each chunk using HashMap operations
        chunks.forEach(chunk -> {
            String chunkId = "chunk_" + chunks.indexOf(chunk);
            Map<String, Object> chunkResult = processChunk(chunk);
            processed.put(chunkId, chunkResult);
            
            // Update stream cache with HashMap operations
            updateStreamCache(chunkId, chunkResult);
        });
        
        return processed;
    }
    
    /**
     * Process individual chunk using HashMap operations
     * 
     * @param chunk data chunk to process
     * @return chunk processing results
     */
    private Map<String, Object> processChunk(List<String> chunk) {
        Map<String, Object> chunkResult = new HashMap<>();
        
        // Production-grade: Chunk processing with HashMap
        chunkResult.put("chunkSize", chunk.size());
        chunkResult.put("processedAt", LocalDateTime.now());
        
        // Method 1: Using Streams for data analysis
        IntSummaryStatistics lengthStats = chunk.stream()
                .filter(Objects::nonNull)
                .mapToInt(String::length)
                .summaryStatistics();
        
        chunkResult.put("lengthStats", lengthStats);
        
        // Method 2: Using HashMap for item categorization
        Map<String, List<String>> categorizedItems = chunk.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                    item -> categorizeItem(item),
                    Collectors.toList()
                ));
        
        chunkResult.put("categorizedItems", categorizedItems);
        
        // Method 3: Using forEach with lambda for detailed processing
        Map<String, Object> itemDetails = new HashMap<>();
        chunk.forEach(item -> {
            if (item != null) {
                String key = "item_" + item.hashCode();
                Map<String, Object> details = new HashMap<>();
                details.put("original", item);
                details.put("length", item.length());
                details.put("category", categorizeItem(item));
                details.put("processed", true);
                itemDetails.put(key, details);
            }
        });
        
        chunkResult.put("itemDetails", itemDetails);
        
        return chunkResult;
    }
    
    /**
     * Categorize item using HashMap operations
     * 
     * @param item item to categorize
     * @return category
     */
    private String categorizeItem(String item) {
        // Production-grade: Item categorization with HashMap
        Map<String, String> categoryMappings = new HashMap<>();
        categoryMappings.put("netflix", "streaming");
        categoryMappings.put("production", "enterprise");
        categoryMappings.put("grade", "quality");
        categoryMappings.put("design", "architecture");
        categoryMappings.put("patterns", "structure");
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
     * Perform advanced stream analysis using Streams
     * 
     * @param data input data
     * @return stream analysis results
     */
    private Map<String, Object> performStreamAnalysis(List<String> data) {
        Map<String, Object> analysis = new HashMap<>();
        
        // Production-grade: Advanced stream analysis
        analysis.put("totalItems", data.size());
        
        // Using Streams for comprehensive analysis
        Map<String, Long> categoryCounts = data.stream()
                .filter(Objects::nonNull)
                .map(this::categorizeItem)
                .collect(Collectors.groupingBy(
                    category -> category,
                    Collectors.counting()
                ));
        
        analysis.put("categoryCounts", categoryCounts);
        
        // Length distribution using Streams
        Map<Integer, Long> lengthDistribution = data.stream()
                .filter(Objects::nonNull)
                .mapToInt(String::length)
                .boxed()
                .collect(Collectors.groupingBy(
                    length -> length,
                    Collectors.counting()
                ));
        
        analysis.put("lengthDistribution", lengthDistribution);
        
        // Word frequency analysis using Streams
        Map<String, Long> wordFrequency = data.stream()
                .filter(Objects::nonNull)
                .flatMap(item -> Arrays.stream(item.toLowerCase().split("\\s+")))
                .filter(word -> !word.isEmpty())
                .collect(Collectors.groupingBy(
                    word -> word,
                    Collectors.counting()
                ));
        
        analysis.put("wordFrequency", wordFrequency);
        
        return analysis;
    }
    
    /**
     * Update stream cache using HashMap operations
     * 
     * @param key cache key
     * @param value cache value
     */
    private void updateStreamCache(String key, Object value) {
        // Production-grade: Cache management with HashMap
        streamCache.put(key, value);
        
        // Cache size management using Streams
        if (streamCache.size() > STREAM_BUFFER_SIZE) {
            // Remove oldest entries using Streams
            List<String> keysToRemove = streamCache.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .limit(streamCache.size() - STREAM_BUFFER_SIZE)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            
            keysToRemove.forEach(streamCache::remove);
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
        performanceMetrics.put("streamBufferUtilization", (double) streamCache.size() / STREAM_BUFFER_SIZE);
        
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
        performanceMetrics.put("streamBufferUtilization", 0.0);
        performanceMetrics.put("strategyCreated", LocalDateTime.now());
        performanceMetrics.put("version", "1.0.0");
    }
}
