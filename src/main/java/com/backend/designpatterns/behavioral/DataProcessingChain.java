package com.backend.designpatterns.behavioral;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Netflix Production-Grade Chain of Responsibility Pattern Implementation
 * 
 * Demonstrates Netflix SDE-2 design pattern expertise:
 * - Chain of Responsibility pattern for request processing pipelines
 * - Dynamic chain construction and execution
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
public class DataProcessingChain {

    // Production-grade: Thread-safe collections for concurrent access
    private final Map<String, Object> chainRegistry = new ConcurrentHashMap<>();
    private final Map<String, Object> executionMetrics = new ConcurrentHashMap<>();
    
    // Production-grade: Chain configuration
    private static final String CHAIN_VERSION = "2.0.0";
    
    /**
     * Request processing context
     */
    public static class ProcessingContext {
        private final String requestId;
        private final List<String> data;
        private final Map<String, Object> metadata;
        private final Map<String, Object> results;
        private final List<String> processingHistory;
        
        public ProcessingContext(String requestId, List<String> data) {
            this.requestId = requestId;
            this.data = new ArrayList<>(data);
            this.metadata = new HashMap<>();
            this.results = new HashMap<>();
            this.processingHistory = new ArrayList<>();
        }
        
        // Getters and setters
        public String getRequestId() { return requestId; }
        public List<String> getData() { return Collections.unmodifiableList(data); }
        public Map<String, Object> getMetadata() { return Collections.unmodifiableMap(metadata); }
        public Map<String, Object> getResults() { return Collections.unmodifiableMap(results); }
        public List<String> getProcessingHistory() { return Collections.unmodifiableList(processingHistory); }
        
        public void addMetadata(String key, Object value) {
            metadata.put(key, value);
        }
        
        public void addResult(String key, Object value) {
            results.put(key, value);
        }
        
        public void addToHistory(String step) {
            processingHistory.add(step + " at " + LocalDateTime.now());
        }
        
        public Object getMetadata(String key) {
            return metadata.get(key);
        }
        
        public Object getResult(String key) {
            return results.get(key);
        }
    }
    
    /**
     * Abstract handler in the chain
     */
    public abstract static class ProcessingHandler {
        protected ProcessingHandler nextHandler;
        protected final String handlerName;
        protected final Map<String, Object> handlerMetrics;
        
        public ProcessingHandler(String handlerName) {
            this.handlerName = handlerName;
            this.handlerMetrics = new HashMap<>();
            this.handlerMetrics.put("handlerName", handlerName);
            this.handlerMetrics.put("createdAt", LocalDateTime.now());
            this.handlerMetrics.put("processedRequests", 0);
            this.handlerMetrics.put("successfulRequests", 0);
            this.handlerMetrics.put("failedRequests", 0);
        }
        
        /**
         * Set next handler in chain
         */
        public ProcessingHandler setNext(ProcessingHandler nextHandler) {
            this.nextHandler = nextHandler;
            return nextHandler;
        }
        
        /**
         * Process request and pass to next handler
         */
        public ProcessingContext process(ProcessingContext context) {
            long startTime = System.currentTimeMillis();
            
            try {
                // Add to processing history
                context.addToHistory("Started " + handlerName);
                
                // Process request
                ProcessingContext result = doProcess(context);
                
                // Update metrics
                updateMetrics(true, System.currentTimeMillis() - startTime);
                
                // Pass to next handler
                if (nextHandler != null) {
                    result.addToHistory("Passed to " + nextHandler.handlerName);
                    return nextHandler.process(result);
                } else {
                    result.addToHistory("Chain completed");
                    return result;
                }
                
            } catch (Exception e) {
                context.addToHistory("Error in " + handlerName + ": " + e.getMessage());
                updateMetrics(false, System.currentTimeMillis() - startTime);
                throw new RuntimeException("Handler " + handlerName + " failed", e);
            }
        }
        
        /**
         * Abstract method to be implemented by concrete handlers
         */
        protected abstract ProcessingContext doProcess(ProcessingContext context);
        
        /**
         * Check if handler can process the request
         */
        public abstract boolean canHandle(ProcessingContext context);
        
        /**
         * Get handler information
         */
        public Map<String, Object> getHandlerInfo() {
            Map<String, Object> info = new HashMap<>(handlerMetrics);
            info.put("nextHandler", nextHandler != null ? nextHandler.handlerName : "NONE");
            return info;
        }
        
        /**
         * Update handler metrics using HashMap operations
         */
        private void updateMetrics(boolean success, long processingTime) {
            Integer processedRequests = (Integer) handlerMetrics.getOrDefault("processedRequests", 0);
            Integer successfulRequests = (Integer) handlerMetrics.getOrDefault("successfulRequests", 0);
            Integer failedRequests = (Integer) handlerMetrics.getOrDefault("failedRequests", 0);
            Long totalProcessingTime = (Long) handlerMetrics.getOrDefault("totalProcessingTime", 0L);
            
            handlerMetrics.put("processedRequests", processedRequests + 1);
            handlerMetrics.put("successfulRequests", successfulRequests + (success ? 1 : 0));
            handlerMetrics.put("failedRequests", failedRequests + (success ? 0 : 1));
            handlerMetrics.put("totalProcessingTime", totalProcessingTime + processingTime);
            handlerMetrics.put("averageProcessingTime", 
                (double) (totalProcessingTime + processingTime) / (processedRequests + 1));
            handlerMetrics.put("lastProcessed", LocalDateTime.now());
        }
    }
    
    /**
     * Validation handler
     */
    public static class ValidationHandler extends ProcessingHandler {
        public ValidationHandler() {
            super("ValidationHandler");
        }
        
        @Override
        public boolean canHandle(ProcessingContext context) {
            return context.getData() != null && !context.getData().isEmpty();
        }
        
        @Override
        protected ProcessingContext doProcess(ProcessingContext context) {
            // Production-grade: Validation using HashMap operations
            Map<String, Object> validationResults = new HashMap<>();
            List<String> errors = new ArrayList<>();
            
            // Validate data using Streams
            long nullCount = context.getData().stream()
                    .filter(Objects::isNull)
                    .count();
            
            if (nullCount > 0) {
                errors.add("Data contains " + nullCount + " null values");
            }
            
            // Validate data size
            if (context.getData().size() > 10000) {
                errors.add("Data size exceeds maximum limit of 10000");
            }
            
            validationResults.put("valid", errors.isEmpty());
            validationResults.put("errors", errors);
            validationResults.put("dataSize", context.getData().size());
            validationResults.put("timestamp", LocalDateTime.now());
            
            context.addResult("validation", validationResults);
            context.addMetadata("validationPassed", errors.isEmpty());
            
            if (!errors.isEmpty()) {
                throw new RuntimeException("Validation failed: " + String.join(", ", errors));
            }
            
            return context;
        }
    }
    
    /**
     * Authentication handler
     */
    public static class AuthenticationHandler extends ProcessingHandler {
        private final Set<String> validTokens;
        
        public AuthenticationHandler() {
            super("AuthenticationHandler");
            this.validTokens = Set.of("admin_token", "user_token", "service_token");
        }
        
        @Override
        public boolean canHandle(ProcessingContext context) {
            return context.getMetadata("token") != null;
        }
        
        @Override
        protected ProcessingContext doProcess(ProcessingContext context) {
            // Production-grade: Authentication using HashMap operations
            String token = (String) context.getMetadata("token");
            
            if (token == null || !validTokens.contains(token)) {
                throw new RuntimeException("Invalid or missing authentication token");
            }
            
            Map<String, Object> authResults = new HashMap<>();
            authResults.put("authenticated", true);
            authResults.put("token", token);
            authResults.put("timestamp", LocalDateTime.now());
            
            context.addResult("authentication", authResults);
            context.addMetadata("authenticated", true);
            
            return context;
        }
    }
    
    /**
     * Data transformation handler
     */
    public static class DataTransformationHandler extends ProcessingHandler {
        public DataTransformationHandler() {
            super("DataTransformationHandler");
        }
        
        @Override
        public boolean canHandle(ProcessingContext context) {
            return context.getData() != null && !context.getData().isEmpty();
        }
        
        @Override
        protected ProcessingContext doProcess(ProcessingContext context) {
            // Production-grade: Data transformation using Streams
            List<String> transformedData = context.getData().stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .filter(item -> !item.isEmpty())
                    .collect(Collectors.toList());
            
            Map<String, Object> transformationResults = new HashMap<>();
            transformationResults.put("originalSize", context.getData().size());
            transformationResults.put("transformedSize", transformedData.size());
            transformationResults.put("transformedData", transformedData);
            transformationResults.put("timestamp", LocalDateTime.now());
            
            context.addResult("transformation", transformationResults);
            context.addMetadata("dataTransformed", true);
            
            return context;
        }
    }
    
    /**
     * Data processing handler
     */
    public static class DataProcessingHandler extends ProcessingHandler {
        public DataProcessingHandler() {
            super("DataProcessingHandler");
        }
        
        @Override
        public boolean canHandle(ProcessingContext context) {
            return context.getMetadata("dataTransformed") != null && 
                   (Boolean) context.getMetadata("dataTransformed");
        }
        
        @Override
        protected ProcessingContext doProcess(ProcessingContext context) {
            // Production-grade: Data processing using HashMap operations
            Map<String, Object> transformationResults = (Map<String, Object>) context.getResult("transformation");
            @SuppressWarnings("unchecked")
            List<String> transformedData = (List<String>) transformationResults.get("transformedData");
            
            Map<String, Object> processingResults = new HashMap<>();
            
            // Using Streams for data analysis
            Map<String, Long> wordFrequency = transformedData.stream()
                    .flatMap(item -> Arrays.stream(item.split("\\s+")))
                    .filter(word -> !word.isEmpty())
                    .collect(Collectors.groupingBy(
                        word -> word,
                        Collectors.counting()
                    ));
            
            processingResults.put("wordFrequency", wordFrequency);
            processingResults.put("uniqueWords", wordFrequency.size());
            processingResults.put("totalWords", wordFrequency.values().stream()
                    .mapToLong(Long::longValue)
                    .sum());
            
            // Using HashMap for character analysis
            Map<Character, Long> charFrequency = transformedData.stream()
                    .flatMapToInt(String::chars)
                    .mapToObj(ch -> (char) ch)
                    .collect(Collectors.groupingBy(
                        Character::toLowerCase,
                        Collectors.counting()
                    ));
            
            processingResults.put("charFrequency", charFrequency);
            processingResults.put("timestamp", LocalDateTime.now());
            
            context.addResult("processing", processingResults);
            context.addMetadata("dataProcessed", true);
            
            return context;
        }
    }
    
    /**
     * Response formatting handler
     */
    public static class ResponseFormattingHandler extends ProcessingHandler {
        public ResponseFormattingHandler() {
            super("ResponseFormattingHandler");
        }
        
        @Override
        public boolean canHandle(ProcessingContext context) {
            return context.getMetadata("dataProcessed") != null && 
                   (Boolean) context.getMetadata("dataProcessed");
        }
        
        @Override
        protected ProcessingContext doProcess(ProcessingContext context) {
            // Production-grade: Response formatting using HashMap operations
            Map<String, Object> finalResponse = new HashMap<>();
            
            // Method 1: Using entrySet iteration for result aggregation
            for (Map.Entry<String, Object> entry : context.getResults().entrySet()) {
                finalResponse.put(entry.getKey(), entry.getValue());
            }
            
            // Add metadata using HashMap operations
            finalResponse.put("requestId", context.getRequestId());
            finalResponse.put("processingHistory", context.getProcessingHistory());
            finalResponse.put("status", "SUCCESS");
            finalResponse.put("timestamp", LocalDateTime.now());
            finalResponse.put("chainVersion", CHAIN_VERSION);
            
            context.addResult("finalResponse", finalResponse);
            context.addMetadata("responseFormatted", true);
            
            return context;
        }
    }
    
    /**
     * Create processing chain using HashMap operations
     * 
     * @param chainName name of the chain
     * @param handlerTypes types of handlers to include
     * @return created processing chain
     */
    public ProcessingHandler createChain(String chainName, List<String> handlerTypes) {
        // Production-grade: Chain creation with HashMap operations
        Map<String, ProcessingHandler> handlerMap = new HashMap<>();
        
        // Create handlers based on types
        for (String handlerType : handlerTypes) {
            ProcessingHandler handler = createHandler(handlerType);
            if (handler != null) {
                handlerMap.put(handlerType, handler);
            }
        }
        
        // Build chain using HashMap iteration
        ProcessingHandler firstHandler = null;
        ProcessingHandler currentHandler = null;
        
        for (String handlerType : handlerTypes) {
            ProcessingHandler handler = handlerMap.get(handlerType);
            if (handler != null) {
                if (firstHandler == null) {
                    firstHandler = handler;
                    currentHandler = handler;
                } else {
                    currentHandler = currentHandler.setNext(handler);
                }
            }
        }
        
        // Register chain using HashMap operations
        chainRegistry.put(chainName, firstHandler);
        updateChainMetrics(chainName, "CREATED");
        
        return firstHandler;
    }
    
    /**
     * Create handler based on type using HashMap operations
     * 
     * @param handlerType type of handler
     * @return created handler
     */
    private ProcessingHandler createHandler(String handlerType) {
        // Production-grade: Handler creation with HashMap
        Map<String, java.util.function.Supplier<ProcessingHandler>> handlerFactories = 
            new HashMap<>();
        
        handlerFactories.put("VALIDATION", ValidationHandler::new);
        handlerFactories.put("AUTHENTICATION", AuthenticationHandler::new);
        handlerFactories.put("TRANSFORMATION", DataTransformationHandler::new);
        handlerFactories.put("PROCESSING", DataProcessingHandler::new);
        handlerFactories.put("FORMATTING", ResponseFormattingHandler::new);
        
        return handlerFactories.getOrDefault(handlerType, () -> null).get();
    }
    
    /**
     * Process request through chain using HashMap operations
     * 
     * @param chainName name of the chain
     * @param data input data
     * @param metadata request metadata
     * @return processing results
     */
    public Map<String, Object> processRequest(String chainName, List<String> data, Map<String, Object> metadata) {
        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();
        
        try {
            // Get chain from registry using HashMap operations
            ProcessingHandler chain = (ProcessingHandler) chainRegistry.get(chainName);
            if (chain == null) {
                return createErrorResult("Chain not found: " + chainName);
            }
            
            // Create processing context
            ProcessingContext context = new ProcessingContext(requestId, data);
            
            // Add metadata using HashMap operations
            if (metadata != null) {
                metadata.forEach(context::addMetadata);
            }
            
            // Process through chain
            ProcessingContext result = chain.process(context);
            
            // Extract final response
            Map<String, Object> finalResponse = (Map<String, Object>) result.getResult("finalResponse");
            if (finalResponse == null) {
                finalResponse = new HashMap<>(result.getResults());
            }
            
            finalResponse.put("chainName", chainName);
            finalResponse.put("totalProcessingTime", System.currentTimeMillis() - startTime);
            
            // Update execution metrics using HashMap operations
            updateExecutionMetrics(chainName, true, System.currentTimeMillis() - startTime);
            
            return finalResponse;
            
        } catch (Exception e) {
            updateExecutionMetrics(chainName, false, System.currentTimeMillis() - startTime);
            return createErrorResult("Chain processing failed: " + e.getMessage());
        }
    }
    
    /**
     * Update chain metrics using HashMap operations
     * 
     * @param chainName chain name
     * @param action action performed
     */
    private void updateChainMetrics(String chainName, String action) {
        String metricsKey = "chain_" + chainName;
        Map<String, Object> metrics = (Map<String, Object>) executionMetrics.getOrDefault(metricsKey, new HashMap<>());
        
        metrics.put("lastAction", action);
        metrics.put("lastUpdate", LocalDateTime.now());
        metrics.put("actionCount", (Integer) metrics.getOrDefault("actionCount", 0) + 1);
        
        executionMetrics.put(metricsKey, metrics);
    }
    
    /**
     * Update execution metrics using HashMap operations
     * 
     * @param chainName chain name
     * @param success whether execution was successful
     * @param executionTime execution time in milliseconds
     */
    private void updateExecutionMetrics(String chainName, boolean success, long executionTime) {
        String metricsKey = "execution_" + chainName;
        Map<String, Object> metrics = (Map<String, Object>) executionMetrics.getOrDefault(metricsKey, new HashMap<>());
        
        Integer totalExecutions = (Integer) metrics.getOrDefault("totalExecutions", 0);
        Integer successfulExecutions = (Integer) metrics.getOrDefault("successfulExecutions", 0);
        Long totalExecutionTime = (Long) metrics.getOrDefault("totalExecutionTime", 0L);
        
        metrics.put("totalExecutions", totalExecutions + 1);
        metrics.put("successfulExecutions", successfulExecutions + (success ? 1 : 0));
        metrics.put("totalExecutionTime", totalExecutionTime + executionTime);
        metrics.put("averageExecutionTime", (double) (totalExecutionTime + executionTime) / (totalExecutions + 1));
        metrics.put("lastExecution", LocalDateTime.now());
        
        executionMetrics.put(metricsKey, metrics);
    }
    
    /**
     * Get chain statistics using HashMap operations
     * 
     * @return chain statistics
     */
    public Map<String, Object> getChainStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Production-grade: Statistics using HashMap
        stats.put("chainVersion", CHAIN_VERSION);
        stats.put("totalChains", chainRegistry.size());
        stats.put("executionMetrics", new HashMap<>(executionMetrics));
        stats.put("timestamp", LocalDateTime.now());
        
        // Using Streams for chain analysis
        Map<String, Object> chainAnalysis = new HashMap<>();
        chainRegistry.forEach((chainName, chain) -> {
            Map<String, Object> analysis = new HashMap<>();
            analysis.put("handlerName", chain.handlerName);
            analysis.put("handlerInfo", chain.getHandlerInfo());
            chainAnalysis.put(chainName, analysis);
        });
        
        stats.put("chainAnalysis", chainAnalysis);
        
        return stats;
    }
    
    /**
     * Create error result using HashMap
     * 
     * @param errorMessage the error message
     * @return error result map
     */
    private Map<String, Object> createErrorResult(String errorMessage) {
        Map<String, Object> errorResult = new HashMap<>();
        errorResult.put("status", "ERROR");
        errorResult.put("error", errorMessage);
        errorResult.put("timestamp", LocalDateTime.now());
        errorResult.put("chainVersion", CHAIN_VERSION);
        return errorResult;
    }
    
    /**
     * Clear chain registry
     */
    public void clearChains() {
        chainRegistry.clear();
        executionMetrics.clear();
    }
}
