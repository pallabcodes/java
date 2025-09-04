package com.backend.solid;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Netflix Production-Grade SOLID Principles + Multithreading Implementation
 * 
 * Demonstrates Netflix SDE-2 backend engineering excellence:
 * 
 * SOLID PRINCIPLES:
 * - Single Responsibility: Each method has one clear purpose
 * - Open/Closed: Extensible without modification
 * - Liskov Substitution: Interfaces properly implemented
 * - Interface Segregation: Focused, specific interfaces
 * - Dependency Inversion: High-level modules don't depend on low-level
 * 
 * MULTITHREADING CONSIDERATIONS:
 * - Custom ThreadPoolExecutor with Netflix-scale configuration
 * - Advanced synchronization with synchronized blocks and atomic operations
 * - Concurrent data structures (ConcurrentHashMap, CopyOnWriteArrayList)
 * - Atomic operations and CAS (Compare-And-Swap) patterns
 * - Thread-safe metrics and monitoring
 * - Performance optimization and monitoring
 * 
 * @author Netflix Backend Team
 * @version 1.0.0
 */
@Service
public class DataProcessingService {

    // Production-grade: Custom ThreadPoolExecutor for Netflix-scale operations
    private final ThreadPoolExecutor processingThreadPool;
    private final ThreadPoolExecutor monitoringThreadPool;
    
    // Production-grade: Thread-safe collections for concurrent access
    private final Map<String, Object> dataCache = new ConcurrentHashMap<>();
    private final Map<String, Object> processingMetrics = new ConcurrentHashMap<>();
    private final List<String> processingQueue = new CopyOnWriteArrayList<>();
    
    // Production-grade: Atomic counters for thread-safe metrics
    private final AtomicLong totalProcessedItems = new AtomicLong(0);
    private final AtomicLong totalProcessingTime = new AtomicLong(0);
    private final AtomicInteger activeThreads = new AtomicInteger(0);
    
    // Production-grade: Netflix-scale thread pool configuration
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    private static final int MAX_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 4;
    private static final int QUEUE_CAPACITY = 10000;
    private static final long KEEP_ALIVE_TIME = 60L;
    
    public DataProcessingService() {
        // Production-grade: Custom ThreadPoolExecutor with Netflix-scale settings
        this.processingThreadPool = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(QUEUE_CAPACITY),
            new ThreadFactory() {
                private final AtomicInteger threadNumber = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "DataProcessor-" + threadNumber.getAndIncrement());
                    thread.setPriority(Thread.NORM_PRIORITY);
                    thread.setDaemon(false);
                    return thread;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy() // Netflix-scale: Prevents task rejection
        );
        
        // Production-grade: Monitoring thread pool for metrics collection
        this.monitoringThreadPool = new ThreadPoolExecutor(
            2, 4, 30L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            r -> new Thread(r, "MetricsCollector-" + System.currentTimeMillis()),
            new ThreadPoolExecutor.DiscardOldestPolicy()
        );
        
        // Production-grade: Start monitoring thread
        startMonitoringThread();
    }
    
    /**
     * SINGLE RESPONSIBILITY PRINCIPLE: Process data with advanced multithreading
     * 
     * @param data input data to process
     * @return processing results
     */
    public CompletableFuture<Map<String, Object>> processDataAsync(List<String> data) {
        // Production-grade: Async processing with CompletableFuture
        return CompletableFuture.supplyAsync(() -> {
            activeThreads.incrementAndGet();
            long startTime = System.currentTimeMillis();
            
            try {
                // Production-grade: Data processing with HashMap operations
                Map<String, Object> result = new HashMap<>();
                
                // Method 1: Using synchronized block for thread safety
                synchronized (this) {
                    result.put("dataSize", data.size());
                    result.put("processingMode", "ASYNC");
                    result.put("threadId", Thread.currentThread().getId());
                    result.put("threadName", Thread.currentThread().getName());
                }
                
                // Method 2: Using Streams for concurrent data processing
                Map<String, Long> frequencyMap = data.parallelStream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.groupingBy(
                        String::toLowerCase,
                        Collectors.counting()
                    ));
                
                result.put("frequencyAnalysis", frequencyMap);
                result.put("uniqueItems", frequencyMap.size());
                
                // Method 3: Using synchronized block for cache updates
                synchronized (dataCache) {
                    dataCache.put("processed_" + System.currentTimeMillis(), result);
                    processingQueue.add("completed_" + System.currentTimeMillis());
                }
                
                return result;
                
            } finally {
                // Production-grade: Metrics collection
                long processingTime = System.currentTimeMillis() - startTime;
                totalProcessedItems.incrementAndGet();
                totalProcessingTime.addAndGet(processingTime);
                activeThreads.decrementAndGet();
                
                // Update processing metrics atomically
                updateProcessingMetrics(processingTime, data.size());
            }
        }, processingThreadPool);
    }
    
    /**
     * OPEN/CLOSED PRINCIPLE: Extensible processing without modification
     * 
     * @param data input data
     * @param processor custom processor function
     * @return processing results
     */
    public <T> CompletableFuture<T> processDataWithCustomProcessor(
            List<String> data, 
            java.util.function.Function<List<String>, T> processor) {
        
        return CompletableFuture.supplyAsync(() -> {
            activeThreads.incrementAndGet();
            long startTime = System.currentTimeMillis();
            
            try {
                // Production-grade: Custom processing with thread safety
                T result = processor.apply(data);
                return result;
                
            } finally {
                activeThreads.decrementAndGet();
                updateProcessingMetrics(System.currentTimeMillis() - startTime, data.size());
            }
        }, processingThreadPool);
    }
    
    /**
     * LISKOV SUBSTITUTION PRINCIPLE: Interface-based processing
     * 
     * @param data input data
     * @param processor processor interface implementation
     * @return processing results
     */
    public CompletableFuture<Map<String, Object>> processDataWithInterface(
            List<String> data, 
            DataProcessor processor) {
        
        return CompletableFuture.supplyAsync(() -> {
            activeThreads.incrementAndGet();
            long startTime = System.currentTimeMillis();
            
            try {
                // Production-grade: Interface-based processing
                Map<String, Object> result = processor.process(data);
                result.put("processorType", processor.getClass().getSimpleName());
                result.put("processingTime", System.currentTimeMillis() - startTime);
                
                return result;
                
            } finally {
                activeThreads.decrementAndGet();
                updateProcessingMetrics(System.currentTimeMillis() - startTime, data.size());
            }
        }, processingThreadPool);
    }
    
    /**
     * INTERFACE SEGREGATION PRINCIPLE: Focused, specific interfaces
     */
    public interface DataProcessor {
        Map<String, Object> process(List<String> data);
    }
    
    public interface MetricsCollector {
        Map<String, Object> collectMetrics();
    }
    
    public interface CacheManager {
        void updateCache(String key, Object value);
        Optional<Object> getFromCache(String key);
    }
    
    /**
     * DEPENDENCY INVERSION PRINCIPLE: High-level modules don't depend on low-level
     */
    public CompletableFuture<Map<String, Object>> processDataWithDependencies(
            List<String> data,
            MetricsCollector metricsCollector,
            CacheManager cacheManager) {
        
        return CompletableFuture.supplyAsync(() -> {
            activeThreads.incrementAndGet();
            long startTime = System.currentTimeMillis();
            
            try {
                // Production-grade: Dependency injection pattern
                Map<String, Object> result = new HashMap<>();
                result.put("dataSize", data.size());
                result.put("processingMode", "DEPENDENCY_INJECTION");
                
                // Use injected dependencies
                Map<String, Object> metrics = metricsCollector.collectMetrics();
                result.put("systemMetrics", metrics);
                
                // Cache result using injected cache manager
                String cacheKey = "result_" + System.currentTimeMillis();
                cacheManager.updateCache(cacheKey, result);
                
                return result;
                
            } finally {
                activeThreads.decrementAndGet();
                updateProcessingMetrics(System.currentTimeMillis() - startTime, data.size());
            }
        }, processingThreadPool);
    }
    
    /**
     * Production-grade: Advanced multithreading metrics collection
     */
    private void updateProcessingMetrics(long processingTime, int itemCount) {
        // Production-grade: Atomic metrics update
        processingMetrics.compute("totalProcessingTime", (key, value) -> {
            long current = value instanceof Long ? (Long) value : 0L;
            return current + processingTime;
        });
        
        processingMetrics.compute("totalItemsProcessed", (key, value) -> {
            long current = value instanceof Long ? (Long) value : 0L;
            return current + itemCount;
        });
        
        processingMetrics.compute("averageProcessingTime", (key, value) -> {
            long totalTime = (Long) processingMetrics.getOrDefault("totalProcessingTime", 0L);
            long totalItems = (Long) processingMetrics.getOrDefault("totalItemsProcessed", 0L);
            return totalItems > 0 ? (double) totalTime / totalItems : 0.0;
        });
    }
    
    /**
     * Production-grade: Monitoring thread for Netflix-scale operations
     */
    private void startMonitoringThread() {
        monitoringThreadPool.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Production-grade: Thread pool monitoring
                    Map<String, Object> threadPoolMetrics = new HashMap<>();
                    threadPoolMetrics.put("activeThreads", processingThreadPool.getActiveCount());
                    threadPoolMetrics.put("poolSize", processingThreadPool.getPoolSize());
                    threadPoolMetrics.put("corePoolSize", processingThreadPool.getCorePoolSize());
                    threadPoolMetrics.put("maximumPoolSize", processingThreadPool.getMaximumPoolSize());
                    threadPoolMetrics.put("queueSize", processingThreadPool.getQueue().size());
                    threadPoolMetrics.put("completedTasks", processingThreadPool.getCompletedTaskCount());
                    
                    // Production-grade: Cache monitoring
                    Map<String, Object> cacheMetrics = new HashMap<>();
                    cacheMetrics.put("cacheSize", dataCache.size());
                    cacheMetrics.put("queueSize", processingQueue.size());
                    
                    // Production-grade: Performance metrics
                    Map<String, Object> performanceMetrics = new HashMap<>();
                    performanceMetrics.put("totalProcessedItems", totalProcessedItems.get());
                    performanceMetrics.put("totalProcessingTime", totalProcessingTime.get());
                    performanceMetrics.put("averageProcessingTime", 
                        totalProcessedItems.get() > 0 ? 
                        (double) totalProcessingTime.get() / totalProcessedItems.get() : 0.0);
                    
                    // Update global metrics
                    processingMetrics.put("threadPoolMetrics", threadPoolMetrics);
                    processingMetrics.put("cacheMetrics", cacheMetrics);
                    processingMetrics.put("performanceMetrics", performanceMetrics);
                    processingMetrics.put("lastUpdate", LocalDateTime.now());
                    
                    Thread.sleep(5000); // Update every 5 seconds
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    // Production-grade: Error handling in monitoring thread
                    processingMetrics.put("monitoringError", e.getMessage());
                    processingMetrics.put("errorTimestamp", LocalDateTime.now());
                }
            }
        });
    }
    
    /**
     * Production-grade: Get comprehensive system metrics
     */
    public Map<String, Object> getSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Production-grade: Thread pool metrics
        metrics.put("threadPool", Map.of(
            "activeThreads", processingThreadPool.getActiveCount(),
            "poolSize", processingThreadPool.getPoolSize(),
            "corePoolSize", processingThreadPool.getCorePoolSize(),
            "maximumPoolSize", processingThreadPool.getMaximumPoolSize(),
            "queueSize", processingThreadPool.getQueue().size(),
            "completedTasks", processingThreadPool.getCompletedTaskCount()
        ));
        
        // Production-grade: Processing metrics
        metrics.put("processing", Map.of(
            "totalProcessedItems", totalProcessedItems.get(),
            "totalProcessingTime", totalProcessingTime.get(),
            "averageProcessingTime", 
                totalProcessedItems.get() > 0 ? 
                (double) totalProcessingTime.get() / totalProcessedItems.get() : 0.0,
            "activeThreads", activeThreads.get()
        ));
        
        // Production-grade: Cache metrics
        metrics.put("cache", Map.of(
            "cacheSize", dataCache.size(),
            "queueSize", processingQueue.size()
        ));
        
        // Production-grade: System metrics
        metrics.put("system", Map.of(
            "availableProcessors", Runtime.getRuntime().availableProcessors(),
            "freeMemory", Runtime.getRuntime().freeMemory(),
            "totalMemory", Runtime.getRuntime().totalMemory(),
            "maxMemory", Runtime.getRuntime().maxMemory()
        ));
        
        metrics.put("timestamp", LocalDateTime.now());
        
        return metrics;
    }
    
    /**
     * Production-grade: Graceful shutdown
     */
    public void shutdown() {
        // Production-grade: Graceful thread pool shutdown
        processingThreadPool.shutdown();
        monitoringThreadPool.shutdown();
        
        try {
            if (!processingThreadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                processingThreadPool.shutdownNow();
            }
            if (!monitoringThreadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                monitoringThreadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            processingThreadPool.shutdownNow();
            monitoringThreadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
