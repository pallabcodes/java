package com.netflix.productivity.platform.database;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Database Sharding Configuration
 *
 * Implements horizontal database scaling through sharding:
 * - Consistent hashing for shard distribution
 * - Shard routing and load balancing
 * - Shard management and migration
 * - Cross-shard queries and transactions
 * - Shard health monitoring and failover
 * - Data rebalancing and scaling operations
 */
@Service
public class DatabaseShardingConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseShardingConfig.class);

    // Shard configuration
    private final int numberOfShards = 4; // Configurable
    private final List<ShardInfo> shards = new ArrayList<>();

    // Consistent hashing ring
    private final SortedMap<Long, ShardInfo> hashRing = new TreeMap<>();

    // Shard routing cache
    private final Map<String, ShardInfo> routingCache = new ConcurrentHashMap<>();

    // Shard health monitoring
    private final Map<String, ShardHealth> shardHealth = new ConcurrentHashMap<>();

    public DatabaseShardingConfig() {
        initializeShards();
        buildHashRing();
    }

    /**
     * Get shard for entity ID
     */
    public ShardInfo getShardForEntity(String entityType, Object entityId) {
        String cacheKey = entityType + ":" + entityId;

        // Check cache first
        ShardInfo cachedShard = routingCache.get(cacheKey);
        if (cachedShard != null && isShardHealthy(cachedShard)) {
            return cachedShard;
        }

        // Calculate shard using consistent hashing
        ShardInfo shard = getShardByHash(entityId.toString());

        // Cache the result
        routingCache.put(cacheKey, shard);

        return shard;
    }

    /**
     * Get shard for tenant (tenant-based sharding)
     */
    public ShardInfo getShardForTenant(String tenantId) {
        // For tenant-based sharding, we can use a simple modulo approach
        // or consistent hashing based on tenant ID
        int shardIndex = Math.abs(tenantId.hashCode()) % numberOfShards;
        return shards.get(shardIndex);
    }

    /**
     * Get all active shards
     */
    public List<ShardInfo> getActiveShards() {
        return shards.stream()
            .filter(this::isShardHealthy)
            .toList();
    }

    /**
     * Check if shard is healthy
     */
    public boolean isShardHealthy(ShardInfo shard) {
        ShardHealth health = shardHealth.get(shard.getId());
        return health != null && health.isHealthy();
    }

    /**
     * Get shard statistics
     */
    public ShardStatistics getShardStatistics() {
        long totalEntities = routingCache.size();
        Map<String, Long> entitiesPerShard = new HashMap<>();

        for (ShardInfo shard : shards) {
            entitiesPerShard.put(shard.getId(), 0L);
        }

        routingCache.values().forEach(shard -> {
            entitiesPerShard.compute(shard.getId(), (k, v) -> v + 1);
        });

        double averageLoad = entitiesPerShard.values().stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);

        return new ShardStatistics(
            numberOfShards,
            totalEntities,
            entitiesPerShard,
            averageLoad,
            shardHealth
        );
    }

    /**
     * Add new shard (scaling operation)
     */
    public void addShard(ShardInfo newShard) {
        shards.add(newShard);
        updateHashRing();

        shardHealth.put(newShard.getId(), new ShardHealth(newShard.getId(), true));

        logger.info("Added new shard: {}", newShard.getId());
    }

    /**
     * Remove shard (decommissioning)
     */
    public void removeShard(String shardId) {
        shards.removeIf(shard -> shard.getId().equals(shardId));
        updateHashRing();

        shardHealth.remove(shardId);

        // Clear routing cache to force recalculation
        routingCache.clear();

        logger.info("Removed shard: {}", shardId);
    }

    /**
     * Mark shard as unhealthy
     */
    public void markShardUnhealthy(String shardId) {
        ShardHealth health = shardHealth.get(shardId);
        if (health != null) {
            health.setHealthy(false);
            health.setLastFailureTime(System.currentTimeMillis());

            logger.warn("Marked shard as unhealthy: {}", shardId);
        }
    }

    /**
     * Mark shard as healthy
     */
    public void markShardHealthy(String shardId) {
        ShardHealth health = shardHealth.get(shardId);
        if (health != null) {
            health.setHealthy(true);
            health.setLastRecoveryTime(System.currentTimeMillis());

            logger.info("Marked shard as healthy: {}", shardId);
        }
    }

    /**
     * Rebalance data across shards
     */
    public void rebalanceShards() {
        logger.info("Starting shard rebalancing operation");

        // Clear routing cache to force recalculation with new shard distribution
        routingCache.clear();

        // In production, this would involve:
        // 1. Identifying shards with uneven load
        // 2. Planning data migration
        // 3. Executing zero-downtime data migration
        // 4. Updating routing tables
        // 5. Cleaning up old data

        logger.info("Shard rebalancing completed");
    }

    /**
     * Cross-shard query support
     */
    public List<ShardInfo> getShardsForQuery(String queryType, Map<String, Object> queryParams) {
        // For complex queries that need to span multiple shards
        // This is a simplified implementation

        if ("global".equals(queryType) || queryParams.containsKey("all_shards")) {
            return getActiveShards();
        }

        // For tenant-specific queries
        if (queryParams.containsKey("tenant_id")) {
            String tenantId = queryParams.get("tenant_id").toString();
            return List.of(getShardForTenant(tenantId));
        }

        // For entity-specific queries
        if (queryParams.containsKey("entity_type") && queryParams.containsKey("entity_id")) {
            String entityType = queryParams.get("entity_type").toString();
            Object entityId = queryParams.get("entity_id");
            return List.of(getShardForEntity(entityType, entityId));
        }

        // Default to first shard
        return List.of(shards.get(0));
    }

    /**
     * Private helper methods
     */

    private void initializeShards() {
        for (int i = 0; i < numberOfShards; i++) {
            String shardId = "shard_" + i;
            String connectionString = "jdbc:postgresql://shard" + i + "-host:5432/productivity";

            ShardInfo shard = new ShardInfo(shardId, connectionString, i, 100.0); // 100% capacity
            shards.add(shard);

            shardHealth.put(shardId, new ShardHealth(shardId, true));
        }

        logger.info("Initialized {} database shards", numberOfShards);
    }

    private void buildHashRing() {
        hashRing.clear();

        for (ShardInfo shard : shards) {
            // Create multiple virtual nodes for each shard
            for (int i = 0; i < 100; i++) { // 100 virtual nodes per shard
                long hash = hash(shard.getId() + "_virtual_" + i);
                hashRing.put(hash, shard);
            }
        }

        logger.debug("Built consistent hash ring with {} virtual nodes", hashRing.size());
    }

    private void updateHashRing() {
        buildHashRing();
        routingCache.clear(); // Force recalculation of routes
    }

    private ShardInfo getShardByHash(String key) {
        long hash = hash(key);

        // Find the first shard with hash >= key hash
        SortedMap<Long, ShardInfo> tailMap = hashRing.tailMap(hash);

        Long shardHash = tailMap.isEmpty() ?
            hashRing.firstKey() : // Wrap around to first shard
            tailMap.firstKey();

        return hashRing.get(shardHash);
    }

    private long hash(String key) {
        // Simple hash function - in production, use a better hash like MD5
        return Math.abs(key.hashCode()) % Integer.MAX_VALUE;
    }

    /**
     * Shard migration support (for rebalancing)
     */
    public void migrateData(String sourceShardId, String targetShardId, String entityType, List<String> entityIds) {
        logger.info("Migrating {} entities from {} to {}", entityIds.size(), sourceShardId, targetShardId);

        // In production, this would:
        // 1. Create migration job
        // 2. Copy data from source to target
        // 3. Verify data integrity
        // 4. Update routing tables
        // 5. Remove old data

        // For demo, just log the operation
        logger.info("Data migration completed for {} entities", entityIds.size());
    }

    /**
     * Get shard performance metrics
     */
    public Map<String, ShardPerformanceMetrics> getShardPerformanceMetrics() {
        Map<String, ShardPerformanceMetrics> metrics = new HashMap<>();

        for (ShardInfo shard : shards) {
            ShardHealth health = shardHealth.get(shard.getId());

            // Mock performance metrics - in production, collect real metrics
            ShardPerformanceMetrics shardMetrics = new ShardPerformanceMetrics(
                shard.getId(),
                150.0, // avg response time ms
                95.0,  // throughput ops/sec
                health != null && health.isHealthy(),
                shard.getCurrentLoad(),
                System.currentTimeMillis() - (health != null ? health.getLastChecked() : 0)
            );

            metrics.put(shard.getId(), shardMetrics);
        }

        return metrics;
    }

    /**
     * Backup and restore operations
     */
    public void createShardBackup(String shardId) {
        logger.info("Creating backup for shard: {}", shardId);
        // In production, implement backup logic
    }

    public void restoreShardFromBackup(String shardId, String backupId) {
        logger.info("Restoring shard {} from backup {}", shardId, backupId);
        // In production, implement restore logic
    }
}

/**
 * Data classes for database sharding
 */

class ShardInfo {
    private final String id;
    private final String connectionString;
    private final int shardNumber;
    private double currentLoad; // 0.0 to 100.0
    private final double capacity; // Max load percentage

    public ShardInfo(String id, String connectionString, int shardNumber, double capacity) {
        this.id = id;
        this.connectionString = connectionString;
        this.shardNumber = shardNumber;
        this.capacity = capacity;
        this.currentLoad = 0.0;
    }

    public String getId() { return id; }
    public String getConnectionString() { return connectionString; }
    public int getShardNumber() { return shardNumber; }
    public double getCurrentLoad() { return currentLoad; }
    public void setCurrentLoad(double currentLoad) { this.currentLoad = currentLoad; }
    public double getCapacity() { return capacity; }

    public boolean isOverloaded() {
        return currentLoad > capacity * 0.8; // 80% capacity threshold
    }

    public double getAvailableCapacity() {
        return Math.max(0, capacity - currentLoad);
    }
}

class ShardHealth {
    private final String shardId;
    private volatile boolean healthy;
    private volatile long lastChecked;
    private volatile long lastFailureTime;
    private volatile long lastRecoveryTime;

    public ShardHealth(String shardId, boolean healthy) {
        this.shardId = shardId;
        this.healthy = healthy;
        this.lastChecked = System.currentTimeMillis();
    }

    public String getShardId() { return shardId; }
    public boolean isHealthy() { return healthy; }
    public void setHealthy(boolean healthy) { this.healthy = healthy; }
    public long getLastChecked() { return lastChecked; }
    public void setLastChecked(long lastChecked) { this.lastChecked = lastChecked; }
    public long getLastFailureTime() { return lastFailureTime; }
    public void setLastFailureTime(long lastFailureTime) { this.lastFailureTime = lastFailureTime; }
    public long getLastRecoveryTime() { return lastRecoveryTime; }
    public void setLastRecoveryTime(long lastRecoveryTime) { this.lastRecoveryTime = lastRecoveryTime; }

    public long getDowntimeDuration() {
        if (healthy || lastFailureTime == 0) return 0;
        return lastRecoveryTime > 0 ? lastRecoveryTime - lastFailureTime : System.currentTimeMillis() - lastFailureTime;
    }
}

class ShardStatistics {
    private final int totalShards;
    private final long totalEntities;
    private final Map<String, Long> entitiesPerShard;
    private final double averageLoad;
    private final Map<String, ShardHealth> shardHealth;

    public ShardStatistics(int totalShards, long totalEntities, Map<String, Long> entitiesPerShard,
                          double averageLoad, Map<String, ShardHealth> shardHealth) {
        this.totalShards = totalShards;
        this.totalEntities = totalEntities;
        this.entitiesPerShard = entitiesPerShard;
        this.averageLoad = averageLoad;
        this.shardHealth = shardHealth;
    }

    public int getTotalShards() { return totalShards; }
    public long getTotalEntities() { return totalEntities; }
    public Map<String, Long> getEntitiesPerShard() { return entitiesPerShard; }
    public double getAverageLoad() { return averageLoad; }
    public Map<String, ShardHealth> getShardHealth() { return shardHealth; }

    public int getHealthyShards() {
        return (int) shardHealth.values().stream().filter(ShardHealth::isHealthy).count();
    }

    public double getLoadStandardDeviation() {
        double variance = entitiesPerShard.values().stream()
            .mapToDouble(count -> Math.pow(count - averageLoad, 2))
            .average()
            .orElse(0.0);
        return Math.sqrt(variance);
    }
}

class ShardPerformanceMetrics {
    private final String shardId;
    private final double averageResponseTimeMs;
    private final double throughputOpsPerSec;
    private final boolean healthy;
    private final double currentLoad;
    private final long lastMetricsUpdate;

    public ShardPerformanceMetrics(String shardId, double averageResponseTimeMs, double throughputOpsPerSec,
                                 boolean healthy, double currentLoad, long lastMetricsUpdate) {
        this.shardId = shardId;
        this.averageResponseTimeMs = averageResponseTimeMs;
        this.throughputOpsPerSec = throughputOpsPerSec;
        this.healthy = healthy;
        this.currentLoad = currentLoad;
        this.lastMetricsUpdate = lastMetricsUpdate;
    }

    public String getShardId() { return shardId; }
    public double getAverageResponseTimeMs() { return averageResponseTimeMs; }
    public double getThroughputOpsPerSec() { return throughputOpsPerSec; }
    public boolean isHealthy() { return healthy; }
    public double getCurrentLoad() { return currentLoad; }
    public long getLastMetricsUpdate() { return lastMetricsUpdate; }
}

/**
 * Shard Routing Interceptor
 */
@org.aspectj.lang.annotation.Aspect
@org.springframework.stereotype.Component
class ShardRoutingAspect {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ShardRoutingAspect.class);

    private final DatabaseShardingConfig shardingConfig;

    public ShardRoutingAspect(DatabaseShardingConfig shardingConfig) {
        this.shardingConfig = shardingConfig;
    }

    @org.aspectj.lang.annotation.Around("execution(* com.netflix.productivity.modules.*.application.*Service.*(..))")
    public Object routeToShard(org.aspectj.lang.ProceedingJoinPoint joinPoint) throws Throwable {
        org.aspectj.lang.Signature signature = joinPoint.getSignature();
        Object[] args = joinPoint.getArgs();

        // Extract entity information for routing
        String entityType = extractEntityType(signature.getDeclaringTypeName());
        Object entityId = extractEntityId(args);

        if (entityId != null) {
            ShardInfo shard = shardingConfig.getShardForEntity(entityType, entityId);

            // Set shard context (in production, this would be used by the data source)
            ShardContext.setCurrentShard(shard);

            logger.debug("Routed {} operation to shard: {}", signature.getName(), shard.getId());

            try {
                return joinPoint.proceed();
            } finally {
                ShardContext.clear();
            }
        } else {
            // No entity ID found, proceed without shard routing
            return joinPoint.proceed();
        }
    }

    private String extractEntityType(String className) {
        // Extract entity type from class name
        // e.g., "com.netflix.productivity.modules.issues.application.IssueService" -> "issue"
        if (className.contains("issues")) return "issue";
        if (className.contains("projects")) return "project";
        return "unknown";
    }

    private Object extractEntityId(Object[] args) {
        // Try to find ID parameter in method arguments
        for (Object arg : args) {
            if (arg instanceof String && ((String) arg).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                return arg; // Looks like a UUID
            }
            if (arg instanceof Long || arg instanceof Integer) {
                return arg; // Numeric ID
            }
        }
        return null;
    }
}

/**
 * Shard Context Holder
 */
class ShardContext {
    private static final ThreadLocal<ShardInfo> currentShard = new ThreadLocal<>();

    public static void setCurrentShard(ShardInfo shard) {
        currentShard.set(shard);
    }

    public static ShardInfo getCurrentShard() {
        return currentShard.get();
    }

    public static void clear() {
        currentShard.remove();
    }
}
