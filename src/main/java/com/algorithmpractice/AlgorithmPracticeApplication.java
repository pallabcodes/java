package com.algorithmpractice;

import com.algorithmpractice.algorithms.sorting.QuickSort;
import com.algorithmpractice.algorithms.searching.BinarySearch;
import com.algorithmpractice.datastructures.array.DynamicArray;
import com.algorithmpractice.utils.ArrayUtils;
import com.algorithmpractice.config.AlgorithmConfig;
import com.algorithmpractice.monitoring.PerformanceMonitor;
import com.algorithmpractice.monitoring.OperationMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Main application class demonstrating Netflix Engineering Standards.
 * 
 * <p>This application showcases enterprise-grade code that prioritizes:</p>
 * <ul>
 *   <li><strong>Code Readability</strong>: Clear, self-documenting code structure that any engineer can understand in 5 minutes</li>
 *   <li><strong>5-Minute Debuggability</strong>: Comprehensive logging, performance monitoring, and error context</li>
 *   <li><strong>Production Readiness</strong>: Async operations, performance metrics, and graceful degradation</li>
 *   <li><strong>Maintainability</strong>: Well-organized code without oversimplification</li>
 * </ul>
 * 
 * <p>Key Design Decisions:</p>
 * <ul>
 *   <li>Structured demonstration flow with clear separation of concerns</li>
 *   <li>Performance monitoring and metrics collection</li>
 *   <li>Async execution for non-blocking operations</li>
 *   <li>Comprehensive error handling with context</li>
 *   <li>Graceful degradation and fallback strategies</li>
 * </ul>
 * 
 * @author Netflix Backend Engineering Team
 * @version 2.0.0
 * @since 2024
 */
public final class AlgorithmPracticeApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlgorithmPracticeApplication.class);
    
    // Performance and monitoring constants
    private static final int ASYNC_DEMO_TIMEOUT_MS = 10000;
    private static final int MAX_CONCURRENT_DEMOS = 10;
    private static final String DEMO_SORTING = "SORTING_ALGORITHMS";
    private static final String DEMO_SEARCHING = "SEARCHING_ALGORITHMS";
    private static final String DEMO_DATA_STRUCTURES = "DATA_STRUCTURES";
    
    // Performance monitoring
    private static final PerformanceMonitor performanceMonitor = new PerformanceMonitor();
    private static final OperationMetrics operationMetrics = new OperationMetrics();
    
    // Async execution for non-blocking demonstrations
    private static final ExecutorService asyncExecutor = Executors.newFixedThreadPool(MAX_CONCURRENT_DEMOS);

    private AlgorithmPracticeApplication() {
        // Utility class - prevent instantiation
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Main method - entry point of the application.
     * 
     * <p>This method demonstrates Netflix engineering standards for readability and debuggability:</p>
 * <ul>
 *   <li>Clear operation flow with meaningful variable names</li>
 *   <li>Comprehensive error handling with context</li>
 *   <li>Performance monitoring and metrics</li>
 *   <li>Async operations for better user experience</li>
 *   <li>Graceful degradation and fallback strategies</li>
 * </ul>
 * 
 * @param args command line arguments (not used)
 */
    public static void main(final String[] args) {
        final String sessionId = generateSessionId();
        final long applicationStartTime = System.currentTimeMillis();
        
        try {
            LOGGER.info("🚀 [{}] Starting Algorithm Practice Application v{}", sessionId, AlgorithmConfig.APP_VERSION);
            LOGGER.info("==========================================");
            
            // Initialize performance monitoring
            initializePerformanceMonitoring(sessionId);
            
            // Execute demonstrations with comprehensive monitoring
            executeAlgorithmDemonstrations(sessionId);
            
            // Record application success metrics
            recordApplicationSuccess(sessionId, applicationStartTime);
            
            LOGGER.info("✅ [{}] All demonstrations completed successfully in {}ms!", 
                       sessionId, System.currentTimeMillis() - applicationStartTime);
            
        } catch (final Exception e) {
            // Comprehensive error handling with context for rapid debugging
            final long applicationDuration = System.currentTimeMillis() - applicationStartTime;
            recordApplicationFailure(sessionId, applicationStartTime, applicationDuration, e);
            
            final String errorContext = buildApplicationErrorContext(sessionId, applicationDuration);
            LOGGER.error("❌ [{}] Application failed with error: {}. Context: {}", 
                        sessionId, e.getMessage(), errorContext, e);
            
            System.exit(1);
        } finally {
            // Graceful shutdown
            shutdownApplication(sessionId);
        }
    }

    /**
     * Executes all algorithm demonstrations with comprehensive monitoring.
     * 
     * <p>This method demonstrates how to organize complex operations for readability
 * while maintaining production-grade monitoring and error handling.</p>
 * 
 * @param sessionId the session identifier for tracing
 */
    private static void executeAlgorithmDemonstrations(final String sessionId) {
        LOGGER.info("📊 [{}] Starting Algorithm Demonstrations", sessionId);
        
        // Execute demonstrations with async support for better performance
        final CompletableFuture<Void> sortingDemo = executeSortingDemoAsync(sessionId);
        final CompletableFuture<Void> searchingDemo = executeSearchingDemoAsync(sessionId);
        final CompletableFuture<Void> dataStructuresDemo = executeDataStructuresDemoAsync(sessionId);
        
        // Wait for all demonstrations to complete
        try {
            CompletableFuture.allOf(sortingDemo, searchingDemo, dataStructuresDemo)
                .get(ASYNC_DEMO_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            
            LOGGER.info("✅ [{}] All demonstrations completed successfully", sessionId);
            
        } catch (final Exception e) {
            final String errorMessage = "One or more demonstrations failed to complete within timeout";
            LOGGER.error("❌ [{}] {}: {}", sessionId, errorMessage, e.getMessage(), e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    /**
     * Executes sorting algorithms demonstration asynchronously.
     * 
     * @param sessionId the session identifier for tracing
     * @return CompletableFuture representing the async operation
     */
    private static CompletableFuture<Void> executeSortingDemoAsync(final String sessionId) {
        return CompletableFuture.runAsync(() -> {
            final String demoId = DEMO_SORTING;
            final long demoStartTime = System.currentTimeMillis();
            
            try {
                LOGGER.info("📊 [{}] {} Demo Starting:", sessionId, demoId);
                LOGGER.info("---------------------------");
                
                // Execute sorting demonstration with monitoring
                final SortingDemoResult result = executeSortingAlgorithms(sessionId, demoId);
                
                // Record success metrics
                recordDemoSuccess(sessionId, demoId, demoStartTime, result);
                
                LOGGER.info("✅ [{}] {} Demo completed successfully in {}ms", 
                           sessionId, demoId, System.currentTimeMillis() - demoStartTime);
                
            } catch (final Exception e) {
                final long demoDuration = System.currentTimeMillis() - demoStartTime;
                recordDemoFailure(sessionId, demoId, demoStartTime, demoDuration, e);
                
                final String errorContext = buildDemoErrorContext(sessionId, demoId, demoDuration);
                LOGGER.error("❌ [{}] {} Demo failed: {}. Context: {}", 
                            sessionId, demoId, e.getMessage(), errorContext, e);
                
                throw new RuntimeException("Sorting demo failed", e);
            }
        }, asyncExecutor);
    }

    /**
     * Executes searching algorithms demonstration asynchronously.
     * 
     * @param sessionId the session identifier for tracing
     * @return CompletableFuture representing the async operation
     */
    private static CompletableFuture<Void> executeSearchingDemoAsync(final String sessionId) {
        return CompletableFuture.runAsync(() -> {
            final String demoId = DEMO_SEARCHING;
            final long demoStartTime = System.currentTimeMillis();
            
            try {
                LOGGER.info("🔍 [{}] {} Demo Starting:", sessionId, demoId);
                LOGGER.info("-----------------------------");
                
                // Execute searching demonstration with monitoring
                final SearchingDemoResult result = executeSearchingAlgorithms(sessionId, demoId);
                
                // Record success metrics
                recordDemoSuccess(sessionId, demoId, demoStartTime, result);
                
                LOGGER.info("✅ [{}] {} Demo completed successfully in {}ms", 
                           sessionId, demoId, System.currentTimeMillis() - demoStartTime);
                
            } catch (final Exception e) {
                final long demoDuration = System.currentTimeMillis() - demoStartTime;
                recordDemoFailure(sessionId, demoId, demoStartTime, demoDuration, e);
                
                final String errorContext = buildDemoErrorContext(sessionId, demoId, demoDuration);
                LOGGER.error("❌ [{}] {} Demo failed: {}. Context: {}", 
                            sessionId, demoId, e.getMessage(), errorContext, e);
                
                throw new RuntimeException("Searching demo failed", e);
            }
        }, asyncExecutor);
    }

    /**
     * Executes data structures demonstration asynchronously.
     * 
     * @param sessionId the session identifier for tracing
     * @return CompletableFuture representing the async operation
     */
    private static CompletableFuture<Void> executeDataStructuresDemoAsync(final String sessionId) {
        return CompletableFuture.runAsync(() -> {
            final String demoId = DEMO_DATA_STRUCTURES;
            final long demoStartTime = System.currentTimeMillis();
            
            try {
                LOGGER.info("🏗️  [{}] {} Demo Starting:", sessionId, demoId);
                LOGGER.info("-------------------------");
                
                // Execute data structures demonstration with monitoring
                final DataStructuresDemoResult result = executeDataStructures(sessionId, demoId);
                
                // Record success metrics
                recordDemoSuccess(sessionId, demoId, demoStartTime, result);
                
                LOGGER.info("✅ [{}] {} Demo completed successfully in {}ms", 
                           sessionId, demoId, System.currentTimeMillis() - demoStartTime);
                
            } catch (final Exception e) {
                final long demoDuration = System.currentTimeMillis() - demoStartTime;
                recordDemoFailure(sessionId, demoId, demoStartTime, demoDuration, e);
                
                final String errorContext = buildDemoErrorContext(sessionId, demoId, demoDuration);
                LOGGER.error("❌ [{}] {} Demo failed: {}. Context: {}", 
                            sessionId, demoId, e.getMessage(), errorContext, e);
                
                throw new RuntimeException("Data structures demo failed", e);
            }
        }, asyncExecutor);
    }

    // ========== CORE ALGORITHM IMPLEMENTATIONS ==========

    /**
     * Executes sorting algorithms with comprehensive monitoring and validation.
     * 
     * @param sessionId the session identifier for tracing
     * @param demoId the demonstration identifier
     * @return SortingDemoResult containing performance metrics and validation results
     */
    private static SortingDemoResult executeSortingAlgorithms(final String sessionId, final String demoId) {
        final long operationStartTime = System.currentTimeMillis();
        
        // Prepare test data
        final int[] unsortedArray = AlgorithmConfig.DEMO_UNSORTED_ARRAY.clone();
        LOGGER.info("[{}] {} - Original array: {}", sessionId, demoId, Arrays.toString(unsortedArray));
        
        // Execute QuickSort with performance monitoring
        final long quickSortStartTime = System.currentTimeMillis();
        final int[] quickSorted = unsortedArray.clone();
        QuickSort.sort(quickSorted);
        final long quickSortDuration = System.currentTimeMillis() - quickSortStartTime;
        
        // Validate sorting results
        final boolean isSorted = ArrayUtils.isSorted(quickSorted);
        final int arraySize = quickSorted.length;
        
        // Record performance metrics
        performanceMonitor.recordSortingPerformance(arraySize, quickSortDuration);
        
        LOGGER.info("[{}] {} - After QuickSort: {}", sessionId, demoId, Arrays.toString(quickSorted));
        LOGGER.info("[{}] {} - Array is sorted: {} (Size: {}, Duration: {}ms)", 
                   sessionId, demoId, isSorted, arraySize, quickSortDuration);
        
        if (!isSorted) {
            LOGGER.warn("⚠️  [{}] {} - Sorting verification failed!", sessionId, demoId);
        }
        
        return new SortingDemoResult(arraySize, quickSortDuration, isSorted);
    }

    /**
     * Executes searching algorithms with comprehensive monitoring and validation.
     * 
     * @param sessionId the session identifier for tracing
     * @param demoId the demonstration identifier
     * @return SearchingDemoResult containing performance metrics and search results
     */
    private static SearchingDemoResult executeSearchingAlgorithms(final String sessionId, final String demoId) {
        final long operationStartTime = System.currentTimeMillis();
        
        // Prepare test data
        final int[] sortedArray = AlgorithmConfig.DEMO_SORTED_ARRAY.clone();
        final int target = AlgorithmConfig.DEMO_TARGET_VALUE;
        
        LOGGER.info("[{}] {} - Searching for {} in: {}", sessionId, demoId, target, Arrays.toString(sortedArray));
        
        // Execute Binary Search with performance monitoring
        final long binarySearchStartTime = System.currentTimeMillis();
        final int binarySearchResult = BinarySearch.search(sortedArray, target);
        final long binarySearchDuration = System.currentTimeMillis() - binarySearchStartTime;
        
        // Record performance metrics
        performanceMonitor.recordSearchingPerformance(sortedArray.length, binarySearchDuration);
        
        // Log results
        if (binarySearchResult != BinarySearch.NOT_FOUND) {
            LOGGER.info("[{}] {} - Binary Search: Found {} at index {} (Duration: {}ms)", 
                       sessionId, demoId, target, binarySearchResult, binarySearchDuration);
        } else {
            LOGGER.info("[{}] {} - Binary Search: {} not found (Duration: {}ms)", 
                       sessionId, demoId, target, binarySearchDuration);
        }
        
        return new SearchingDemoResult(sortedArray.length, binarySearchDuration, binarySearchResult != BinarySearch.NOT_FOUND);
    }

    /**
     * Executes data structures demonstration with comprehensive monitoring.
     * 
     * @param sessionId the session identifier for tracing
     * @param demoId the demonstration identifier
     * @return DataStructuresDemoResult containing performance metrics and operation results
     */
    private static DataStructuresDemoResult executeDataStructures(final String sessionId, final String demoId) {
        final long operationStartTime = System.currentTimeMillis();
        
        try {
            // Create and populate dynamic array
            final DynamicArray<Integer> dynamicArray = new DynamicArray<>(AlgorithmConfig.DYNAMIC_ARRAY_DEFAULT_CAPACITY);
            
            // Add elements with performance monitoring
            final long addStartTime = System.currentTimeMillis();
            dynamicArray.add(10);
            dynamicArray.add(20);
            dynamicArray.add(30);
            final long addDuration = System.currentTimeMillis() - addStartTime;
            
            // Retrieve elements with performance monitoring
            final long getStartTime = System.currentTimeMillis();
            final Integer elementAtIndex1 = dynamicArray.get(1);
            final long getDuration = System.currentTimeMillis() - getStartTime;
            
            // Record performance metrics
            performanceMonitor.recordDataStructurePerformance(dynamicArray.size(), addDuration, getDuration);
            
            LOGGER.info("[{}] {} - Dynamic Array size: {}", sessionId, demoId, dynamicArray.size());
            LOGGER.info("[{}] {} - Dynamic Array elements: {}", sessionId, demoId, dynamicArray);
            LOGGER.info("[{}] {} - Element at index 1: {} (Add: {}ms, Get: {}ms)", 
                       sessionId, demoId, elementAtIndex1, addDuration, getDuration);
            
            return new DataStructuresDemoResult(dynamicArray.size(), addDuration, getDuration);
            
        } catch (final Exception e) {
            final String errorMessage = "Data structures demonstration failed";
            LOGGER.error("[{}] {} - {}: {}", sessionId, demoId, errorMessage, e.getMessage(), e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    // ========== PERFORMANCE MONITORING AND METRICS ==========

    /**
     * Initializes performance monitoring for the application session.
     * 
     * @param sessionId the session identifier for tracing
     */
    private static void initializePerformanceMonitoring(final String sessionId) {
        LOGGER.info("[{}] Initializing performance monitoring", sessionId);
        performanceMonitor.initializeSession(sessionId);
        operationMetrics.initializeSession(sessionId);
        LOGGER.info("[{}] Performance monitoring initialized successfully", sessionId);
    }

    /**
     * Records successful demonstration completion.
     * 
     * @param sessionId the session identifier
     * @param demoId the demonstration identifier
     * @param startTime the start time of the demonstration
     * @param result the demonstration result
     */
    private static void recordDemoSuccess(final String sessionId, final String demoId, 
                                        final long startTime, final Object result) {
        final long duration = System.currentTimeMillis() - startTime;
        operationMetrics.recordDemoSuccess(sessionId, demoId, duration);
        LOGGER.debug("[{}] Demo {} completed successfully in {}ms", sessionId, demoId, duration);
    }

    /**
     * Records failed demonstration completion.
     * 
     * @param sessionId the session identifier
     * @param demoId the demonstration identifier
     * @param startTime the start time of the demonstration
     * @param duration the duration until failure
     * @param error the error that occurred
     */
    private static void recordDemoFailure(final String sessionId, final String demoId, 
                                        final long startTime, final long duration, final Exception error) {
        operationMetrics.recordDemoFailure(sessionId, demoId, duration, error);
        LOGGER.debug("[{}] Demo {} failed after {}ms", sessionId, demoId, duration);
    }

    /**
     * Records successful application completion.
     * 
     * @param sessionId the session identifier
     * @param startTime the application start time
     */
    private static void recordApplicationSuccess(final String sessionId, final long startTime) {
        final long duration = System.currentTimeMillis() - startTime;
        operationMetrics.recordApplicationSuccess(sessionId, duration);
        LOGGER.debug("[{}] Application completed successfully in {}ms", sessionId, duration);
    }

    /**
     * Records failed application completion.
     * 
     * @param sessionId the session identifier
     * @param startTime the application start time
     * @param duration the duration until failure
     * @param error the error that occurred
     */
    private static void recordApplicationFailure(final String sessionId, final long startTime, 
                                               final long duration, final Exception error) {
        operationMetrics.recordApplicationFailure(sessionId, duration, error);
        LOGGER.debug("[{}] Application failed after {}ms", sessionId, duration);
    }

    // ========== UTILITY METHODS FOR DEBUGGABILITY ==========

    /**
     * Generates a unique session identifier for tracing operations.
     * 
     * @return a unique session identifier
     */
    private static String generateSessionId() {
        return java.util.UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Builds comprehensive error context for rapid debugging.
     * 
     * @param sessionId the session identifier
     * @param duration the operation duration
     * @return formatted error context string
     */
    private static String buildApplicationErrorContext(final String sessionId, final long duration) {
        return String.format("Session ID: %s, Duration: %dms, Timestamp: %d, Thread: %s", 
                           sessionId, duration, System.currentTimeMillis(), Thread.currentThread().getName());
    }

    /**
     * Builds comprehensive demo error context for rapid debugging.
     * 
     * @param sessionId the session identifier
     * @param demoId the demonstration identifier
     * @param duration the operation duration
     * @return formatted error context string
     */
    private static String buildDemoErrorContext(final String sessionId, final String demoId, final long duration) {
        return String.format("Session ID: %s, Demo: %s, Duration: %dms, Timestamp: %d, Thread: %s", 
                           sessionId, demoId, duration, System.currentTimeMillis(), Thread.currentThread().getName());
    }

    /**
     * Shuts down the application gracefully.
     * 
     * @param sessionId the session identifier for tracing
     */
    private static void shutdownApplication(final String sessionId) {
        LOGGER.info("[{}] Shutting down Algorithm Practice Application...", sessionId);
        
        // Shutdown async executor
        asyncExecutor.shutdown();
        try {
            if (!asyncExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                asyncExecutor.shutdownNow();
            }
        } catch (final InterruptedException e) {
            asyncExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        // Finalize performance monitoring
        performanceMonitor.finalizeSession(sessionId);
        operationMetrics.finalizeSession(sessionId);
        
        LOGGER.info("[{}] Application shutdown completed", sessionId);
    }

    // ========== INNER CLASSES FOR ORGANIZATION ==========

    /**
     * Result container for sorting algorithm demonstrations.
     * This demonstrates how to organize related data without oversimplification.
     */
    private static final class SortingDemoResult {
        private final int arraySize;
        private final long sortingDuration;
        private final boolean isSorted;

        public SortingDemoResult(final int arraySize, final long sortingDuration, final boolean isSorted) {
            this.arraySize = arraySize;
            this.sortingDuration = sortingDuration;
            this.isSorted = isSorted;
        }

        // Getters for metrics collection
        public int getArraySize() { return arraySize; }
        public long getSortingDuration() { return sortingDuration; }
        public boolean isSorted() { return isSorted; }
    }

    /**
     * Result container for searching algorithm demonstrations.
     */
    private static final class SearchingDemoResult {
        private final int arraySize;
        private final long searchDuration;
        private final boolean targetFound;

        public SearchingDemoResult(final int arraySize, final long searchDuration, final boolean targetFound) {
            this.arraySize = arraySize;
            this.searchDuration = searchDuration;
            this.targetFound = targetFound;
        }

        // Getters for metrics collection
        public int getArraySize() { return arraySize; }
        public long getSearchDuration() { return searchDuration; }
        public boolean isTargetFound() { return targetFound; }
    }

    /**
     * Result container for data structures demonstrations.
     */
    private static final class DataStructuresDemoResult {
        private final int arraySize;
        private final long addDuration;
        private final long getDuration;

        public DataStructuresDemoResult(final int arraySize, final long addDuration, final long getDuration) {
            this.arraySize = arraySize;
            this.addDuration = addDuration;
            this.getDuration = getDuration;
        }

        // Getters for metrics collection
        public int getArraySize() { return arraySize; }
        public long getAddDuration() { return addDuration; }
        public long getGetDuration() { return getDuration; }
    }
}
