package com.backend.designpatterns.structural;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Netflix Production-Grade Decorator Pattern Implementation
 * 
 * Demonstrates Netflix SDE-2 design pattern expertise:
 * - Decorator pattern for dynamic behavior enhancement
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
public class DataProcessingDecorator {

    // Production-grade: Thread-safe collections for concurrent access
    private final Map<String, Object> decoratorCache = new ConcurrentHashMap<>();
    private final Map<String, Object> performanceMetrics = new ConcurrentHashMap<>();
    
    /**
     * Base data processing interface
     */
    public interface DataProcessor {
        Map<String, Object> processData(List<String> data);
        String getProcessorName();
        Map<String, Object> getProcessorInfo();
    }
    
    /**
     * Base data processor implementation
     */
    public static class BasicDataProcessor implements DataProcessor {
        @Override
        public Map<String, Object> processData(List<String> data) {
            Map<String, Object> result = new HashMap<>();
            result.put("processedItems", data.size());
            result.put("processor", "BASIC");
            result.put("timestamp", LocalDateTime.now());
            return result;
        }
        
        @Override
        public String getProcessorName() {
            return "BasicDataProcessor";
        }
        
        @Override
        public Map<String, Object> getProcessorInfo() {
            Map<String, Object> info = new HashMap<>();
            info.put("name", getProcessorName());
            info.put("capabilities", Arrays.asList("basic_processing"));
            info.put("version", "1.0.0");
            return info;
        }
    }
    
    /**
     * Abstract decorator class
     */
    public abstract static class DataProcessorDecorator implements DataProcessor {
        protected final DataProcessor wrappedProcessor;
        
        protected DataProcessorDecorator(DataProcessor processor) {
            this.wrappedProcessor = processor;
        }
        
        @Override
        public String getProcessorName() {
            return wrappedProcessor.getProcessorName();
        }
        
        @Override
        public Map<String, Object> getProcessorInfo() {
            return wrappedProcessor.getProcessorInfo();
        }
    }
    
    /**
     * Logging decorator with HashMap operations
     */
    public static class LoggingDecorator extends DataProcessorDecorator {
        public LoggingDecorator(DataProcessor processor) {
            super(processor);
        }
        
        @Override
        public Map<String, Object> processData(List<String> data) {
            long startTime = System.currentTimeMillis();
            
            // Production-grade: Logging with HashMap operations
            Map<String, Object> logData = new HashMap<>();
            logData.put("operation", "PROCESSING_START");
            logData.put("processor", getProcessorName());
            logData.put("dataSize", data.size());
            logData.put("startTime", startTime);
            
            // Process data using wrapped processor
            Map<String, Object> result = wrappedProcessor.processData(data);
            
            // Add logging information using HashMap operations
            long endTime = System.currentTimeMillis();
            long processingTime = endTime - startTime;
            
            // Method 1: Using entrySet iteration for result enhancement
            for (Map.Entry<String, Object> entry : result.entrySet()) {
                if (entry.getValue() instanceof Number) {
                    result.put(entry.getKey() + "_logged", entry.getValue());
                }
            }
            
            // Method 2: Using forEach with lambda for logging
            logData.forEach((key, value) -> 
                result.put("log_" + key, value));
            
            result.put("processingTime", processingTime);
            result.put("endTime", endTime);
            result.put("decorator", "LOGGING");
            
            return result;
        }
        
        @Override
        public String getProcessorName() {
            return "Logging" + wrappedProcessor.getProcessorName();
        }
        
        @Override
        public Map<String, Object> getProcessorInfo() {
            Map<String, Object> info = new HashMap<>(wrappedProcessor.getProcessorInfo());
            info.put("decorator", "LOGGING");
            info.put("capabilities", Arrays.asList("basic_processing", "logging"));
            return info;
        }
    }
    
    /**
     * Caching decorator with HashMap operations
     */
    public static class CachingDecorator extends DataProcessorDecorator {
        private final Map<String, Object> cache = new ConcurrentHashMap<>();
        private static final int MAX_CACHE_SIZE = 1000;
        
        public CachingDecorator(DataProcessor processor) {
            super(processor);
        }
        
        @Override
        public Map<String, Object> processData(List<String> data) {
            // Production-grade: Cache key generation using HashMap
            String cacheKey = generateCacheKey(data);
            
            // Check cache using HashMap operations
            if (cache.containsKey(cacheKey)) {
                Map<String, Object> cachedResult = new HashMap<>((Map<String, Object>) cache.get(cacheKey));
                cachedResult.put("source", "CACHE");
                cachedResult.put("cacheHit", true);
                cachedResult.put("timestamp", LocalDateTime.now());
                return cachedResult;
            }
            
            // Process data using wrapped processor
            Map<String, Object> result = wrappedProcessor.processData(data);
            
            // Add caching information
            result.put("source", "PROCESSED");
            result.put("cacheHit", false);
            result.put("decorator", "CACHING");
            
            // Cache result using HashMap operations
            cacheResult(cacheKey, result);
            
            return result;
        }
        
        /**
         * Generate cache key using HashMap operations
         * 
         * @param data data to generate key for
         * @return cache key
         */
        private String generateCacheKey(List<String> data) {
            // Production-grade: Key generation with HashMap
            Map<String, Object> keyComponents = new HashMap<>();
            keyComponents.put("dataSize", data.size());
            keyComponents.put("dataHash", data.hashCode());
            keyComponents.put("firstItem", data.isEmpty() ? "" : data.get(0));
            keyComponents.put("lastItem", data.isEmpty() ? "" : data.get(data.size() - 1));
            
            // Using Streams for key generation
            String keyString = keyComponents.entrySet().stream()
                    .map(entry -> entry.getKey() + ":" + entry.getValue())
                    .collect(Collectors.joining("|"));
            
            return "cache_" + keyString.hashCode();
        }
        
        /**
         * Cache result using HashMap operations
         * 
         * @param key cache key
         * @param result result to cache
         */
        private void cacheResult(String key, Map<String, Object> result) {
            // Production-grade: Cache management with HashMap
            cache.put(key, new HashMap<>(result));
            
            // Cache size management using Streams
            if (cache.size() > MAX_CACHE_SIZE) {
                // Remove oldest entries using Streams
                List<String> keysToRemove = cache.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .limit(cache.size() - MAX_CACHE_SIZE)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());
                
                keysToRemove.forEach(cache::remove);
            }
        }
        
        @Override
        public String getProcessorName() {
            return "Caching" + wrappedProcessor.getProcessorName();
        }
        
        @Override
        public Map<String, Object> getProcessorInfo() {
            Map<String, Object> info = new HashMap<>(wrappedProcessor.getProcessorInfo());
            info.put("decorator", "CACHING");
            info.put("cacheSize", cache.size());
            info.put("maxCacheSize", MAX_CACHE_SIZE);
            info.put("capabilities", Arrays.asList("basic_processing", "caching"));
            return info;
        }
    }
    
    /**
     * Validation decorator with HashMap operations
     */
    public static class ValidationDecorator extends DataProcessorDecorator {
        public ValidationDecorator(DataProcessor processor) {
            super(processor);
        }
        
        @Override
        public Map<String, Object> processData(List<String> data) {
            // Production-grade: Validation with HashMap operations
            Map<String, Object> validationResult = validateData(data);
            
            if (!(Boolean) validationResult.get("valid")) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("error", "Validation failed");
                errorResult.put("validationErrors", validationResult.get("errors"));
                errorResult.put("decorator", "VALIDATION");
                errorResult.put("timestamp", LocalDateTime.now());
                return errorResult;
            }
            
            // Process data using wrapped processor
            Map<String, Object> result = wrappedProcessor.processData(data);
            
            // Add validation information
            result.put("validation", validationResult);
            result.put("decorator", "VALIDATION");
            
            return result;
        }
        
        /**
         * Validate data using HashMap operations
         * 
         * @param data data to validate
         * @return validation result
         */
        private Map<String, Object> validateData(List<String> data) {
            Map<String, Object> validation = new HashMap<>();
            List<String> errors = new ArrayList<>();
            
            // Production-grade: Validation using HashMap operations
            if (data == null) {
                errors.add("Data cannot be null");
            } else if (data.isEmpty()) {
                errors.add("Data cannot be empty");
            } else {
                // Method 1: Using Streams for validation
                long nullCount = data.stream()
                        .filter(Objects::isNull)
                        .count();
                
                if (nullCount > 0) {
                    errors.add("Data contains " + nullCount + " null values");
                }
                
                // Method 2: Using forEach with lambda for detailed validation
                Map<String, Integer> itemLengths = new HashMap<>();
                data.forEach(item -> {
                    if (item != null) {
                        itemLengths.put(item, item.length());
                    }
                });
                
                // Method 3: Using entrySet iteration for validation analysis
                for (Map.Entry<String, Integer> entry : itemLengths.entrySet()) {
                    if (entry.getValue() > 1000) {
                        errors.add("Item '" + entry.getKey() + "' exceeds maximum length");
                    }
                }
            }
            
            validation.put("valid", errors.isEmpty());
            validation.put("errors", errors);
            validation.put("timestamp", LocalDateTime.now());
            
            return validation;
        }
        
        @Override
        public String getProcessorName() {
            return "Validation" + wrappedProcessor.getProcessorName();
        }
        
        @Override
        public Map<String, Object> getProcessorInfo() {
            Map<String, Object> info = new HashMap<>(wrappedProcessor.getProcessorInfo());
            info.put("decorator", "VALIDATION");
            info.put("capabilities", Arrays.asList("basic_processing", "validation"));
            return info;
        }
    }
    
    /**
     * Create decorated processor with multiple decorators
     * 
     * @param baseProcessor base processor
     * @param decorators decorator types to apply
     * @return decorated processor
     */
    public DataProcessor createDecoratedProcessor(DataProcessor baseProcessor, List<String> decorators) {
        DataProcessor processor = baseProcessor;
        
        // Production-grade: Decorator application using HashMap operations
        Map<String, java.util.function.Function<DataProcessor, DataProcessor>> decoratorFactories = 
            new HashMap<>();
        
        decoratorFactories.put("LOGGING", LoggingDecorator::new);
        decoratorFactories.put("CACHING", CachingDecorator::new);
        decoratorFactories.put("VALIDATION", ValidationDecorator::new);
        
        // Apply decorators using Stream operations
        for (String decoratorType : decorators) {
            java.util.function.Function<DataProcessor, DataProcessor> factory = 
                decoratorFactories.get(decoratorType);
            
            if (factory != null) {
                processor = factory.apply(processor);
            }
        }
        
        return processor;
    }
    
    /**
     * Get decorator statistics using HashMap operations
     * 
     * @return decorator statistics
     */
    public Map<String, Object> getDecoratorStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Production-grade: Statistics using HashMap
        stats.put("decoratorCacheSize", decoratorCache.size());
        stats.put("performanceMetrics", new HashMap<>(performanceMetrics));
        stats.put("availableDecorators", Arrays.asList("LOGGING", "CACHING", "VALIDATION"));
        stats.put("timestamp", LocalDateTime.now());
        
        return stats;
    }
    
    /**
     * Clear decorator cache
     */
    public void clearDecoratorCache() {
        decoratorCache.clear();
    }
}
