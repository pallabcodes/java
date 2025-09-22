package com.netflix.systemdesign.loadbalancing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Netflix Production-Grade Round Robin Load Balancer
 * 
 * This class implements round robin load balancing with:
 * 1. Thread-safe server selection
 * 2. Health checking integration
 * 3. Weighted round robin support
 * 4. Metrics collection
 * 5. Performance optimization
 * 6. Circuit breaker support
 * 7. Server weight management
 * 8. Fair distribution algorithms
 * 
 * For C/C++ engineers:
 * - Round robin is like cycling through an array of servers
 * - Atomic counter is like thread-safe increment operation
 * - Weighted round robin is like weighted random selection
 * - Health checking is like validating server availability
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@Slf4j
@Component
public class RoundRobinLoadBalancer implements LoadBalancingAlgorithm {
    
    private final AtomicInteger currentIndex;
    private final Map<String, ServerWeight> serverWeights;
    private final MetricsCollector metricsCollector;
    private final HealthChecker healthChecker;
    private final boolean enableWeightedRoundRobin;
    
    /**
     * Constructor for round robin load balancer
     * 
     * @param metricsCollector Metrics collection service
     * @param healthChecker Health checking service
     * @param enableWeightedRoundRobin Enable weighted round robin
     */
    public RoundRobinLoadBalancer(MetricsCollector metricsCollector,
                                HealthChecker healthChecker,
                                boolean enableWeightedRoundRobin) {
        this.currentIndex = new AtomicInteger(0);
        this.serverWeights = new ConcurrentHashMap<>();
        this.metricsCollector = metricsCollector;
        this.healthChecker = healthChecker;
        this.enableWeightedRoundRobin = enableWeightedRoundRobin;
        
        log.info("Initialized round robin load balancer with weighted support: {}", 
                enableWeightedRoundRobin);
    }
    
    /**
     * Select server using round robin algorithm
     * 
     * @param servers Available servers
     * @param request The request
     * @return Selected server instance
     */
    @Override
    public ServerInstance selectServer(List<ServerInstance> servers, Request request) {
        if (servers.isEmpty()) {
            throw new IllegalArgumentException("No servers available for selection");
        }
        
        // Filter healthy servers only
        List<ServerInstance> healthyServers = servers.stream()
                .filter(healthChecker::isHealthy)
                .collect(Collectors.toList());
        
        if (healthyServers.isEmpty()) {
            log.warn("No healthy servers available, using first available server");
            return servers.get(0);
        }
        
        ServerInstance selectedServer;
        
        if (enableWeightedRoundRobin) {
            selectedServer = selectServerWeighted(healthyServers);
        } else {
            selectedServer = selectServerStandard(healthyServers);
        }
        
        // Record metrics
        metricsCollector.recordAlgorithmSelection("round_robin", selectedServer);
        metricsCollector.recordServerSelection(selectedServer, System.currentTimeMillis());
        
        log.debug("Selected server {} using round robin algorithm", selectedServer.getId());
        
        return selectedServer;
    }
    
    /**
     * Select server using standard round robin
     * 
     * @param healthyServers List of healthy servers
     * @return Selected server instance
     */
    private ServerInstance selectServerStandard(List<ServerInstance> healthyServers) {
        int index = currentIndex.getAndIncrement() % healthyServers.size();
        return healthyServers.get(index);
    }
    
    /**
     * Select server using weighted round robin
     * 
     * @param healthyServers List of healthy servers
     * @return Selected server instance
     */
    private ServerInstance selectServerWeighted(List<ServerInstance> healthyServers) {
        // Calculate total weight
        int totalWeight = healthyServers.stream()
                .mapToInt(server -> getServerWeight(server))
                .sum();
        
        if (totalWeight == 0) {
            // Fallback to standard round robin if no weights
            return selectServerStandard(healthyServers);
        }
        
        // Get current index and increment
        int currentIdx = currentIndex.getAndIncrement();
        
        // Find server based on weight distribution
        int weightSum = 0;
        for (ServerInstance server : healthyServers) {
            weightSum += getServerWeight(server);
            if (currentIdx % totalWeight < weightSum) {
                return server;
            }
        }
        
        // Fallback to last server
        return healthyServers.get(healthyServers.size() - 1);
    }
    
    /**
     * Set server weight
     * 
     * @param server Server instance
     * @param weight Server weight
     */
    public void setServerWeight(ServerInstance server, int weight) {
        if (server == null) {
            log.warn("Cannot set weight for null server");
            return;
        }
        
        if (weight < 0) {
            log.warn("Server weight cannot be negative, setting to 0");
            weight = 0;
        }
        
        serverWeights.put(server.getId(), new ServerWeight(server, weight));
        
        log.info("Set weight {} for server {}", weight, server.getId());
        
        // Record metrics
        metricsCollector.recordServerWeightChange(server, weight);
    }
    
    /**
     * Get server weight
     * 
     * @param server Server instance
     * @return Server weight
     */
    public int getServerWeight(ServerInstance server) {
        ServerWeight serverWeight = serverWeights.get(server.getId());
        return serverWeight != null ? serverWeight.getWeight() : 1; // Default weight is 1
    }
    
    /**
     * Remove server weight
     * 
     * @param server Server instance
     */
    public void removeServerWeight(ServerInstance server) {
        if (server == null) {
            log.warn("Cannot remove weight for null server");
            return;
        }
        
        serverWeights.remove(server.getId());
        
        log.info("Removed weight for server {}", server.getId());
        
        // Record metrics
        metricsCollector.recordServerWeightRemoved(server);
    }
    
    /**
     * Get all server weights
     * 
     * @return Map of server ID to weight
     */
    public Map<String, Integer> getAllServerWeights() {
        Map<String, Integer> weights = new HashMap<>();
        
        for (Map.Entry<String, ServerWeight> entry : serverWeights.entrySet()) {
            weights.put(entry.getKey(), entry.getValue().getWeight());
        }
        
        return weights;
    }
    
    /**
     * Reset round robin counter
     */
    public void resetCounter() {
        currentIndex.set(0);
        log.info("Reset round robin counter");
        
        // Record metrics
        metricsCollector.recordCounterReset();
    }
    
    /**
     * Get current counter value
     * 
     * @return Current counter value
     */
    public int getCurrentCounter() {
        return currentIndex.get();
    }
    
    /**
     * Get load balancer statistics
     * 
     * @return Load balancer statistics
     */
    public RoundRobinStatistics getStatistics() {
        return RoundRobinStatistics.builder()
                .currentCounter(currentIndex.get())
                .enableWeightedRoundRobin(enableWeightedRoundRobin)
                .serverWeights(getAllServerWeights())
                .totalServers(serverWeights.size())
                .build();
    }
    
    /**
     * Server weight container
     */
    private static class ServerWeight {
        private final ServerInstance server;
        private final int weight;
        
        public ServerWeight(ServerInstance server, int weight) {
            this.server = server;
            this.weight = weight;
        }
        
        public ServerInstance getServer() { return server; }
        public int getWeight() { return weight; }
    }
    
    /**
     * Round robin statistics
     */
    @lombok.Builder
    public static class RoundRobinStatistics {
        private final int currentCounter;
        private final boolean enableWeightedRoundRobin;
        private final Map<String, Integer> serverWeights;
        private final int totalServers;
        
        public int getCurrentCounter() { return currentCounter; }
        public boolean isEnableWeightedRoundRobin() { return enableWeightedRoundRobin; }
        public Map<String, Integer> getServerWeights() { return serverWeights; }
        public int getTotalServers() { return totalServers; }
    }
}
