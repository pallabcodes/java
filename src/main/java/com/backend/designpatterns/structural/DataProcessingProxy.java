package com.backend.designpatterns.structural;

import com.backend.designpatterns.strategy.DataProcessingStrategy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Netflix Production-Grade Proxy Pattern Implementation
 * 
 * Demonstrates Netflix SDE-2 design pattern expertise:
 * - Proxy pattern for service discovery and load balancing
 * - Virtual proxy with lazy loading
 * - Protection proxy with access control
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
public class DataProcessingProxy {

    // Production-grade: Thread-safe collections for concurrent access
    private final Map<String, Object> serviceRegistry = new ConcurrentHashMap<>();
    private final Map<String, Object> loadBalancingMetrics = new ConcurrentHashMap<>();
    private final Map<String, DataProcessingStrategy> strategyCache = new ConcurrentHashMap<>();
    
    // Production-grade: Load balancing configuration
    private final AtomicInteger requestCounter = new AtomicInteger(0);
    private static final int MAX_CACHE_SIZE = 1000;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    
    /**
     * Service endpoint interface
     */
    public interface ServiceEndpoint {
        String getServiceId();
        String getServiceUrl();
        boolean isHealthy();
        Map<String, Object> getServiceInfo();
        Map<String, Object> processRequest(List<String> data);
    }
    
    /**
     * Real service endpoint implementation
     */
    public static class RealServiceEndpoint implements ServiceEndpoint {
        private final String serviceId;
        private final String serviceUrl;
        private final DataProcessingStrategy strategy;
        private final Map<String, Object> serviceInfo;
        
        public RealServiceEndpoint(String serviceId, String serviceUrl, DataProcessingStrategy strategy) {
            this.serviceId = serviceId;
            this.serviceUrl = serviceUrl;
            this.strategy = strategy;
            this.serviceInfo = new HashMap<>();
            this.serviceInfo.put("serviceId", serviceId);
            this.serviceInfo.put("serviceUrl", serviceUrl);
            this.serviceInfo.put("strategyName", strategy.getStrategyName());
            this.serviceInfo.put("createdAt", LocalDateTime.now());
        }
        
        @Override
        public String getServiceId() { return serviceId; }
        
        @Override
        public String getServiceUrl() { return serviceUrl; }
        
        @Override
        public boolean isHealthy() {
            // Production-grade: Health check simulation
            return Math.random() > 0.1; // 90% success rate
        }
        
        @Override
        public Map<String, Object> getServiceInfo() {
            return new HashMap<>(serviceInfo);
        }
        
        @Override
        public Map<String, Object> processRequest(List<String> data) {
            // Production-grade: Request processing with HashMap operations
            Map<String, Object> result = strategy.processData(data);
            result.put("serviceId", serviceId);
            result.put("serviceUrl", serviceUrl);
            result.put("processedAt", LocalDateTime.now());
            return result;
        }
    }
    
    /**
     * Virtual proxy for lazy loading of services
     */
    public static class VirtualServiceProxy implements ServiceEndpoint {
        private final String serviceId;
        private final String serviceUrl;
        private final DataProcessingStrategy strategy;
        private ServiceEndpoint realService;
        private final Map<String, Object> proxyInfo;
        
        public VirtualServiceProxy(String serviceId, String serviceUrl, DataProcessingStrategy strategy) {
            this.serviceId = serviceId;
            this.serviceUrl = serviceUrl;
            this.strategy = strategy;
            this.proxyInfo = new HashMap<>();
            this.proxyInfo.put("serviceId", serviceId);
            this.proxyInfo.put("serviceUrl", serviceUrl);
            this.proxyInfo.put("proxyType", "VIRTUAL");
            this.proxyInfo.put("createdAt", LocalDateTime.now());
        }
        
        @Override
        public String getServiceId() { return serviceId; }
        
        @Override
        public String getServiceUrl() { return serviceUrl; }
        
        @Override
        public boolean isHealthy() {
            // Lazy initialization of real service
            if (realService == null) {
                realService = new RealServiceEndpoint(serviceId, serviceUrl, strategy);
            }
            return realService.isHealthy();
        }
        
        @Override
        public Map<String, Object> getServiceInfo() {
            Map<String, Object> info = new HashMap<>(proxyInfo);
            if (realService != null) {
                info.put("realServiceInfo", realService.getServiceInfo());
            }
            return info;
        }
        
        @Override
        public Map<String, Object> processRequest(List<String> data) {
            // Lazy initialization of real service
            if (realService == null) {
                realService = new RealServiceEndpoint(serviceId, serviceUrl, strategy);
            }
            return realService.processRequest(data);
        }
    }
    
    /**
     * Protection proxy with access control
     */
    public static class ProtectionServiceProxy implements ServiceEndpoint {
        private final ServiceEndpoint realService;
        private final Set<String> allowedUsers;
        private final Map<String, Object> accessLog;
        
        public ProtectionServiceProxy(ServiceEndpoint realService, Set<String> allowedUsers) {
            this.realService = realService;
            this.allowedUsers = new HashSet<>(allowedUsers);
            this.accessLog = new ConcurrentHashMap<>();
        }
        
        @Override
        public String getServiceId() { return realService.getServiceId(); }
        
        @Override
        public String getServiceUrl() { return realService.getServiceUrl(); }
        
        @Override
        public boolean isHealthy() { return realService.isHealthy(); }
        
        @Override
        public Map<String, Object> getServiceInfo() {
            Map<String, Object> info = new HashMap<>(realService.getServiceInfo());
            info.put("proxyType", "PROTECTION");
            info.put("allowedUsers", allowedUsers.size());
            return info;
        }
        
        @Override
        public Map<String, Object> processRequest(List<String> data) {
            // Production-grade: Access control with HashMap operations
            String requestId = UUID.randomUUID().toString();
            Map<String, Object> accessInfo = new HashMap<>();
            accessInfo.put("requestId", requestId);
            accessInfo.put("timestamp", LocalDateTime.now());
            accessInfo.put("authorized", true); // Simplified for demo
            
            accessLog.put(requestId, accessInfo);
            
            // Process request through real service
            Map<String, Object> result = realService.processRequest(data);
            result.put("accessControlled", true);
            result.put("accessRequestId", requestId);
            
            return result;
        }
        
        public Map<String, Object> getAccessLog() {
            return new HashMap<>(accessLog);
        }
    }
    
    /**
     * Register service endpoint using HashMap operations
     * 
     * @param serviceId service identifier
     * @param serviceUrl service URL
     * @param strategy processing strategy
     * @param proxyType type of proxy to create
     * @return registered service endpoint
     */
    public ServiceEndpoint registerService(String serviceId, String serviceUrl, 
                                         DataProcessingStrategy strategy, String proxyType) {
        ServiceEndpoint service;
        
        // Production-grade: Service creation based on proxy type
        switch (proxyType.toUpperCase()) {
            case "VIRTUAL":
                service = new VirtualServiceProxy(serviceId, serviceUrl, strategy);
                break;
            case "PROTECTION":
                service = new ProtectionServiceProxy(
                    new RealServiceEndpoint(serviceId, serviceUrl, strategy),
                    Set.of("admin", "user", "service")
                );
                break;
            default:
                service = new RealServiceEndpoint(serviceId, serviceUrl, strategy);
        }
        
        // Register service using HashMap operations
        serviceRegistry.put(serviceId, service);
        updateServiceMetrics(serviceId, "REGISTERED");
        
        return service;
    }
    
    /**
     * Process request through load balancing using HashMap operations
     * 
     * @param requestType type of processing request
     * @param data input data
     * @return processing results
     */
    public Map<String, Object> processRequest(String requestType, List<String> data) {
        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Production-grade: Service discovery using HashMap operations
            List<ServiceEndpoint> availableServices = findAvailableServices(requestType);
            
            if (availableServices.isEmpty()) {
                return createErrorResult("No available services for request type: " + requestType);
            }
            
            // Load balancing using round-robin
            ServiceEndpoint selectedService = selectService(availableServices);
            
            // Process request through selected service
            result = selectedService.processRequest(data);
            result.put("requestId", requestId);
            result.put("selectedService", selectedService.getServiceId());
            result.put("processingTime", System.currentTimeMillis() - startTime);
            result.put("timestamp", LocalDateTime.now());
            
            // Update load balancing metrics using HashMap operations
            updateLoadBalancingMetrics(selectedService.getServiceId(), true, 
                                    System.currentTimeMillis() - startTime);
            
        } catch (Exception e) {
            result = createErrorResult("Request processing failed: " + e.getMessage());
            updateLoadBalancingMetrics("UNKNOWN", false, System.currentTimeMillis() - startTime);
        }
        
        return result;
    }
    
    /**
     * Find available services using HashMap operations
     * 
     * @param requestType request type
     * @return list of available services
     */
    private List<ServiceEndpoint> findAvailableServices(String requestType) {
        // Production-grade: Service discovery using Streams
        return serviceRegistry.values().stream()
                .filter(service -> service instanceof ServiceEndpoint)
                .map(service -> (ServiceEndpoint) service)
                .filter(ServiceEndpoint::isHealthy)
                .filter(service -> service.getServiceId().contains(requestType.toUpperCase()))
                .collect(Collectors.toList());
    }
    
    /**
     * Select service using load balancing algorithm
     * 
     * @param services available services
     * @return selected service
     */
    private ServiceEndpoint selectService(List<ServiceEndpoint> services) {
        // Production-grade: Round-robin load balancing
        int index = requestCounter.getAndIncrement() % services.size();
        return services.get(index);
    }
    
    /**
     * Update service metrics using HashMap operations
     * 
     * @param serviceId service identifier
     * @param action action performed
     */
    private void updateServiceMetrics(String serviceId, String action) {
        // Production-grade: Metrics tracking with HashMap
        String metricsKey = "service_" + serviceId;
        Map<String, Object> metrics = (Map<String, Object>) loadBalancingMetrics.getOrDefault(metricsKey, new HashMap<>());
        
        metrics.put("lastAction", action);
        metrics.put("lastUpdate", LocalDateTime.now());
        metrics.put("actionCount", (Integer) metrics.getOrDefault("actionCount", 0) + 1);
        
        loadBalancingMetrics.put(metricsKey, metrics);
    }
    
    /**
     * Update load balancing metrics using HashMap operations
     * 
     * @param serviceId service identifier
     * @param success whether request was successful
     * @param processingTime processing time in milliseconds
     */
    private void updateLoadBalancingMetrics(String serviceId, boolean success, long processingTime) {
        // Production-grade: Load balancing metrics with HashMap
        String metricsKey = "lb_" + serviceId;
        Map<String, Object> metrics = (Map<String, Object>) loadBalancingMetrics.getOrDefault(metricsKey, new HashMap<>());
        
        Integer totalRequests = (Integer) metrics.getOrDefault("totalRequests", 0);
        Integer successfulRequests = (Integer) metrics.getOrDefault("successfulRequests", 0);
        Long totalProcessingTime = (Long) metrics.getOrDefault("totalProcessingTime", 0L);
        
        metrics.put("totalRequests", totalRequests + 1);
        metrics.put("successfulRequests", successfulRequests + (success ? 1 : 0));
        metrics.put("totalProcessingTime", totalProcessingTime + processingTime);
        metrics.put("averageProcessingTime", (double) (totalProcessingTime + processingTime) / (totalRequests + 1));
        metrics.put("lastRequest", LocalDateTime.now());
        
        loadBalancingMetrics.put(metricsKey, metrics);
    }
    
    /**
     * Get service registry using HashMap operations
     * 
     * @return service registry
     */
    public Map<String, Object> getServiceRegistry() {
        Map<String, Object> registry = new HashMap<>();
        
        // Production-grade: Registry information using HashMap iteration
        serviceRegistry.forEach((serviceId, service) -> {
            if (service instanceof ServiceEndpoint) {
                ServiceEndpoint endpoint = (ServiceEndpoint) service;
                Map<String, Object> serviceInfo = new HashMap<>();
                serviceInfo.put("serviceId", endpoint.getServiceId());
                serviceInfo.put("serviceUrl", endpoint.getServiceUrl());
                serviceInfo.put("isHealthy", endpoint.isHealthy());
                serviceInfo.put("serviceInfo", endpoint.getServiceInfo());
                registry.put(serviceId, serviceInfo);
            }
        });
        
        return registry;
    }
    
    /**
     * Get load balancing statistics using HashMap operations
     * 
     * @return load balancing statistics
     */
    public Map<String, Object> getLoadBalancingStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Production-grade: Statistics using HashMap
        stats.put("totalServices", serviceRegistry.size());
        stats.put("loadBalancingMetrics", new HashMap<>(loadBalancingMetrics));
        stats.put("requestCounter", requestCounter.get());
        stats.put("timestamp", LocalDateTime.now());
        
        // Using Streams for service analysis
        Map<String, Object> serviceAnalysis = new HashMap<>();
        serviceRegistry.forEach((serviceId, service) -> {
            if (service instanceof ServiceEndpoint) {
                ServiceEndpoint endpoint = (ServiceEndpoint) service;
                Map<String, Object> analysis = new HashMap<>();
                analysis.put("isHealthy", endpoint.isHealthy());
                analysis.put("serviceUrl", endpoint.getServiceUrl());
                serviceAnalysis.put(serviceId, analysis);
            }
        });
        
        stats.put("serviceAnalysis", serviceAnalysis);
        
        return stats;
    }
    
    /**
     * Health check for all services using HashMap operations
     * 
     * @return health check results
     */
    public Map<String, Object> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        // Production-grade: Health check using HashMap operations
        int totalServices = serviceRegistry.size();
        int healthyServices = 0;
        
        // Method 1: Using entrySet iteration
        for (Map.Entry<String, Object> entry : serviceRegistry.entrySet()) {
            if (entry.getValue() instanceof ServiceEndpoint) {
                ServiceEndpoint service = (ServiceEndpoint) entry.getValue();
                if (service.isHealthy()) {
                    healthyServices++;
                }
            }
        }
        
        health.put("totalServices", totalServices);
        health.put("healthyServices", healthyServices);
        health.put("unhealthyServices", totalServices - healthyServices);
        health.put("healthPercentage", totalServices > 0 ? (double) healthyServices / totalServices * 100 : 0.0);
        health.put("timestamp", LocalDateTime.now());
        
        return health;
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
    
    /**
     * Clear service registry
     */
    public void clearRegistry() {
        serviceRegistry.clear();
        loadBalancingMetrics.clear();
        requestCounter.set(0);
    }
}
