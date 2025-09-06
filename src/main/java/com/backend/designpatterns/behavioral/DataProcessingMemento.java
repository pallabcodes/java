package com.backend.designpatterns.behavioral;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Netflix Production-Grade Memento Pattern Implementation
 * 
 * Demonstrates Netflix SDE-2 design pattern expertise:
 * - Memento pattern for undo/redo and state restoration
 * - State snapshots and restoration capabilities
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
public class DataProcessingMemento {

    // Production-grade: Thread-safe collections for concurrent access
    private final Map<String, Object> mementoRegistry = new ConcurrentHashMap<>();
    private final Map<String, Object> mementoMetrics = new ConcurrentHashMap<>();
    
    // Production-grade: Memento configuration
    private static final String MEMENTO_VERSION = "2.0.0";
    private static final int MAX_MEMENTO_HISTORY = 100;
    
    /**
     * Memento interface for state snapshots
     */
    public interface DataProcessingMementoInterface {
        String getMementoId();
        LocalDateTime getCreatedAt();
        Map<String, Object> getState();
        Map<String, Object> getMetadata();
    }
    
    /**
     * Concrete memento implementation
     */
    public static class DataProcessingMementoImpl implements DataProcessingMementoInterface {
        private final String mementoId;
        private final LocalDateTime createdAt;
        private final Map<String, Object> state;
        private final Map<String, Object> metadata;
        
        public DataProcessingMementoImpl(String mementoId, Map<String, Object> state, Map<String, Object> metadata) {
            this.mementoId = mementoId;
            this.createdAt = LocalDateTime.now();
            this.state = new HashMap<>(state);
            this.metadata = new HashMap<>(metadata);
        }
        
        @Override
        public String getMementoId() { return mementoId; }
        
        @Override
        public LocalDateTime getCreatedAt() { return createdAt; }
        
        @Override
        public Map<String, Object> getState() { return Collections.unmodifiableMap(state); }
        
        @Override
        public Map<String, Object> getMetadata() { return Collections.unmodifiableMap(metadata); }
    }
    
    /**
     * Originator class that creates and restores mementos
     */
    public static class DataProcessingOriginator {
        private final String originatorId;
        private Map<String, Object> currentState;
        private final Map<String, Object> metadata;
        private final List<DataProcessingMementoInterface> mementoHistory;
        
        public DataProcessingOriginator(String originatorId) {
            this.originatorId = originatorId;
            this.currentState = new HashMap<>();
            this.metadata = new HashMap<>();
            this.mementoHistory = new ArrayList<>();
        }
        
        /**
         * Get current state using HashMap operations
         * 
         * @return current state
         */
        public Map<String, Object> getCurrentState() {
            return Collections.unmodifiableMap(currentState);
        }
        
        /**
         * Set current state using HashMap operations
         * 
         * @param state new state
         */
        public void setCurrentState(Map<String, Object> state) {
            this.currentState = new HashMap<>(state);
        }
        
        /**
         * Add data to current state using HashMap operations
         * 
         * @param key data key
         * @param value data value
         */
        public void addData(String key, Object value) {
            currentState.put(key, value);
        }
        
        /**
         * Add metadata using HashMap operations
         * 
         * @param key metadata key
         * @param value metadata value
         */
        public void addMetadata(String key, Object value) {
            metadata.put(key, value);
        }
        
        /**
         * Create memento from current state using HashMap operations
         * 
         * @return created memento
         */
        public DataProcessingMementoInterface createMemento() {
            String mementoId = UUID.randomUUID().toString();
            
            // Production-grade: Memento creation with HashMap operations
            Map<String, Object> mementoMetadata = new HashMap<>(metadata);
            mementoMetadata.put("originatorId", originatorId);
            mementoMetadata.put("stateSize", currentState.size());
            mementoMetadata.put("createdAt", LocalDateTime.now());
            
            DataProcessingMementoInterface memento = new DataProcessingMementoImpl(
                mementoId, currentState, mementoMetadata
            );
            
            // Add to history using HashMap operations
            mementoHistory.add(memento);
            
            // Manage history size using Streams
            if (mementoHistory.size() > MAX_MEMENTO_HISTORY) {
                mementoHistory.remove(0); // Remove oldest memento
            }
            
            return memento;
        }
        
        /**
         * Restore state from memento using HashMap operations
         * 
         * @param memento memento to restore from
         */
        public void restoreFromMemento(DataProcessingMementoInterface memento) {
            if (memento == null) {
                throw new IllegalArgumentException("Memento cannot be null");
            }
            
            // Production-grade: State restoration with HashMap operations
            this.currentState = new HashMap<>(memento.getState());
            
            // Update metadata using HashMap operations
            Map<String, Object> mementoMetadata = memento.getMetadata();
            mementoMetadata.forEach((key, value) -> {
                if (!key.equals("originatorId")) { // Don't overwrite originator ID
                    metadata.put(key, value);
                }
            });
            
            metadata.put("lastRestored", LocalDateTime.now());
            metadata.put("restoredFromMemento", memento.getMementoId());
        }
        
        /**
         * Get memento history using HashMap operations
         * 
         * @return memento history
         */
        public List<DataProcessingMementoInterface> getMementoHistory() {
            return new ArrayList<>(mementoHistory);
        }
        
        /**
         * Get originator information using HashMap operations
         * 
         * @return originator information
         */
        public Map<String, Object> getOriginatorInfo() {
            Map<String, Object> info = new HashMap<>();
            info.put("originatorId", originatorId);
            info.put("currentStateSize", currentState.size());
            info.put("metadataSize", metadata.size());
            info.put("mementoHistorySize", mementoHistory.size());
            info.put("lastUpdated", LocalDateTime.now());
            
            return info;
        }
    }
    
    /**
     * Caretaker class that manages mementos
     */
    public static class DataProcessingCaretaker {
        private final Map<String, DataProcessingOriginator> originators;
        private final Map<String, Object> caretakerMetrics;
        
        public DataProcessingCaretaker() {
            this.originators = new ConcurrentHashMap<>();
            this.caretakerMetrics = new HashMap<>();
            this.caretakerMetrics.put("createdAt", LocalDateTime.now());
            this.caretakerMetrics.put("totalOriginators", 0);
            this.caretakerMetrics.put("totalMementos", 0);
        }
        
        /**
         * Create originator using HashMap operations
         * 
         * @param originatorId originator identifier
         * @return created originator
         */
        public DataProcessingOriginator createOriginator(String originatorId) {
            DataProcessingOriginator originator = new DataProcessingOriginator(originatorId);
            originators.put(originatorId, originator);
            
            // Update metrics using HashMap operations
            updateCaretakerMetrics("ORIGINATOR_CREATED");
            
            return originator;
        }
        
        /**
         * Get originator using HashMap operations
         * 
         * @param originatorId originator identifier
         * @return originator or null if not found
         */
        public DataProcessingOriginator getOriginator(String originatorId) {
            return originators.get(originatorId);
        }
        
        /**
         * Save memento for originator using HashMap operations
         * 
         * @param originatorId originator identifier
         * @return created memento
         */
        public DataProcessingMementoInterface saveMemento(String originatorId) {
            DataProcessingOriginator originator = originators.get(originatorId);
            if (originator == null) {
                throw new IllegalArgumentException("Originator not found: " + originatorId);
            }
            
            DataProcessingMementoInterface memento = originator.createMemento();
            
            // Update metrics using HashMap operations
            updateCaretakerMetrics("MEMENTO_SAVED");
            
            return memento;
        }
        
        /**
         * Restore memento for originator using HashMap operations
         * 
         * @param originatorId originator identifier
         * @param mementoId memento identifier
         * @return true if restoration successful
         */
        public boolean restoreMemento(String originatorId, String mementoId) {
            DataProcessingOriginator originator = originators.get(originatorId);
            if (originator == null) {
                return false;
            }
            
            // Find memento in history using Streams
            Optional<DataProcessingMementoInterface> memento = originator.getMementoHistory().stream()
                    .filter(m -> m.getMementoId().equals(mementoId))
                    .findFirst();
            
            if (memento.isPresent()) {
                originator.restoreFromMemento(memento.get());
                updateCaretakerMetrics("MEMENTO_RESTORED");
                return true;
            }
            
            return false;
        }
        
        /**
         * Get all originators using HashMap operations
         * 
         * @return map of originators
         */
        public Map<String, DataProcessingOriginator> getAllOriginators() {
            return new HashMap<>(originators);
        }
        
        /**
         * Update caretaker metrics using HashMap operations
         * 
         * @param action action performed
         */
        private void updateCaretakerMetrics(String action) {
            caretakerMetrics.put("lastAction", action);
            caretakerMetrics.put("lastUpdate", LocalDateTime.now());
            
            if ("ORIGINATOR_CREATED".equals(action)) {
                Integer totalOriginators = (Integer) caretakerMetrics.getOrDefault("totalOriginators", 0);
                caretakerMetrics.put("totalOriginators", totalOriginators + 1);
            } else if ("MEMENTO_SAVED".equals(action)) {
                Integer totalMementos = (Integer) caretakerMetrics.getOrDefault("totalMementos", 0);
                caretakerMetrics.put("totalMementos", totalMementos + 1);
            }
        }
        
        /**
         * Get caretaker statistics using HashMap operations
         * 
         * @return caretaker statistics
         */
        public Map<String, Object> getCaretakerStatistics() {
            Map<String, Object> stats = new HashMap<>(caretakerMetrics);
            
            // Using Streams for originator analysis
            Map<String, Object> originatorAnalysis = new HashMap<>();
            originators.forEach((originatorId, originator) -> {
                Map<String, Object> analysis = new HashMap<>();
                analysis.put("mementoHistorySize", originator.getMementoHistory().size());
                analysis.put("currentStateSize", originator.getCurrentState().size());
                analysis.put("originatorInfo", originator.getOriginatorInfo());
                originatorAnalysis.put(originatorId, analysis);
            });
            
            stats.put("originatorAnalysis", originatorAnalysis);
            stats.put("timestamp", LocalDateTime.now());
            
            return stats;
        }
    }
    
    /**
     * Create caretaker for managing mementos using HashMap operations
     * 
     * @return created caretaker
     */
    public DataProcessingCaretaker createCaretaker() {
        DataProcessingCaretaker caretaker = new DataProcessingCaretaker();
        
        // Register caretaker using HashMap operations
        String caretakerId = UUID.randomUUID().toString();
        mementoRegistry.put(caretakerId, caretaker);
        updateMementoMetrics(caretakerId, "CARETAKER_CREATED");
        
        return caretaker;
    }
    
    /**
     * Process data with memento support using HashMap operations
     * 
     * @param originatorId originator identifier
     * @param data input data
     * @param operations list of operations to perform
     * @return processing results
     */
    public Map<String, Object> processDataWithMemento(String originatorId, Map<String, Object> data, List<String> operations) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Get or create caretaker using HashMap operations
            DataProcessingCaretaker caretaker = getOrCreateCaretaker();
            
            // Get or create originator using HashMap operations
            DataProcessingOriginator originator = caretaker.getOriginator(originatorId);
            if (originator == null) {
                originator = caretaker.createOriginator(originatorId);
            }
            
            // Set initial state using HashMap operations
            originator.setCurrentState(data);
            
            Map<String, Object> results = new HashMap<>();
            List<Map<String, Object>> operationResults = new ArrayList<>();
            
            // Process operations using HashMap operations
            for (String operation : operations) {
                // Save memento before operation
                DataProcessingMementoInterface memento = caretaker.saveMemento(originatorId);
                
                // Perform operation using HashMap operations
                Map<String, Object> operationResult = performOperation(originator, operation);
                operationResults.add(operationResult);
                
                // Add memento info to result
                operationResult.put("mementoId", memento.getMementoId());
                operationResult.put("mementoCreatedAt", memento.getCreatedAt());
            }
            
            results.put("originatorId", originatorId);
            results.put("operationResults", operationResults);
            results.put("finalState", originator.getCurrentState());
            results.put("mementoHistorySize", originator.getMementoHistory().size());
            results.put("totalProcessingTime", System.currentTimeMillis() - startTime);
            results.put("timestamp", LocalDateTime.now());
            results.put("mementoVersion", MEMENTO_VERSION);
            
            // Update execution metrics using HashMap operations
            updateExecutionMetrics(originatorId, true, System.currentTimeMillis() - startTime);
            
            return results;
            
        } catch (Exception e) {
            updateExecutionMetrics(originatorId, false, System.currentTimeMillis() - startTime);
            return createErrorResult("Memento processing failed: " + e.getMessage());
        }
    }
    
    /**
     * Perform operation on originator using HashMap operations
     * 
     * @param originator originator to operate on
     * @param operation operation to perform
     * @return operation result
     */
    private Map<String, Object> performOperation(DataProcessingOriginator originator, String operation) {
        Map<String, Object> result = new HashMap<>();
        
        // Production-grade: Operation processing with HashMap operations
        switch (operation.toUpperCase()) {
            case "UPPERCASE":
                Map<String, Object> currentState = originator.getCurrentState();
                Map<String, Object> newState = new HashMap<>();
                
                // Method 1: Using entrySet iteration for data transformation
                for (Map.Entry<String, Object> entry : currentState.entrySet()) {
                    if (entry.getValue() instanceof String) {
                        newState.put(entry.getKey(), ((String) entry.getValue()).toUpperCase());
                    } else {
                        newState.put(entry.getKey(), entry.getValue());
                    }
                }
                
                originator.setCurrentState(newState);
                result.put("operation", "UPPERCASE");
                result.put("transformedKeys", newState.size());
                break;
                
            case "ADD_TIMESTAMP":
                originator.addData("operationTimestamp", LocalDateTime.now());
                originator.addData("operationType", "ADD_TIMESTAMP");
                result.put("operation", "ADD_TIMESTAMP");
                result.put("timestampAdded", true);
                break;
                
            case "ANALYZE_DATA":
                Map<String, Object> analysis = new HashMap<>();
                Map<String, Object> state = originator.getCurrentState();
                
                // Using Streams for data analysis
                Map<String, Long> dataTypeCounts = state.values().stream()
                        .collect(Collectors.groupingBy(
                            value -> value.getClass().getSimpleName(),
                            Collectors.counting()
                        ));
                
                analysis.put("dataTypeCounts", dataTypeCounts);
                analysis.put("totalDataItems", state.size());
                analysis.put("analysisTimestamp", LocalDateTime.now());
                
                originator.addData("analysis", analysis);
                result.put("operation", "ANALYZE_DATA");
                result.put("analysisComplete", true);
                break;
                
            default:
                result.put("operation", operation);
                result.put("status", "UNKNOWN_OPERATION");
        }
        
        result.put("timestamp", LocalDateTime.now());
        return result;
    }
    
    /**
     * Get or create caretaker using HashMap operations
     * 
     * @return caretaker instance
     */
    private DataProcessingCaretaker getOrCreateCaretaker() {
        // Production-grade: Caretaker management with HashMap operations
        if (mementoRegistry.isEmpty()) {
            return createCaretaker();
        }
        
        // Return first available caretaker
        return mementoRegistry.values().stream()
                .filter(caretaker -> caretaker instanceof DataProcessingCaretaker)
                .map(caretaker -> (DataProcessingCaretaker) caretaker)
                .findFirst()
                .orElse(createCaretaker());
    }
    
    /**
     * Update memento metrics using HashMap operations
     * 
     * @param mementoId memento identifier
     * @param action action performed
     */
    private void updateMementoMetrics(String mementoId, String action) {
        String metricsKey = "memento_" + mementoId;
        Map<String, Object> metrics = (Map<String, Object>) mementoMetrics.getOrDefault(metricsKey, new HashMap<>());
        
        metrics.put("lastAction", action);
        metrics.put("lastUpdate", LocalDateTime.now());
        metrics.put("actionCount", (Integer) metrics.getOrDefault("actionCount", 0) + 1);
        
        mementoMetrics.put(metricsKey, metrics);
    }
    
    /**
     * Update execution metrics using HashMap operations
     * 
     * @param originatorId originator identifier
     * @param success whether execution was successful
     * @param executionTime execution time in milliseconds
     */
    private void updateExecutionMetrics(String originatorId, boolean success, long executionTime) {
        String metricsKey = "execution_" + originatorId;
        Map<String, Object> metrics = (Map<String, Object>) mementoMetrics.getOrDefault(metricsKey, new HashMap<>());
        
        Integer totalExecutions = (Integer) metrics.getOrDefault("totalExecutions", 0);
        Integer successfulExecutions = (Integer) metrics.getOrDefault("successfulExecutions", 0);
        Long totalExecutionTime = (Long) metrics.getOrDefault("totalExecutionTime", 0L);
        
        metrics.put("totalExecutions", totalExecutions + 1);
        metrics.put("successfulExecutions", successfulExecutions + (success ? 1 : 0));
        metrics.put("totalExecutionTime", totalExecutionTime + executionTime);
        metrics.put("averageExecutionTime", (double) (totalExecutionTime + executionTime) / (totalExecutions + 1));
        metrics.put("lastExecution", LocalDateTime.now());
        
        mementoMetrics.put(metricsKey, metrics);
    }
    
    /**
     * Get memento statistics using HashMap operations
     * 
     * @return memento statistics
     */
    public Map<String, Object> getMementoStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Production-grade: Statistics using HashMap
        stats.put("mementoVersion", MEMENTO_VERSION);
        stats.put("totalCaretakers", mementoRegistry.size());
        stats.put("mementoMetrics", new HashMap<>(mementoMetrics));
        stats.put("timestamp", LocalDateTime.now());
        
        // Using Streams for memento analysis
        Map<String, Object> mementoAnalysis = new HashMap<>();
        mementoRegistry.forEach((mementoId, caretaker) -> {
            if (caretaker instanceof DataProcessingCaretaker) {
                DataProcessingCaretaker c = (DataProcessingCaretaker) caretaker;
                Map<String, Object> analysis = new HashMap<>();
                analysis.put("totalOriginators", c.getAllOriginators().size());
                analysis.put("caretakerStats", c.getCaretakerStatistics());
                mementoAnalysis.put(mementoId, analysis);
            }
        });
        
        stats.put("mementoAnalysis", mementoAnalysis);
        
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
        errorResult.put("mementoVersion", MEMENTO_VERSION);
        return errorResult;
    }
    
    /**
     * Clear memento registry
     */
    public void clearMementos() {
        mementoRegistry.clear();
        mementoMetrics.clear();
    }
}
