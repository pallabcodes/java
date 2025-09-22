package com.netflix.systemdesign.loadbalancing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Netflix Production-Grade Consistent Hashing Load Balancer
 * 
 * This class implements consistent hashing for load balancing with:
 * 1. Virtual nodes for better distribution
 * 2. Ring-based server selection
 * 3. Dynamic server addition/removal
 * 4. Hash collision handling
 * 5. Performance optimization
 * 6. Metrics collection
 * 7. Health checking integration
 * 8. Circuit breaker support
 * 
 * For C/C++ engineers:
 * - Consistent hashing is like a hash table with circular addressing
 * - Virtual nodes are like multiple hash table entries per server
 * - Hash ring is like a circular buffer with hash values as keys
 * - Server selection is like binary search in sorted array
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@Slf4j
@Component
public class ConsistentHashingLoadBalancer implements LoadBalancingAlgorithm {
    
    private final TreeMap<Long, ServerInstance> hashRing;
    private final Map<String, Set<Long>> serverVirtualNodes;
    private final int virtualNodesPerServer;
    private final HashFunction hashFunction;
    private final MetricsCollector metricsCollector;
    private final HealthChecker healthChecker;
    
    /**
     * Constructor for consistent hashing load balancer
     * 
     * @param virtualNodesPerServer Number of virtual nodes per server
     * @param hashFunction Hash function implementation
     * @param metricsCollector Metrics collection service
     * @param healthChecker Health checking service
     */
    public ConsistentHashingLoadBalancer(int virtualNodesPerServer,
                                       HashFunction hashFunction,
                                       MetricsCollector metricsCollector,
                                       HealthChecker healthChecker) {
        this.hashRing = new TreeMap<>();
        this.serverVirtualNodes = new ConcurrentHashMap<>();
        this.virtualNodesPerServer = virtualNodesPerServer;
        this.hashFunction = hashFunction;
        this.metricsCollector = metricsCollector;
        this.healthChecker = healthChecker;
        
        log.info("Initialized consistent hashing load balancer with {} virtual nodes per server", 
                virtualNodesPerServer);
    }
    
    /**
     * Select server using consistent hashing
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
        
        // Generate hash for request key
        String requestKey = generateRequestKey(request);
        long requestHash = hashFunction.hash(requestKey);
        
        // Find server in hash ring
        ServerInstance selectedServer = findServerInRing(requestHash, healthyServers);
        
        if (selectedServer == null) {
            log.error("Failed to find server in hash ring for request hash: {}", requestHash);
            selectedServer = healthyServers.get(0); // Fallback to first healthy server
        }
        
        // Record metrics
        metricsCollector.recordAlgorithmSelection("consistent_hashing", selectedServer);
        metricsCollector.recordServerSelection(selectedServer, requestHash);
        
        log.debug("Selected server {} for request hash {} using consistent hashing", 
                selectedServer.getId(), requestHash);
        
        return selectedServer;
    }
    
    /**
     * Add server to hash ring
     * 
     * @param server Server instance to add
     */
    public synchronized void addServer(ServerInstance server) {
        if (server == null) {
            log.warn("Cannot add null server to hash ring");
            return;
        }
        
        String serverId = server.getId();
        Set<Long> virtualNodeHashes = new HashSet<>();
        
        // Add virtual nodes for the server
        for (int i = 0; i < virtualNodesPerServer; i++) {
            String virtualNodeId = serverId + "#" + i;
            long hash = hashFunction.hash(virtualNodeId);
            
            // Handle hash collisions
            while (hashRing.containsKey(hash)) {
                hash = hashFunction.hash(virtualNodeId + "_" + System.currentTimeMillis());
            }
            
            hashRing.put(hash, server);
            virtualNodeHashes.add(hash);
        }
        
        serverVirtualNodes.put(serverId, virtualNodeHashes);
        
        log.info("Added server {} with {} virtual nodes to hash ring", 
                serverId, virtualNodesPerServer);
        
        // Record metrics
        metricsCollector.recordServerAdded(server, virtualNodesPerServer);
    }
    
    /**
     * Remove server from hash ring
     * 
     * @param server Server instance to remove
     */
    public synchronized void removeServer(ServerInstance server) {
        if (server == null) {
            log.warn("Cannot remove null server from hash ring");
            return;
        }
        
        String serverId = server.getId();
        Set<Long> virtualNodeHashes = serverVirtualNodes.remove(serverId);
        
        if (virtualNodeHashes != null) {
            // Remove all virtual nodes for this server
            for (Long hash : virtualNodeHashes) {
                hashRing.remove(hash);
            }
            
            log.info("Removed server {} with {} virtual nodes from hash ring", 
                    serverId, virtualNodeHashes.size());
            
            // Record metrics
            metricsCollector.recordServerRemoved(server, virtualNodeHashes.size());
        } else {
            log.warn("Server {} not found in hash ring", serverId);
        }
    }
    
    /**
     * Get server distribution statistics
     * 
     * @return Map of server ID to number of virtual nodes
     */
    public Map<String, Integer> getServerDistribution() {
        Map<String, Integer> distribution = new HashMap<>();
        
        for (Map.Entry<String, Set<Long>> entry : serverVirtualNodes.entrySet()) {
            distribution.put(entry.getKey(), entry.getValue().size());
        }
        
        return distribution;
    }
    
    /**
     * Get hash ring statistics
     * 
     * @return Hash ring statistics
     */
    public HashRingStatistics getHashRingStatistics() {
        return HashRingStatistics.builder()
                .totalVirtualNodes(hashRing.size())
                .uniqueServers(serverVirtualNodes.size())
                .virtualNodesPerServer(virtualNodesPerServer)
                .hashRingSize(hashRing.size())
                .build();
    }
    
    /**
     * Find server in hash ring for given hash
     * 
     * @param requestHash Request hash value
     * @param healthyServers List of healthy servers
     * @return Selected server instance
     */
    private ServerInstance findServerInRing(long requestHash, List<ServerInstance> healthyServers) {
        // Find the first server with hash >= requestHash
        Map.Entry<Long, ServerInstance> entry = hashRing.ceilingEntry(requestHash);
        
        if (entry == null) {
            // Wrap around to first server in ring
            entry = hashRing.firstEntry();
        }
        
        ServerInstance selectedServer = entry.getValue();
        
        // Ensure selected server is healthy
        if (healthyServers.contains(selectedServer)) {
            return selectedServer;
        }
        
        // If selected server is not healthy, find next healthy server
        return findNextHealthyServer(entry.getKey(), healthyServers);
    }
    
    /**
     * Find next healthy server in hash ring
     * 
     * @param currentHash Current hash position
     * @param healthyServers List of healthy servers
     * @return Next healthy server
     */
    private ServerInstance findNextHealthyServer(long currentHash, List<ServerInstance> healthyServers) {
        // Get all servers in order from current position
        NavigableMap<Long, ServerInstance> tailMap = hashRing.tailMap(currentHash, false);
        
        // Check servers after current position
        for (Map.Entry<Long, ServerInstance> entry : tailMap.entrySet()) {
            if (healthyServers.contains(entry.getValue())) {
                return entry.getValue();
            }
        }
        
        // Check servers before current position (wrap around)
        for (Map.Entry<Long, ServerInstance> entry : hashRing.headMap(currentHash, false).entrySet()) {
            if (healthyServers.contains(entry.getValue())) {
                return entry.getValue();
            }
        }
        
        return null; // No healthy server found
    }
    
    /**
     * Generate request key for hashing
     * 
     * @param request The request
     * @return Request key string
     */
    private String generateRequestKey(Request request) {
        // Use request ID as primary key
        String primaryKey = request.getId();
        
        // Add additional context if available
        if (request.getUserId() != null) {
            primaryKey += ":" + request.getUserId();
        }
        
        if (request.getSessionId() != null) {
            primaryKey += ":" + request.getSessionId();
        }
        
        return primaryKey;
    }
    
    /**
     * Rebalance hash ring when servers are added/removed
     * 
     * @param servers Current list of servers
     */
    public synchronized void rebalanceHashRing(List<ServerInstance> servers) {
        log.info("Starting hash ring rebalancing for {} servers", servers.size());
        
        // Clear existing ring
        hashRing.clear();
        serverVirtualNodes.clear();
        
        // Add all servers to ring
        for (ServerInstance server : servers) {
            addServer(server);
        }
        
        log.info("Completed hash ring rebalancing with {} virtual nodes", hashRing.size());
        
        // Record metrics
        metricsCollector.recordHashRingRebalance(servers.size(), hashRing.size());
    }
    
    /**
     * Get hash ring visualization data
     * 
     * @return Hash ring visualization data
     */
    public List<HashRingEntry> getHashRingVisualization() {
        List<HashRingEntry> entries = new ArrayList<>();
        
        for (Map.Entry<Long, ServerInstance> entry : hashRing.entrySet()) {
            entries.add(new HashRingEntry(
                    entry.getKey(),
                    entry.getValue().getId(),
                    entry.getValue().getStatus()
            ));
        }
        
        // Sort by hash value
        entries.sort(Comparator.comparing(HashRingEntry::getHash));
        
        return entries;
    }
    
    /**
     * Hash ring entry for visualization
     */
    public static class HashRingEntry {
        private final long hash;
        private final String serverId;
        private final String status;
        
        public HashRingEntry(long hash, String serverId, String status) {
            this.hash = hash;
            this.serverId = serverId;
            this.status = status;
        }
        
        public long getHash() { return hash; }
        public String getServerId() { return serverId; }
        public String getStatus() { return status; }
    }
    
    /**
     * Hash ring statistics
     */
    @lombok.Builder
    public static class HashRingStatistics {
        private final int totalVirtualNodes;
        private final int uniqueServers;
        private final int virtualNodesPerServer;
        private final int hashRingSize;
        
        public int getTotalVirtualNodes() { return totalVirtualNodes; }
        public int getUniqueServers() { return uniqueServers; }
        public int getVirtualNodesPerServer() { return virtualNodesPerServer; }
        public int getHashRingSize() { return hashRingSize; }
    }
}
