package com.backend.designpatterns.behavioral;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Netflix Production-Grade State Pattern Implementation
 * 
 * Demonstrates Netflix SDE-2 design pattern expertise:
 * - State pattern for user session and content state management
 * - Dynamic state transitions and behavior changes
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
public class DataProcessingState {

    // Production-grade: Thread-safe collections for concurrent access
    private final Map<String, Object> stateRegistry = new ConcurrentHashMap<>();
    private final Map<String, Object> stateMetrics = new ConcurrentHashMap<>();
    
    // Production-grade: State configuration
    private static final String STATE_VERSION = "2.0.0";
    
    /**
     * State context for data processing
     */
    public static class DataProcessingContext {
        private final String contextId;
        private final Map<String, Object> data;
        private final Map<String, Object> metadata;
        private final List<String> stateHistory;
        private final LocalDateTime createdAt;
        
        public DataProcessingContext(String contextId) {
            this.contextId = contextId;
            this.data = new HashMap<>();
            this.metadata = new HashMap<>();
            this.stateHistory = new ArrayList<>();
            this.createdAt = LocalDateTime.now();
        }
        
        // Getters and setters
        public String getContextId() { return contextId; }
        public Map<String, Object> getData() { return Collections.unmodifiableMap(data); }
        public Map<String, Object> getMetadata() { return Collections.unmodifiableMap(metadata); }
        public List<String> getStateHistory() { return Collections.unmodifiableList(stateHistory); }
        public LocalDateTime getCreatedAt() { return createdAt; }
        
        public void addData(String key, Object value) {
            data.put(key, value);
        }
        
        public void addMetadata(String key, Object value) {
            metadata.put(key, value);
        }
        
        public void addToStateHistory(String state) {
            stateHistory.add(state + " at " + LocalDateTime.now());
        }
        
        public Object getData(String key) {
            return data.get(key);
        }
        
        public Object getMetadata(String key) {
            return metadata.get(key);
        }
    }
    
    /**
     * Abstract state interface
     */
    public interface ProcessingState {
        String getStateName();
        Map<String, Object> processData(DataProcessingContext context);
        boolean canTransitionTo(String nextState);
        Map<String, Object> getStateInfo();
        List<String> getAvailableTransitions();
    }
    
    /**
     * Initial state - data received but not processed
     */
    public static class InitialState implements ProcessingState {
        @Override
        public String getStateName() {
            return "INITIAL";
        }
        
        @Override
        public Map<String, Object> processData(DataProcessingContext context) {
            Map<String, Object> result = new HashMap<>();
            
            // Production-grade: Initial processing using HashMap operations
            result.put("state", getStateName());
            result.put("contextId", context.getContextId());
            result.put("dataSize", context.getData().size());
            result.put("timestamp", LocalDateTime.now());
            
            // Add to state history
            context.addToStateHistory("Entered " + getStateName());
            
            // Validate data using Streams
            long nullDataCount = context.getData().values().stream()
                    .filter(Objects::isNull)
                    .count();
            
            result.put("nullDataCount", nullDataCount);
            result.put("isValid", nullDataCount == 0);
            
            return result;
        }
        
        @Override
        public boolean canTransitionTo(String nextState) {
            return Set.of("VALIDATING", "ERROR").contains(nextState);
        }
        
        @Override
        public Map<String, Object> getStateInfo() {
            Map<String, Object> info = new HashMap<>();
            info.put("stateName", getStateName());
            info.put("description", "Initial state when data is received");
            info.put("availableTransitions", getAvailableTransitions());
            info.put("createdAt", LocalDateTime.now());
            return info;
        }
        
        @Override
        public List<String> getAvailableTransitions() {
            return Arrays.asList("VALIDATING", "ERROR");
        }
    }
    
    /**
     * Validating state - data validation in progress
     */
    public static class ValidatingState implements ProcessingState {
        @Override
        public String getStateName() {
            return "VALIDATING";
        }
        
        @Override
        public Map<String, Object> processData(DataProcessingContext context) {
            Map<String, Object> result = new HashMap<>();
            
            // Production-grade: Validation using HashMap operations
            result.put("state", getStateName());
            result.put("contextId", context.getContextId());
            result.put("timestamp", LocalDateTime.now());
            
            // Add to state history
            context.addToStateHistory("Entered " + getStateName());
            
            // Validate data using Streams
            Map<String, Object> validationResults = new HashMap<>();
            List<String> errors = new ArrayList<>();
            
            // Method 1: Using entrySet iteration for validation
            for (Map.Entry<String, Object> entry : context.getData().entrySet()) {
                if (entry.getValue() == null) {
                    errors.add("Data key '" + entry.getKey() + "' has null value");
                } else if (entry.getValue() instanceof String) {
                    String strValue = (String) entry.getValue();
                    if (strValue.trim().isEmpty()) {
                        errors.add("Data key '" + entry.getKey() + "' has empty value");
                    }
                }
            }
            
            // Method 2: Using Streams for additional validation
            long emptyStringCount = context.getData().values().stream()
                    .filter(value -> value instanceof String)
                    .map(value -> (String) value)
                    .filter(String::isEmpty)
                    .count();
            
            if (emptyStringCount > 0) {
                errors.add("Found " + emptyStringCount + " empty string values");
            }
            
            validationResults.put("valid", errors.isEmpty());
            validationResults.put("errors", errors);
            validationResults.put("dataSize", context.getData().size());
            validationResults.put("validationTimestamp", LocalDateTime.now());
            
            result.put("validation", validationResults);
            context.addMetadata("validationPassed", errors.isEmpty());
            
            return result;
        }
        
        @Override
        public boolean canTransitionTo(String nextState) {
            return Set.of("PROCESSING", "ERROR").contains(nextState);
        }
        
        @Override
        public Map<String, Object> getStateInfo() {
            Map<String, Object> info = new HashMap<>();
            info.put("stateName", getStateName());
            info.put("description", "State for data validation");
            info.put("availableTransitions", getAvailableTransitions());
            info.put("createdAt", LocalDateTime.now());
            return info;
        }
        
        @Override
        public List<String> getAvailableTransitions() {
            return Arrays.asList("PROCESSING", "ERROR");
        }
    }
    
    /**
     * Processing state - data processing in progress
     */
    public static class ProcessingState implements ProcessingState {
        @Override
        public String getStateName() {
            return "PROCESSING";
        }
        
        @Override
        public Map<String, Object> processData(DataProcessingContext context) {
            Map<String, Object> result = new HashMap<>();
            
            // Production-grade: Processing using HashMap operations
            result.put("state", getStateName());
            result.put("contextId", context.getContextId());
            result.put("timestamp", LocalDateTime.now());
            
            // Add to state history
            context.addToStateHistory("Entered " + getStateName());
            
            // Process data using Streams
            Map<String, Object> processedData = new HashMap<>();
            
            // Method 1: Using entrySet iteration for data processing
            for (Map.Entry<String, Object> entry : context.getData().entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                if (value instanceof String) {
                    String strValue = (String) value;
                    processedData.put(key + "_processed", strValue.toUpperCase());
                    processedData.put(key + "_length", strValue.length());
                } else if (value instanceof Number) {
                    Number numValue = (Number) value;
                    processedData.put(key + "_processed", numValue.doubleValue() * 2);
                } else {
                    processedData.put(key + "_processed", value);
                }
            }
            
            // Method 2: Using Streams for data analysis
            Map<String, Long> dataTypeCounts = context.getData().values().stream()
                    .collect(Collectors.groupingBy(
                        value -> value.getClass().getSimpleName(),
                        Collectors.counting()
                    ));
            
            result.put("processedData", processedData);
            result.put("dataTypeCounts", dataTypeCounts);
            result.put("processingComplete", true);
            
            context.addMetadata("dataProcessed", true);
            
            return result;
        }
        
        @Override
        public boolean canTransitionTo(String nextState) {
            return Set.of("COMPLETED", "ERROR").contains(nextState);
        }
        
        @Override
        public Map<String, Object> getStateInfo() {
            Map<String, Object> info = new HashMap<>();
            info.put("stateName", getStateName());
            info.put("description", "State for data processing");
            info.put("availableTransitions", getAvailableTransitions());
            info.put("createdAt", LocalDateTime.now());
            return info;
        }
        
        @Override
        public List<String> getAvailableTransitions() {
            return Arrays.asList("COMPLETED", "ERROR");
        }
    }
    
    /**
     * Completed state - data processing completed
     */
    public static class CompletedState implements ProcessingState {
        @Override
        public String getStateName() {
            return "COMPLETED";
        }
        
        @Override
        public Map<String, Object> processData(DataProcessingContext context) {
            Map<String, Object> result = new HashMap<>();
            
            // Production-grade: Completion using HashMap operations
            result.put("state", getStateName());
            result.put("contextId", context.getContextId());
            result.put("timestamp", LocalDateTime.now());
            
            // Add to state history
            context.addToStateHistory("Entered " + getStateName());
            
            // Generate completion summary using HashMap operations
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalDataItems", context.getData().size());
            summary.put("stateHistory", context.getStateHistory());
            summary.put("processingTime", LocalDateTime.now().toEpochSecond(java.time.ZoneOffset.UTC) - 
                       context.getCreatedAt().toEpochSecond(java.time.ZoneOffset.UTC));
            summary.put("completionTimestamp", LocalDateTime.now());
            
            result.put("summary", summary);
            result.put("status", "SUCCESS");
            
            context.addMetadata("processingCompleted", true);
            
            return result;
        }
        
        @Override
        public boolean canTransitionTo(String nextState) {
            return Set.of("ARCHIVED").contains(nextState);
        }
        
        @Override
        public Map<String, Object> getStateInfo() {
            Map<String, Object> info = new HashMap<>();
            info.put("stateName", getStateName());
            info.put("description", "State when processing is completed");
            info.put("availableTransitions", getAvailableTransitions());
            info.put("createdAt", LocalDateTime.now());
            return info;
        }
        
        @Override
        public List<String> getAvailableTransitions() {
            return Arrays.asList("ARCHIVED");
        }
    }
    
    /**
     * Error state - processing failed
     */
    public static class ErrorState implements ProcessingState {
        @Override
        public String getStateName() {
            return "ERROR";
        }
        
        @Override
        public Map<String, Object> processData(DataProcessingContext context) {
            Map<String, Object> result = new HashMap<>();
            
            // Production-grade: Error handling using HashMap operations
            result.put("state", getStateName());
            result.put("contextId", context.getContextId());
            result.put("timestamp", LocalDateTime.now());
            
            // Add to state history
            context.addToStateHistory("Entered " + getStateName());
            
            // Generate error summary using HashMap operations
            Map<String, Object> errorSummary = new HashMap<>();
            errorSummary.put("errorState", getStateName());
            errorSummary.put("stateHistory", context.getStateHistory());
            errorSummary.put("errorTimestamp", LocalDateTime.now());
            errorSummary.put("canRetry", true);
            
            result.put("errorSummary", errorSummary);
            result.put("status", "ERROR");
            
            context.addMetadata("processingFailed", true);
            
            return result;
        }
        
        @Override
        public boolean canTransitionTo(String nextState) {
            return Set.of("INITIAL", "RETRY").contains(nextState);
        }
        
        @Override
        public Map<String, Object> getStateInfo() {
            Map<String, Object> info = new HashMap<>();
            info.put("stateName", getStateName());
            info.put("description", "State when processing fails");
            info.put("availableTransitions", getAvailableTransitions());
            info.put("createdAt", LocalDateTime.now());
            return info;
        }
        
        @Override
        public List<String> getAvailableTransitions() {
            return Arrays.asList("INITIAL", "RETRY");
        }
    }
    
    /**
     * State machine for managing state transitions
     */
    public static class DataProcessingStateMachine {
        private final Map<String, ProcessingState> states;
        private final Map<String, Object> transitionHistory;
        private ProcessingState currentState;
        private final DataProcessingContext context;
        
        public DataProcessingStateMachine(DataProcessingContext context) {
            this.context = context;
            this.states = new HashMap<>();
            this.transitionHistory = new HashMap<>();
            initializeStates();
            this.currentState = states.get("INITIAL");
        }
        
        /**
         * Initialize available states using HashMap operations
         */
        private void initializeStates() {
            // Production-grade: State initialization with HashMap
            states.put("INITIAL", new InitialState());
            states.put("VALIDATING", new ValidatingState());
            states.put("PROCESSING", new ProcessingState());
            states.put("COMPLETED", new CompletedState());
            states.put("ERROR", new ErrorState());
        }
        
        /**
         * Process data in current state
         * 
         * @return processing results
         */
        public Map<String, Object> processData() {
            if (currentState == null) {
                return createErrorResult("No current state available");
            }
            
            return currentState.processData(context);
        }
        
        /**
         * Transition to next state using HashMap operations
         * 
         * @param nextStateName name of next state
         * @return true if transition successful
         */
        public boolean transitionTo(String nextStateName) {
            if (currentState == null) {
                return false;
            }
            
            // Check if transition is allowed
            if (!currentState.canTransitionTo(nextStateName)) {
                return false;
            }
            
            // Get next state
            ProcessingState nextState = states.get(nextStateName);
            if (nextState == null) {
                return false;
            }
            
            // Record transition using HashMap operations
            String transitionKey = "transition_" + System.currentTimeMillis();
            Map<String, Object> transitionInfo = new HashMap<>();
            transitionInfo.put("fromState", currentState.getStateName());
            transitionInfo.put("toState", nextStateName);
            transitionInfo.put("timestamp", LocalDateTime.now());
            transitionInfo.put("contextId", context.getContextId());
            
            transitionHistory.put(transitionKey, transitionInfo);
            
            // Update current state
            currentState = nextState;
            
            return true;
        }
        
        /**
         * Get current state information
         * 
         * @return current state info
         */
        public Map<String, Object> getCurrentStateInfo() {
            if (currentState == null) {
                return createErrorResult("No current state available");
            }
            
            Map<String, Object> info = new HashMap<>(currentState.getStateInfo());
            info.put("contextId", context.getContextId());
            info.put("availableTransitions", currentState.getAvailableTransitions());
            
            return info;
        }
        
        /**
         * Get transition history using HashMap operations
         * 
         * @return transition history
         */
        public Map<String, Object> getTransitionHistory() {
            return new HashMap<>(transitionHistory);
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
            return errorResult;
        }
    }
    
    /**
     * Create state machine for data processing using HashMap operations
     * 
     * @param contextId context identifier
     * @return created state machine
     */
    public DataProcessingStateMachine createStateMachine(String contextId) {
        DataProcessingContext context = new DataProcessingContext(contextId);
        DataProcessingStateMachine stateMachine = new DataProcessingStateMachine(context);
        
        // Register state machine using HashMap operations
        stateRegistry.put(contextId, stateMachine);
        updateStateMetrics(contextId, "CREATED");
        
        return stateMachine;
    }
    
    /**
     * Process data through state machine using HashMap operations
     * 
     * @param contextId context identifier
     * @param data input data
     * @param stateSequence sequence of states to process
     * @return processing results
     */
    public Map<String, Object> processDataThroughStates(String contextId, Map<String, Object> data, List<String> stateSequence) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Get or create state machine using HashMap operations
            DataProcessingStateMachine stateMachine = (DataProcessingStateMachine) stateRegistry.get(contextId);
            if (stateMachine == null) {
                stateMachine = createStateMachine(contextId);
            }
            
            // Add data to context
            DataProcessingContext context = stateMachine.context;
            data.forEach(context::addData);
            
            Map<String, Object> results = new HashMap<>();
            List<Map<String, Object>> stateResults = new ArrayList<>();
            
            // Process through state sequence using HashMap operations
            for (String stateName : stateSequence) {
                // Transition to state
                if (!stateMachine.transitionTo(stateName)) {
                    return createErrorResult("Cannot transition to state: " + stateName);
                }
                
                // Process data in current state
                Map<String, Object> stateResult = stateMachine.processData();
                stateResults.add(stateResult);
            }
            
            results.put("contextId", contextId);
            results.put("stateResults", stateResults);
            results.put("totalProcessingTime", System.currentTimeMillis() - startTime);
            results.put("timestamp", LocalDateTime.now());
            results.put("stateVersion", STATE_VERSION);
            
            // Update execution metrics using HashMap operations
            updateExecutionMetrics(contextId, true, System.currentTimeMillis() - startTime);
            
            return results;
            
        } catch (Exception e) {
            updateExecutionMetrics(contextId, false, System.currentTimeMillis() - startTime);
            return createErrorResult("State processing failed: " + e.getMessage());
        }
    }
    
    /**
     * Update state metrics using HashMap operations
     * 
     * @param contextId context identifier
     * @param action action performed
     */
    private void updateStateMetrics(String contextId, String action) {
        String metricsKey = "state_" + contextId;
        Map<String, Object> metrics = (Map<String, Object>) stateMetrics.getOrDefault(metricsKey, new HashMap<>());
        
        metrics.put("lastAction", action);
        metrics.put("lastUpdate", LocalDateTime.now());
        metrics.put("actionCount", (Integer) metrics.getOrDefault("actionCount", 0) + 1);
        
        stateMetrics.put(metricsKey, metrics);
    }
    
    /**
     * Update execution metrics using HashMap operations
     * 
     * @param contextId context identifier
     * @param success whether execution was successful
     * @param executionTime execution time in milliseconds
     */
    private void updateExecutionMetrics(String contextId, boolean success, long executionTime) {
        String metricsKey = "execution_" + contextId;
        Map<String, Object> metrics = (Map<String, Object>) stateMetrics.getOrDefault(metricsKey, new HashMap<>());
        
        Integer totalExecutions = (Integer) metrics.getOrDefault("totalExecutions", 0);
        Integer successfulExecutions = (Integer) metrics.getOrDefault("successfulExecutions", 0);
        Long totalExecutionTime = (Long) metrics.getOrDefault("totalExecutionTime", 0L);
        
        metrics.put("totalExecutions", totalExecutions + 1);
        metrics.put("successfulExecutions", successfulExecutions + (success ? 1 : 0));
        metrics.put("totalExecutionTime", totalExecutionTime + executionTime);
        metrics.put("averageExecutionTime", (double) (totalExecutionTime + executionTime) / (totalExecutions + 1));
        metrics.put("lastExecution", LocalDateTime.now());
        
        stateMetrics.put(metricsKey, metrics);
    }
    
    /**
     * Get state machine statistics using HashMap operations
     * 
     * @return state machine statistics
     */
    public Map<String, Object> getStateMachineStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Production-grade: Statistics using HashMap
        stats.put("stateVersion", STATE_VERSION);
        stats.put("totalStateMachines", stateRegistry.size());
        stats.put("stateMetrics", new HashMap<>(stateMetrics));
        stats.put("timestamp", LocalDateTime.now());
        
        // Using Streams for state analysis
        Map<String, Object> stateAnalysis = new HashMap<>();
        stateRegistry.forEach((contextId, stateMachine) -> {
            if (stateMachine instanceof DataProcessingStateMachine) {
                DataProcessingStateMachine sm = (DataProcessingStateMachine) stateMachine;
                Map<String, Object> analysis = new HashMap<>();
                analysis.put("currentState", sm.currentState.getStateName());
                analysis.put("transitionHistory", sm.getTransitionHistory());
                stateAnalysis.put(contextId, analysis);
            }
        });
        
        stats.put("stateAnalysis", stateAnalysis);
        
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
        errorResult.put("stateVersion", STATE_VERSION);
        return errorResult;
    }
    
    /**
     * Clear state registry
     */
    public void clearStateMachines() {
        stateRegistry.clear();
        stateMetrics.clear();
    }
}
