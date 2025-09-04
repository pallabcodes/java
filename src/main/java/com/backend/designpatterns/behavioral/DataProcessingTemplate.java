package com.backend.designpatterns.behavioral;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Netflix Production-Grade Template Method Pattern Implementation
 * 
 * Demonstrates Netflix SDE-2 design pattern expertise:
 * - Template method pattern for workflow definition
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
public class DataProcessingTemplate {

    // Production-grade: Thread-safe collections for concurrent access
    private final Map<String, Object> workflowCache = new ConcurrentHashMap<>();
    private final Map<String, Object> executionMetrics = new ConcurrentHashMap<>();
    
    /**
     * Abstract template class defining data processing workflow
     */
    public abstract static class DataProcessingWorkflow {
        
        // Production-grade: Template method defining the workflow
        public final Map<String, Object> executeWorkflow(List<String> data) {
            Map<String, Object> result = new HashMap<>();
            long startTime = System.currentTimeMillis();
            
            try {
                // Step 1: Validate data (template method)
                Map<String, Object> validationResult = validateData(data);
                if (!(Boolean) validationResult.get("valid")) {
                    result.put("error", "Validation failed");
                    result.put("validationErrors", validationResult.get("errors"));
                    return result;
                }
                
                // Step 2: Preprocess data (template method)
                List<String> preprocessedData = preprocessData(data);
                
                // Step 3: Process data (abstract method - must be implemented)
                Map<String, Object> processedData = processData(preprocessedData);
                
                // Step 4: Postprocess data (template method)
                Map<String, Object> postprocessedData = postprocessData(processedData);
                
                // Step 5: Generate final result
                result.putAll(postprocessedData);
                result.put("workflowType", getWorkflowType());
                result.put("executionTime", System.currentTimeMillis() - startTime);
                result.put("timestamp", LocalDateTime.now());
                result.put("status", "SUCCESS");
                
            } catch (Exception e) {
                result.put("error", "Workflow execution failed: " + e.getMessage());
                result.put("status", "FAILED");
            }
            
            return result;
        }
        
        /**
         * Template method: Validate data (can be overridden)
         * 
         * @param data input data
         * @return validation result
         */
        protected Map<String, Object> validateData(List<String> data) {
            Map<String, Object> validation = new HashMap<>();
            List<String> errors = new ArrayList<>();
            
            // Production-grade: Validation using HashMap operations
            if (data == null) {
                errors.add("Data cannot be null");
            } else if (data.isEmpty()) {
                errors.add("Data cannot be empty");
            } else {
                // Using Streams for validation
                long nullCount = data.stream()
                        .filter(Objects::isNull)
                        .count();
                
                if (nullCount > 0) {
                    errors.add("Data contains " + nullCount + " null values");
                }
                
                // Using HashMap for item validation
                Map<String, Integer> itemLengths = new HashMap<>();
                data.forEach(item -> {
                    if (item != null) {
                        itemLengths.put(item, item.length());
                    }
                });
                
                // Method 1: Using entrySet iteration for validation
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
        
        /**
         * Template method: Preprocess data (can be overridden)
         * 
         * @param data input data
         * @return preprocessed data
         */
        protected List<String> preprocessData(List<String> data) {
            // Production-grade: Preprocessing using Streams
            return data.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(item -> !item.isEmpty())
                    .collect(Collectors.toList());
        }
        
        /**
         * Template method: Postprocess data (can be overridden)
         * 
         * @param processedData processed data
         * @return postprocessed data
         */
        protected Map<String, Object> postprocessData(Map<String, Object> processedData) {
            Map<String, Object> result = new HashMap<>(processedData);
            
            // Production-grade: Postprocessing using HashMap operations
            result.put("postprocessingComplete", true);
            result.put("postprocessingTimestamp", LocalDateTime.now());
            
            // Method 1: Using entrySet iteration for enhancement
            for (Map.Entry<String, Object> entry : processedData.entrySet()) {
                if (entry.getValue() instanceof Number) {
                    result.put(entry.getKey() + "_enhanced", entry.getValue());
                }
            }
            
            return result;
        }
        
        /**
         * Abstract method: Process data (must be implemented by subclasses)
         * 
         * @param data preprocessed data
         * @return processed data
         */
        protected abstract Map<String, Object> processData(List<String> data);
        
        /**
         * Hook method: Get workflow type (can be overridden)
         * 
         * @return workflow type
         */
        protected String getWorkflowType() {
            return "GENERIC";
        }
    }
    
    /**
     * Batch processing workflow implementation
     */
    public static class BatchProcessingWorkflow extends DataProcessingWorkflow {
        
        @Override
        protected Map<String, Object> processData(List<String> data) {
            Map<String, Object> result = new HashMap<>();
            
            // Production-grade: Batch processing with HashMap operations
            result.put("processingMode", "BATCH");
            result.put("totalItems", data.size());
            
            // Using Streams for batch analysis
            Map<String, Long> itemFrequency = data.stream()
                    .collect(Collectors.groupingBy(
                        String::toLowerCase,
                        Collectors.counting()
                    ));
            
            result.put("itemFrequency", itemFrequency);
            result.put("uniqueItems", itemFrequency.size());
            
            // Using HashMap for item categorization
            Map<String, List<String>> categorizedItems = new HashMap<>();
            data.forEach(item -> {
                String category = categorizeItem(item);
                categorizedItems.computeIfAbsent(category, k -> new ArrayList<>()).add(item);
            });
            
            result.put("categorizedItems", categorizedItems);
            
            return result;
        }
        
        @Override
        protected String getWorkflowType() {
            return "BATCH_PROCESSING";
        }
        
        private String categorizeItem(String item) {
            Map<String, String> categories = new HashMap<>();
            categories.put("netflix", "streaming");
            categories.put("production", "enterprise");
            categories.put("grade", "quality");
            categories.put("design", "architecture");
            categories.put("patterns", "structure");
            
            // Method 1: Using entrySet iteration
            for (Map.Entry<String, String> entry : categories.entrySet()) {
                if (item.toLowerCase().contains(entry.getKey())) {
                    return entry.getValue();
                }
            }
            
            return "general";
        }
    }
    
    /**
     * Real-time processing workflow implementation
     */
    public static class RealTimeProcessingWorkflow extends DataProcessingWorkflow {
        
        @Override
        protected Map<String, Object> processData(List<String> data) {
            Map<String, Object> result = new HashMap<>();
            
            // Production-grade: Real-time processing with HashMap operations
            result.put("processingMode", "REAL_TIME");
            result.put("totalItems", data.size());
            
            // Using Streams for real-time analysis
            Map<String, Object> realTimeAnalysis = new HashMap<>();
            data.forEach(item -> {
                if (item != null) {
                    Map<String, Object> itemAnalysis = new HashMap<>();
                    itemAnalysis.put("length", item.length());
                    itemAnalysis.put("uppercase", item.toUpperCase());
                    itemAnalysis.put("processedAt", LocalDateTime.now());
                    
                    realTimeAnalysis.put("item_" + item.hashCode(), itemAnalysis);
                }
            });
            
            result.put("realTimeAnalysis", realTimeAnalysis);
            
            // Using HashMap for performance metrics
            Map<String, Object> performanceMetrics = new HashMap<>();
            performanceMetrics.put("processingSpeed", "HIGH");
            performanceMetrics.put("latency", "LOW");
            performanceMetrics.put("throughput", data.size());
            
            result.put("performanceMetrics", performanceMetrics);
            
            return result;
        }
        
        @Override
        protected String getWorkflowType() {
            return "REAL_TIME_PROCESSING";
        }
    }
    
    /**
     * Stream processing workflow implementation
     */
    public static class StreamProcessingWorkflow extends DataProcessingWorkflow {
        
        @Override
        protected Map<String, Object> processData(List<String> data) {
            Map<String, Object> result = new HashMap<>();
            
            // Production-grade: Stream processing with HashMap operations
            result.put("processingMode", "STREAM");
            result.put("totalItems", data.size());
            
            // Using Streams for stream analysis
            Map<String, Object> streamAnalysis = new HashMap<>();
            
            // Method 1: Using Streams for data transformation
            Map<String, Long> wordFrequency = data.stream()
                    .filter(Objects::nonNull)
                    .flatMap(item -> Arrays.stream(item.toLowerCase().split("\\s+")))
                    .filter(word -> !word.isEmpty())
                    .collect(Collectors.groupingBy(
                        word -> word,
                        Collectors.counting()
                    ));
            
            streamAnalysis.put("wordFrequency", wordFrequency);
            
            // Method 2: Using HashMap for character analysis
            Map<Character, Long> charFrequency = data.stream()
                    .filter(Objects::nonNull)
                    .flatMapToInt(String::chars)
                    .mapToObj(ch -> (char) ch)
                    .collect(Collectors.groupingBy(
                        Character::toLowerCase,
                        Collectors.counting()
                    ));
            
            streamAnalysis.put("charFrequency", charFrequency);
            
            result.put("streamAnalysis", streamAnalysis);
            
            return result;
        }
        
        @Override
        protected String getWorkflowType() {
            return "STREAM_PROCESSING";
        }
    }
    
    /**
     * Execute workflow using template method pattern
     * 
     * @param workflow workflow to execute
     * @param data input data
     * @return execution results
     */
    public Map<String, Object> executeWorkflow(DataProcessingWorkflow workflow, List<String> data) {
        // Production-grade: Workflow execution with HashMap operations
        Map<String, Object> result = workflow.executeWorkflow(data);
        
        // Cache workflow results using HashMap operations
        String workflowKey = "workflow_" + workflow.getWorkflowType() + "_" + System.currentTimeMillis();
        workflowCache.put(workflowKey, result);
        
        // Update execution metrics using HashMap operations
        updateExecutionMetrics(workflow.getWorkflowType(), result);
        
        return result;
    }
    
    /**
     * Update execution metrics using HashMap operations
     * 
     * @param workflowType workflow type
     * @param result execution result
     */
    private void updateExecutionMetrics(String workflowType, Map<String, Object> result) {
        // Production-grade: Metrics tracking with HashMap
        String metricsKey = "metrics_" + workflowType;
        Map<String, Object> metrics = (Map<String, Object>) executionMetrics.getOrDefault(metricsKey, new HashMap<>());
        
        // Method 1: Using entrySet iteration for metrics update
        for (Map.Entry<String, Object> entry : result.entrySet()) {
            if (entry.getValue() instanceof Number) {
                metrics.put(entry.getKey(), entry.getValue());
            }
        }
        
        metrics.put("lastExecution", LocalDateTime.now());
        metrics.put("executionCount", (Integer) metrics.getOrDefault("executionCount", 0) + 1);
        
        executionMetrics.put(metricsKey, metrics);
    }
    
    /**
     * Get workflow statistics using HashMap operations
     * 
     * @return workflow statistics
     */
    public Map<String, Object> getWorkflowStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Production-grade: Statistics using HashMap
        stats.put("workflowCacheSize", workflowCache.size());
        stats.put("executionMetrics", new HashMap<>(executionMetrics));
        stats.put("availableWorkflows", Arrays.asList("BATCH_PROCESSING", "REAL_TIME_PROCESSING", "STREAM_PROCESSING"));
        stats.put("timestamp", LocalDateTime.now());
        
        // Using Streams for cache analysis
        Map<String, Object> cacheAnalysis = new HashMap<>();
        cacheAnalysis.put("workflowKeys", new ArrayList<>(workflowCache.keySet()));
        cacheAnalysis.put("workflowValues", workflowCache.values().stream()
                .limit(5)
                .collect(Collectors.toList()));
        
        stats.put("cacheAnalysis", cacheAnalysis);
        
        return stats;
    }
    
    /**
     * Clear workflow cache
     */
    public void clearWorkflowCache() {
        workflowCache.clear();
        executionMetrics.clear();
    }
}
