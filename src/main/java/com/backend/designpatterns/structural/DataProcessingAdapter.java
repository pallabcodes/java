package com.backend.designpatterns.structural;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Netflix Production-Grade Adapter Pattern Implementation
 * 
 * Demonstrates Netflix SDE-2 design pattern expertise:
 * - Adapter pattern for legacy system compatibility
 * - Advanced HashMap operations and iterations
 * - Stream operations for data transformation
 * - Optional usage for null safety
 * - Collection framework best practices
 * - Thread-safe collections for production
 * 
 * @author Netflix Backend Team
 * @version 1.0.0
 */
@Component
public class DataProcessingAdapter {

    // Production-grade: Thread-safe collections for concurrent access
    private final Map<String, Object> legacyDataCache = new ConcurrentHashMap<>();
    private final Map<String, Object> modernDataCache = new ConcurrentHashMap<>();
    
    // Production-grade: Adapter configuration
    private static final String ADAPTER_VERSION = "2.0.0";
    
    /**
     * Legacy data format interface (simulating old system)
     */
    public interface LegacyDataFormat {
        String getLegacyId();
        String getLegacyContent();
        Date getLegacyTimestamp();
    }
    
    /**
     * Modern data format interface (current system)
     */
    public interface ModernDataFormat {
        String getId();
        String getContent();
        java.time.LocalDateTime getTimestamp();
        Map<String, Object> getMetadata();
    }
    
    /**
     * Legacy data implementation
     */
    public static class LegacyData implements LegacyDataFormat {
        private final String legacyId;
        private final String legacyContent;
        private final Date legacyTimestamp;
        
        public LegacyData(String legacyId, String legacyContent, Date legacyTimestamp) {
            this.legacyId = legacyId;
            this.legacyContent = legacyContent;
            this.legacyTimestamp = legacyTimestamp;
        }
        
        @Override
        public String getLegacyId() { return legacyId; }
        
        @Override
        public String getLegacyContent() { return legacyContent; }
        
        @Override
        public Date getLegacyTimestamp() { return legacyTimestamp; }
    }
    
    /**
     * Modern data implementation
     */
    public static class ModernData implements ModernDataFormat {
        private final String id;
        private final String content;
        private final java.time.LocalDateTime timestamp;
        private final Map<String, Object> metadata;
        
        public ModernData(String id, String content, java.time.LocalDateTime timestamp, Map<String, Object> metadata) {
            this.id = id;
            this.content = content;
            this.timestamp = timestamp;
            this.metadata = new HashMap<>(metadata);
        }
        
        @Override
        public String getId() { return id; }
        
        @Override
        public String getContent() { return content; }
        
        @Override
        public java.time.LocalDateTime getTimestamp() { return timestamp; }
        
        @Override
        public Map<String, Object> getMetadata() { return Collections.unmodifiableMap(metadata); }
    }
    
    /**
     * Adapter class that converts legacy data to modern format
     */
    public static class LegacyToModernAdapter implements ModernDataFormat {
        private final LegacyDataFormat legacyData;
        
        public LegacyToModernAdapter(LegacyDataFormat legacyData) {
            this.legacyData = legacyData;
        }
        
        @Override
        public String getId() {
            return "MODERN_" + legacyData.getLegacyId();
        }
        
        @Override
        public String getContent() {
            return legacyData.getLegacyContent().toUpperCase();
        }
        
        @Override
        public java.time.LocalDateTime getTimestamp() {
            // Convert legacy Date to modern LocalDateTime
            return legacyData.getLegacyTimestamp().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();
        }
        
        @Override
        public Map<String, Object> getMetadata() {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("originalLegacyId", legacyData.getLegacyId());
            metadata.put("adapterVersion", ADAPTER_VERSION);
            metadata.put("convertedAt", java.time.LocalDateTime.now());
            metadata.put("source", "LEGACY_SYSTEM");
            return metadata;
        }
    }
    
    /**
     * Process legacy data using adapter pattern with HashMap operations
     * 
     * @param legacyDataList list of legacy data
     * @return modern data list
     */
    public List<ModernDataFormat> adaptLegacyData(List<LegacyDataFormat> legacyDataList) {
        // Production-grade: Input validation with Optional
        if (legacyDataList == null || legacyDataList.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Production-grade: Using Streams for data transformation
        return legacyDataList.stream()
                .filter(Objects::nonNull)
                .map(LegacyToModernAdapter::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Process modern data and cache results using HashMap operations
     * 
     * @param modernDataList list of modern data
     * @return processing results
     */
    public Map<String, Object> processModernData(List<ModernDataFormat> modernDataList) {
        Map<String, Object> results = new HashMap<>();
        
        if (modernDataList == null || modernDataList.isEmpty()) {
            return results;
        }
        
        // Production-grade: HashMap operations with Streams
        Map<String, Object> processedData = new HashMap<>();
        
        // Method 1: Using forEach with lambda
        modernDataList.forEach(data -> {
            String key = data.getId();
            Map<String, Object> processed = processDataItem(data);
            processedData.put(key, processed);
            
            // Update modern cache with HashMap operations
            updateModernCache(key, processed);
        });
        
        // Method 2: Using Streams for data analysis
        Map<String, Long> contentLengthDistribution = modernDataList.stream()
                .collect(Collectors.groupingBy(
                    data -> categorizeContentLength(data.getContent()),
                    Collectors.counting()
                ));
        
        // Method 3: Using entrySet iteration
        Map<String, Object> metadataAnalysis = new HashMap<>();
        for (Map.Entry<String, Object> entry : processedData.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> valueMap = (Map<String, Object>) value;
                metadataAnalysis.put(key + "_metadata", valueMap.get("metadata"));
            }
        }
        
        results.put("processedData", processedData);
        results.put("contentLengthDistribution", contentLengthDistribution);
        results.put("metadataAnalysis", metadataAnalysis);
        results.put("totalProcessed", processedData.size());
        results.put("timestamp", java.time.LocalDateTime.now());
        
        return results;
    }
    
    /**
     * Process individual data item using HashMap operations
     * 
     * @param data data item to process
     * @return processed result
     */
    private Map<String, Object> processDataItem(ModernDataFormat data) {
        Map<String, Object> processed = new HashMap<>();
        
        // Production-grade: Data processing with HashMap
        processed.put("id", data.getId());
        processed.put("content", data.getContent());
        processed.put("timestamp", data.getTimestamp());
        processed.put("contentLength", data.getContent().length());
        processed.put("wordCount", data.getContent().split("\\s+").length);
        processed.put("processedAt", java.time.LocalDateTime.now());
        
        // Using Streams for metadata analysis
        Map<String, Object> enhancedMetadata = new HashMap<>(data.getMetadata());
        enhancedMetadata.put("processingVersion", ADAPTER_VERSION);
        enhancedMetadata.put("contentHash", data.getContent().hashCode());
        
        processed.put("metadata", enhancedMetadata);
        
        return processed;
    }
    
    /**
     * Categorize content length using HashMap operations
     * 
     * @param content content to categorize
     * @return length category
     */
    private String categorizeContentLength(String content) {
        // Production-grade: Content categorization with HashMap
        Map<Integer, String> lengthCategories = new HashMap<>();
        lengthCategories.put(10, "SHORT");
        lengthCategories.put(50, "MEDIUM");
        lengthCategories.put(100, "LONG");
        lengthCategories.put(Integer.MAX_VALUE, "VERY_LONG");
        
        int length = content.length();
        
        // Method 1: Using entrySet iteration
        for (Map.Entry<Integer, String> entry : lengthCategories.entrySet()) {
            if (length <= entry.getKey()) {
                return entry.getValue();
            }
        }
        
        return "VERY_LONG";
    }
    
    /**
     * Update modern cache using HashMap operations
     * 
     * @param key cache key
     * @param value cache value
     */
    private void updateModernCache(String key, Object value) {
        // Production-grade: Cache management with HashMap
        modernDataCache.put(key, value);
        
        // Cache size management using Streams
        if (modernDataCache.size() > 1000) {
            // Remove oldest entries using Streams
            List<String> keysToRemove = modernDataCache.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .limit(modernDataCache.size() - 1000)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            
            keysToRemove.forEach(modernDataCache::remove);
        }
    }
    
    /**
     * Get adapter statistics using HashMap operations
     * 
     * @return adapter statistics
     */
    public Map<String, Object> getAdapterStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Production-grade: Statistics using HashMap
        stats.put("adapterVersion", ADAPTER_VERSION);
        stats.put("legacyCacheSize", legacyDataCache.size());
        stats.put("modernCacheSize", modernDataCache.size());
        stats.put("totalCacheSize", legacyDataCache.size() + modernDataCache.size());
        stats.put("timestamp", java.time.LocalDateTime.now());
        
        // Using Streams for cache analysis
        Map<String, Object> cacheAnalysis = new HashMap<>();
        cacheAnalysis.put("legacyCacheKeys", new ArrayList<>(legacyDataCache.keySet()));
        cacheAnalysis.put("modernCacheKeys", new ArrayList<>(modernDataCache.keySet()));
        
        stats.put("cacheAnalysis", cacheAnalysis);
        
        return stats;
    }
    
    /**
     * Clear all caches
     */
    public void clearCaches() {
        legacyDataCache.clear();
        modernDataCache.clear();
    }
}
