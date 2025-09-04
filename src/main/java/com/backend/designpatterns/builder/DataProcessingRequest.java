package com.backend.designpatterns.builder;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Netflix Production-Grade Builder Pattern Implementation
 * 
 * Demonstrates Netflix SDE-2 design pattern expertise:
 * - Builder pattern with fluent interface
 * - Advanced HashMap operations and iterations
 * - Stream operations for data validation
 * - Optional usage for null safety
 * - Collection framework best practices
 * - Immutable object creation
 * 
 * @author Netflix Backend Team
 * @version 1.0.0
 */
public class DataProcessingRequest {
    
    // Production-grade: Immutable fields
    private final String requestId;
    private final String strategyType;
    private final List<String> data;
    private final Map<String, Object> parameters;
    private final LocalDateTime timestamp;
    private final String priority;
    private final boolean async;
    
    // Production-grade: Private constructor for immutability
    private DataProcessingRequest(Builder builder) {
        this.requestId = builder.requestId;
        this.strategyType = builder.strategyType;
        this.data = new ArrayList<>(builder.data);
        this.parameters = new HashMap<>(builder.parameters);
        this.timestamp = builder.timestamp;
        this.priority = builder.priority;
        this.async = builder.async;
    }
    
    // Production-grade: Getters with Optional for null safety
    public String getRequestId() { return requestId; }
    
    public String getStrategyType() { return strategyType; }
    
    public List<String> getData() { return Collections.unmodifiableList(data); }
    
    public Map<String, Object> getParameters() { return Collections.unmodifiableMap(parameters); }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    
    public String getPriority() { return priority; }
    
    public boolean isAsync() { return async; }
    
    /**
     * Get parameter value with Optional for null safety
     * 
     * @param key the parameter key
     * @return Optional containing the parameter value
     */
    public Optional<Object> getParameter(String key) {
        return Optional.ofNullable(parameters.get(key));
    }
    
    /**
     * Get parameter with default value using Optional
     * 
     * @param key the parameter key
     * @param defaultValue the default value
     * @return parameter value or default
     */
    public Object getParameterOrDefault(String key, Object defaultValue) {
        return parameters.getOrDefault(key, defaultValue);
    }
    
    /**
     * Validate request using Stream operations
     * 
     * @return validation results
     */
    public Map<String, Object> validate() {
        Map<String, Object> validationResults = new HashMap<>();
        
        // Production-grade: Validation using Streams and Optional
        List<String> errors = new ArrayList<>();
        
        // Validate required fields
        if (requestId == null || requestId.trim().isEmpty()) {
            errors.add("Request ID is required");
        }
        
        if (strategyType == null || strategyType.trim().isEmpty()) {
            errors.add("Strategy type is required");
        }
        
        // Validate data using Stream operations
        if (data == null || data.isEmpty()) {
            errors.add("Data cannot be null or empty");
        } else {
            // Using Streams for data validation
            long nullDataCount = data.stream()
                    .filter(Objects::isNull)
                    .count();
            
            if (nullDataCount > 0) {
                errors.add("Data contains " + nullDataCount + " null values");
            }
            
            // Validate data size
            if (data.size() > 10000) {
                errors.add("Data size exceeds maximum limit of 10000");
            }
        }
        
        // Validate parameters using HashMap iteration
        if (parameters != null) {
            // Method 1: Using entrySet iteration
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                if (entry.getKey() == null || entry.getKey().trim().isEmpty()) {
                    errors.add("Parameter key cannot be null or empty");
                }
            }
            
            // Method 2: Using Streams for validation
            long invalidKeys = parameters.entrySet().stream()
                    .filter(entry -> entry.getKey() == null || entry.getKey().trim().isEmpty())
                    .count();
            
            if (invalidKeys > 0) {
                errors.add("Found " + invalidKeys + " invalid parameter keys");
            }
        }
        
        validationResults.put("valid", errors.isEmpty());
        validationResults.put("errors", errors);
        validationResults.put("timestamp", LocalDateTime.now());
        
        return validationResults;
    }
    
    /**
     * Get request summary using HashMap operations
     * 
     * @return request summary
     */
    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        summary.put("requestId", requestId);
        summary.put("strategyType", strategyType);
        summary.put("dataSize", data.size());
        summary.put("parameterCount", parameters.size());
        summary.put("timestamp", timestamp);
        summary.put("priority", priority);
        summary.put("async", async);
        
        // Production-grade: Data analysis using Streams
        if (!data.isEmpty()) {
            // Using Streams for data analysis
            IntSummaryStatistics lengthStats = data.stream()
                    .filter(Objects::nonNull)
                    .mapToInt(String::length)
                    .summaryStatistics();
            
            summary.put("dataLengthStats", lengthStats);
            summary.put("uniqueDataItems", data.stream().distinct().count());
        }
        
        return summary;
    }
    
    /**
     * Netflix Production-Grade Builder Class
     */
    public static class Builder {
        // Production-grade: Builder fields
        private String requestId;
        private String strategyType;
        private List<String> data = new ArrayList<>();
        private Map<String, Object> parameters = new HashMap<>();
        private LocalDateTime timestamp = LocalDateTime.now();
        private String priority = "NORMAL";
        private boolean async = false;
        
        // Production-grade: Fluent interface methods
        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }
        
        public Builder strategyType(String strategyType) {
            this.strategyType = strategyType;
            return this;
        }
        
        public Builder data(List<String> data) {
            this.data = new ArrayList<>(data);
            return this;
        }
        
        public Builder addData(String item) {
            this.data.add(item);
            return this;
        }
        
        public Builder addAllData(Collection<String> items) {
            this.data.addAll(items);
            return this;
        }
        
        public Builder parameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }
        
        public Builder parameters(Map<String, Object> parameters) {
            this.parameters.putAll(parameters);
            return this;
        }
        
        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder priority(String priority) {
            this.priority = priority;
            return this;
        }
        
        public Builder async(boolean async) {
            this.async = async;
            return this;
        }
        
        /**
         * Build the request with validation
         * 
         * @return DataProcessingRequest instance
         * @throws IllegalArgumentException if validation fails
         */
        public DataProcessingRequest build() {
            DataProcessingRequest request = new DataProcessingRequest(this);
            
            // Production-grade: Validation before building
            Map<String, Object> validation = request.validate();
            if (!(Boolean) validation.get("valid")) {
                List<String> errors = (List<String>) validation.get("errors");
                throw new IllegalArgumentException("Invalid request: " + String.join(", ", errors));
            }
            
            return request;
        }
        
        /**
         * Build without validation (for testing purposes)
         * 
         * @return DataProcessingRequest instance
         */
        public DataProcessingRequest buildWithoutValidation() {
            return new DataProcessingRequest(this);
        }
    }
    
    /**
     * Create builder instance
     * 
     * @return Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    @Override
    public String toString() {
        return "DataProcessingRequest{" +
                "requestId='" + requestId + '\'' +
                ", strategyType='" + strategyType + '\'' +
                ", dataSize=" + data.size() +
                ", parameterCount=" + parameters.size() +
                ", timestamp=" + timestamp +
                ", priority='" + priority + '\'' +
                ", async=" + async +
                '}';
    }
}
