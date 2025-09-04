package com.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Health Check Controller for Netflix-style Backend Service
 * 
 * Demonstrates Netflix SDE-2 production-grade patterns:
 * - HashMap implementations and iterations
 * - Optional usage patterns
 * - Stream operations
 * - Collection framework best practices
 * - Thread-safe collections for production
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    // Production-grade: Thread-safe collections for concurrent access
    private final Map<String, String> componentStatusCache = new ConcurrentHashMap<>();
    private final Map<String, Object> systemMetricsCache = new ConcurrentHashMap<>();

    public HealthController() {
        // Initialize component status cache
        initializeComponentCache();
    }

    /**
     * Basic health check endpoint with HashMap iteration
     * 
     * @return Health status with timestamp
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        // Production-grade: Using HashMap with proper iteration
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "backend-service");
        health.put("version", "1.0.0");
        
        // Demonstrate HashMap iteration patterns
        health.put("componentCount", componentStatusCache.size());
        
        return ResponseEntity.ok(health);
    }

    /**
     * Detailed health check with advanced collection operations
     * 
     * @return Detailed health information
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "backend-service");
        health.put("version", "1.0.0");
        
        // Production-grade: Using Optional for safe operations
        health.put("components", getComponentHealthWithOptional());
        health.put("memory", getMemoryInfoWithStreams());
        health.put("threads", getThreadInfoWithCollections());
        health.put("systemMetrics", getSystemMetricsWithStreams());
        
        return ResponseEntity.ok(health);
    }

    /**
     * Get component health with Optional patterns and HashMap iteration
     * 
     * @return Component health status using Optional
     */
    private Map<String, String> getComponentHealthWithOptional() {
        // Production-grade: Using Optional for null safety
        return componentStatusCache.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> Optional.ofNullable(entry.getValue())
                            .orElse("UNKNOWN")
                ));
    }

    /**
     * Get memory information using Streams and Optional
     * 
     * @return Memory usage information with Stream operations
     */
    private Map<String, Object> getMemoryInfoWithStreams() {
        Runtime runtime = Runtime.getRuntime();
        
        // Production-grade: Using Streams for data transformation
        return Arrays.asList("total", "free", "used", "max")
                .stream()
                .collect(Collectors.toMap(
                    metric -> metric,
                    metric -> {
                        switch (metric) {
                            case "total":
                                return runtime.totalMemory();
                            case "free":
                                return runtime.freeMemory();
                            case "used":
                                return runtime.totalMemory() - runtime.freeMemory();
                            case "max":
                                return runtime.maxMemory();
                            default:
                                return 0L;
                        }
                    }
                ));
    }

    /**
     * Get thread information using advanced collection operations
     * 
     * @return Thread usage information with collection framework usage
     */
    private Map<String, Object> getThreadInfoWithCollections() {
        ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
        while (rootGroup.getParent() != null) {
            rootGroup = rootGroup.getParent();
        }
        
        // Production-grade: Using HashMap with proper iteration
        Map<String, Object> threads = new HashMap<>();
        
        // Demonstrate HashMap iteration patterns
        threads.put("active", rootGroup.activeCount());
        threads.put("daemon", rootGroup.activeCount());
        
        // Production-grade: Using Optional for safe operations
        Optional.ofNullable(rootGroup.getName())
                .ifPresent(name -> threads.put("groupName", name));
        
        return threads;
    }

    /**
     * Get system metrics using Streams and advanced collections
     * 
     * @return System metrics with Stream operations
     */
    private Map<String, Object> getSystemMetricsWithStreams() {
        // Production-grade: Using Streams for data aggregation
        return Arrays.asList("cpu", "memory", "disk", "network")
                .stream()
                .collect(Collectors.toMap(
                    metric -> metric,
                    metric -> getMetricValue(metric)
                ));
    }

    /**
     * Get metric value with Optional pattern
     * 
     * @param metricName the name of the metric
     * @return metric value or default
     */
    private Object getMetricValue(String metricName) {
        // Production-grade: Using Optional for safe operations
        return Optional.ofNullable(systemMetricsCache.get(metricName))
                .orElseGet(() -> {
                    // Default values for metrics
                    switch (metricName) {
                        case "cpu":
                            return Runtime.getRuntime().availableProcessors();
                        case "memory":
                            return Runtime.getRuntime().totalMemory();
                        case "disk":
                            return "N/A"; // Would integrate with actual disk monitoring
                        case "network":
                            return "N/A"; // Would integrate with actual network monitoring
                        default:
                            return "UNKNOWN";
                    }
                });
    }

    /**
     * Initialize component cache with HashMap operations
     */
    private void initializeComponentCache() {
        // Production-grade: HashMap initialization and iteration
        Map<String, String> initialComponents = new HashMap<>();
        initialComponents.put("database", "UP");
        initialComponents.put("security", "UP");
        initialComponents.put("actuator", "UP");
        initialComponents.put("cache", "UP");
        initialComponents.put("messaging", "UP");
        
        // Demonstrate HashMap iteration and bulk operations
        initialComponents.forEach((key, value) -> 
            componentStatusCache.put(key, value));
        
        // Production-grade: Using Streams for bulk operations
        initialComponents.entrySet().stream()
                .filter(entry -> "UP".equals(entry.getValue()))
                .forEach(entry -> 
                    systemMetricsCache.put(entry.getKey() + "_status", entry.getValue()));
    }

    /**
     * Get component status with HashMap iteration
     * 
     * @return Component status using HashMap iteration
     */
    @GetMapping("/components")
    public ResponseEntity<Map<String, String>> getComponentStatus() {
        // Production-grade: HashMap iteration patterns
        Map<String, String> status = new HashMap<>();
        
        // Method 1: Using entrySet() iteration
        for (Map.Entry<String, String> entry : componentStatusCache.entrySet()) {
            status.put(entry.getKey(), entry.getValue());
        }
        
        // Method 2: Using forEach with lambda
        componentStatusCache.forEach((key, value) -> 
            status.put(key + "_cached", value));
        
        return ResponseEntity.ok(status);
    }

    /**
     * Get system statistics using Streams and collections
     * 
     * @return System statistics with advanced collection operations
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        // Production-grade: Using Streams for data aggregation
        Map<String, Object> stats = new HashMap<>();
        
        // Stream operations on collections
        long upComponents = componentStatusCache.values().stream()
                .filter("UP"::equals)
                .count();
        
        long totalComponents = componentStatusCache.size();
        
        // Optional usage for safe operations
        double healthPercentage = Optional.of(totalComponents)
                .filter(total -> total > 0)
                .map(total -> (double) upComponents / total * 100)
                .orElse(0.0);
        
        stats.put("upComponents", upComponents);
        stats.put("totalComponents", totalComponents);
        stats.put("healthPercentage", Math.round(healthPercentage * 100.0) / 100.0);
        stats.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(stats);
    }
}
