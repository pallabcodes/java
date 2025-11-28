package com.netflix.productivity.platform.cache;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Enterprise Cache Manager
 *
 * Provides distributed caching capabilities with Redis integration:
 * - Multi-level caching (L1 in-memory, L2 Redis)
 * - Cache invalidation strategies
 * - TTL (Time-To-Live) management
 * - Cache warming and prefetching
 * - Performance monitoring and metrics
 * - Cache consistency and synchronization
 */
@Service
public class CacheManager {

    private static final Logger logger = LoggerFactory.getLogger(CacheManager.class);

    // L1 Cache (in-memory)
    private final ConcurrentHashMap<String, CacheEntry> l1Cache = new ConcurrentHashMap<>();

    // Cache configuration
    private final CacheConfig config = new CacheConfig(
        3600000L, // 1 hour default TTL
        100000L,  // Max 100K entries in L1
        true,     // Enable L2 (Redis)
        10,       // 10 cleanup threads
        300000L   // Cleanup interval: 5 minutes
    );

    // Cache statistics
    private final CacheStats stats = new CacheStats();

    // Background cleanup scheduler
    private final ScheduledExecutorService cleanupExecutor = Executors.newScheduledThreadPool(2);

    // Cache warming strategies
    private final ConcurrentHashMap<String, CacheWarmer> cacheWarmers = new ConcurrentHashMap<>();

    public CacheManager() {
        initializeCacheCleanup();
        initializeCacheWarmers();
    }

    /**
     * Get value from cache with fallback
     */
    public <T> CacheResult<T> get(String key, Class<T> type) {
        stats.totalRequests++;

        // Try L1 cache first
        CacheEntry entry = l1Cache.get(key);
        if (entry != null && !entry.isExpired()) {
            stats.l1Hits++;
            logger.debug("L1 cache hit for key: {}", key);
            return CacheResult.hit((T) entry.getValue());
        }

        // Try L2 cache (Redis simulation)
        Object l2Value = getFromL2Cache(key);
        if (l2Value != null) {
            stats.l2Hits++;
            // Populate L1 cache
            l1Cache.put(key, new CacheEntry(l2Value, System.currentTimeMillis() + config.defaultTtl));
            logger.debug("L2 cache hit for key: {}", key);
            return CacheResult.hit((T) l2Value);
        }

        stats.misses++;
        logger.debug("Cache miss for key: {}", key);
        return CacheResult.miss();
    }

    /**
     * Put value in cache
     */
    public void put(String key, Object value, long ttlMillis) {
        long expirationTime = System.currentTimeMillis() + ttlMillis;

        // Store in L1 cache
        l1Cache.put(key, new CacheEntry(value, expirationTime));

        // Store in L2 cache (Redis simulation)
        putInL2Cache(key, value, ttlMillis);

        stats.totalEntries++;
        logger.debug("Cached value for key: {} with TTL: {}ms", key, ttlMillis);
    }

    /**
     * Put value in cache with default TTL
     */
    public void put(String key, Object value) {
        put(key, value, config.defaultTtl);
    }

    /**
     * Remove value from cache
     */
    public boolean remove(String key) {
        boolean l1Removed = l1Cache.remove(key) != null;
        boolean l2Removed = removeFromL2Cache(key);

        if (l1Removed || l2Removed) {
            stats.invalidations++;
            logger.debug("Removed cache entry for key: {}", key);
            return true;
        }

        return false;
    }

    /**
     * Check if key exists in cache
     */
    public boolean contains(String key) {
        // Check L1 first
        CacheEntry entry = l1Cache.get(key);
        if (entry != null && !entry.isExpired()) {
            return true;
        }

        // Check L2
        return containsInL2Cache(key);
    }

    /**
     * Clear all cache entries
     */
    public void clear() {
        l1Cache.clear();
        clearL2Cache();
        stats.invalidations += stats.totalEntries;
        stats.totalEntries = 0;
        logger.info("Cleared all cache entries");
    }

    /**
     * Invalidate cache entries by pattern
     */
    public int invalidateByPattern(String pattern) {
        int invalidated = 0;

        // Invalidate L1 cache
        l1Cache.entrySet().removeIf(entry -> {
            if (entry.getKey().matches(pattern.replace("*", ".*"))) {
                invalidated++;
                return true;
            }
            return false;
        });

        // Invalidate L2 cache
        invalidated += invalidateL2ByPattern(pattern);

        stats.invalidations += invalidated;
        logger.info("Invalidated {} cache entries matching pattern: {}", invalidated, pattern);

        return invalidated;
    }

    /**
     * Cache warming - preload frequently accessed data
     */
    public void warmCache(String cacheKey, CacheWarmer warmer) {
        cacheWarmers.put(cacheKey, warmer);

        // Execute cache warming asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                logger.info("Starting cache warming for key: {}", cacheKey);
                Object warmedData = warmer.warm();
                if (warmedData != null) {
                    put(cacheKey, warmedData);
                    logger.info("Cache warming completed for key: {}", cacheKey);
                }
            } catch (Exception e) {
                logger.error("Cache warming failed for key: {}", cacheKey, e);
            }
        });
    }

    /**
     * Get or compute with caching
     */
    public <T> T getOrCompute(String key, java.util.function.Supplier<T> supplier, long ttlMillis) {
        CacheResult<T> cached = get(key, null);
        if (cached.isHit()) {
            return cached.getValue();
        }

        // Compute and cache
        T computed = supplier.get();
        put(key, computed, ttlMillis);
        return computed;
    }

    /**
     * Get cache statistics
     */
    public CacheStats getCacheStats() {
        return new CacheStats(
            stats.totalRequests,
            stats.l1Hits,
            stats.l2Hits,
            stats.misses,
            stats.invalidations,
            l1Cache.size(),
            getL2CacheSize(),
            calculateHitRate(),
            calculateL1HitRate(),
            calculateL2HitRate()
        );
    }

    /**
     * Business-specific cache operations
     */

    // Issue-related caching
    public void cacheIssue(String issueId, Object issueData) {
        put("issue:" + issueId, issueData, 1800000L); // 30 minutes
    }

    public void invalidateIssueCache(String issueId) {
        remove("issue:" + issueId);
        invalidateByPattern("issue:" + issueId + ":*"); // Invalidate related data
    }

    // Project-related caching
    public void cacheProject(String projectId, Object projectData) {
        put("project:" + projectId, projectData, 3600000L); // 1 hour
    }

    public void invalidateProjectCache(String projectId) {
        remove("project:" + projectId);
        invalidateByPattern("project:" + projectId + ":*");
        // Also invalidate related issues
        invalidateByPattern("issue:*:project:" + projectId);
    }

    // User-related caching
    public void cacheUser(String userId, Object userData) {
        put("user:" + userId, userData, 7200000L); // 2 hours
    }

    public void invalidateUserCache(String userId) {
        remove("user:" + userId);
        invalidateByPattern("user:" + userId + ":*");
    }

    // Search result caching
    public void cacheSearchResults(String query, Object results) {
        put("search:" + query.hashCode(), results, 600000L); // 10 minutes
    }

    /**
     * Private helper methods
     */

    private void initializeCacheCleanup() {
        cleanupExecutor.scheduleAtFixedRate(this::performCacheCleanup,
                                          config.cleanupInterval,
                                          config.cleanupInterval,
                                          TimeUnit.MILLISECONDS);
    }

    private void initializeCacheWarmers() {
        // Initialize common cache warmers
        // These would be configured based on application needs
        logger.info("Cache warmers initialized");
    }

    private void performCacheCleanup() {
        logger.debug("Performing cache cleanup");

        long now = System.currentTimeMillis();
        int cleaned = l1Cache.entrySet().removeIf(entry -> entry.getValue().isExpired(now));

        if (cleaned > 0) {
            logger.debug("Cleaned {} expired L1 cache entries", cleaned);
        }
    }

    // L2 Cache simulation methods (would interface with Redis in production)
    private Object getFromL2Cache(String key) {
        // Simulate Redis get operation
        // In production, this would use Jedis or Lettuce
        return null; // Simulate cache miss for demo
    }

    private void putInL2Cache(String key, Object value, long ttlMillis) {
        // Simulate Redis set operation
        // In production, this would use Jedis or Lettuce
        logger.debug("Stored in L2 cache: {}", key);
    }

    private boolean removeFromL2Cache(String key) {
        // Simulate Redis del operation
        return false; // Simulate not found for demo
    }

    private boolean containsInL2Cache(String key) {
        // Simulate Redis exists operation
        return false; // Simulate not found for demo
    }

    private void clearL2Cache() {
        // Simulate Redis flush operation
        logger.debug("Cleared L2 cache");
    }

    private int invalidateL2ByPattern(String pattern) {
        // Simulate Redis key deletion by pattern
        return 0; // Simulate no matches for demo
    }

    private int getL2CacheSize() {
        // Simulate Redis dbsize operation
        return 0; // Simulate empty for demo
    }

    private double calculateHitRate() {
        long totalHits = stats.l1Hits + stats.l2Hits;
        return stats.totalRequests > 0 ? (double) totalHits / stats.totalRequests : 0.0;
    }

    private double calculateL1HitRate() {
        return stats.totalRequests > 0 ? (double) stats.l1Hits / stats.totalRequests : 0.0;
    }

    private double calculateL2HitRate() {
        long l1Misses = stats.totalRequests - stats.l1Hits;
        return l1Misses > 0 ? (double) stats.l2Hits / l1Misses : 0.0;
    }

    /**
     * Shutdown cache manager
     */
    public void shutdown() {
        logger.info("Shutting down cache manager");
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("Cache manager shutdown complete");
    }
}

/**
 * Data classes for caching
 */

class CacheEntry {
    private final Object value;
    private final long expirationTime;

    public CacheEntry(Object value, long expirationTime) {
        this.value = value;
        this.expirationTime = expirationTime;
    }

    public Object getValue() {
        return value;
    }

    public boolean isExpired() {
        return isExpired(System.currentTimeMillis());
    }

    public boolean isExpired(long currentTime) {
        return currentTime > expirationTime;
    }

    public long getExpirationTime() {
        return expirationTime;
    }
}

class CacheResult<T> {
    private final boolean hit;
    private final T value;

    private CacheResult(boolean hit, T value) {
        this.hit = hit;
        this.value = value;
    }

    public static <T> CacheResult<T> hit(T value) {
        return new CacheResult<>(true, value);
    }

    public static <T> CacheResult<T> miss() {
        return new CacheResult<>(false, null);
    }

    public boolean isHit() {
        return hit;
    }

    public boolean isMiss() {
        return !hit;
    }

    public T getValue() {
        if (!hit) {
            throw new IllegalStateException("Cannot get value from cache miss");
        }
        return value;
    }
}

class CacheConfig {
    final long defaultTtl;
    final long maxL1Entries;
    final boolean enableL2;
    final int cleanupThreads;
    final long cleanupInterval;

    public CacheConfig(long defaultTtl, long maxL1Entries, boolean enableL2,
                      int cleanupThreads, long cleanupInterval) {
        this.defaultTtl = defaultTtl;
        this.maxL1Entries = maxL1Entries;
        this.enableL2 = enableL2;
        this.cleanupThreads = cleanupThreads;
        this.cleanupInterval = cleanupInterval;
    }
}

class CacheStats {
    volatile long totalRequests = 0;
    volatile long l1Hits = 0;
    volatile long l2Hits = 0;
    volatile long misses = 0;
    volatile long invalidations = 0;
    volatile long totalEntries = 0;

    // Constructor for immutable stats
    public CacheStats(long totalRequests, long l1Hits, long l2Hits, long misses,
                     long invalidations, long l1Size, long l2Size, double hitRate,
                     double l1HitRate, double l2HitRate) {
        this.totalRequests = totalRequests;
        this.l1Hits = l1Hits;
        this.l2Hits = l2Hits;
        this.misses = misses;
        this.invalidations = invalidations;
    }

    // Default constructor for mutable stats
    public CacheStats() {}
}

@FunctionalInterface
interface CacheWarmer {
    Object warm() throws Exception;
}

/**
 * Specialized cache warmers for common use cases
 */

class IssueCacheWarmer implements CacheWarmer {
    private final String projectId;

    public IssueCacheWarmer(String projectId) {
        this.projectId = projectId;
    }

    @Override
    public Object warm() throws Exception {
        // In production, this would query the database for recent issues
        // and return them for caching
        return java.util.List.of("issue1", "issue2", "issue3"); // Mock data
    }
}

class UserPermissionCacheWarmer implements CacheWarmer {
    private final String userId;

    public UserPermissionCacheWarmer(String userId) {
        this.userId = userId;
    }

    @Override
    public Object warm() throws Exception {
        // In production, this would load user permissions from database
        return java.util.Set.of("READ_ISSUES", "CREATE_ISSUES", "UPDATE_ISSUES"); // Mock data
    }
}

class ProjectStatsCacheWarmer implements CacheWarmer {
    private final String projectId;

    public ProjectStatsCacheWarmer(String projectId) {
        this.projectId = projectId;
    }

    @Override
    public Object warm() throws Exception {
        // In production, this would calculate project statistics
        return java.util.Map.of(
            "totalIssues", 25,
            "openIssues", 10,
            "closedIssues", 15,
            "activeUsers", 8
        ); // Mock data
    }
}
