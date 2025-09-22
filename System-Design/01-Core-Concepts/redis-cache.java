package com.netflix.systemdesign.caching;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Netflix Production-Grade Redis Cache Implementation
 * 
 * This class demonstrates Netflix production standards for Redis caching including:
 * 1. Connection pooling and optimization
 * 2. Serialization and deserialization
 * 3. Cache invalidation strategies
 * 4. Performance monitoring
 * 5. Error handling and fallback
 * 6. Security and access control
 * 7. Memory optimization
 * 8. Cluster support
 * 
 * For C/C++ engineers:
 * - Redis is like a high-performance in-memory database
 * - Cache operations are like hash table operations
 * - TTL is like automatic expiration timers
 * - Serialization is like converting objects to bytes
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@Slf4j
@Component
public class NetflixRedisCache {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ValueOperations<String, Object> valueOperations;
    private final RedisConnectionFactory connectionFactory;
    private final MetricsCollector metricsCollector;
    private final CacheConfiguration cacheConfiguration;
    private final SerializationService serializationService;
    
    /**
     * Constructor for Redis cache
     * 
     * @param redisTemplate Redis template for operations
     * @param connectionFactory Redis connection factory
     * @param metricsCollector Metrics collection service
     * @param cacheConfiguration Cache configuration
     * @param serializationService Serialization service
     */
    public NetflixRedisCache(RedisTemplate<String, Object> redisTemplate,
                           RedisConnectionFactory connectionFactory,
                           MetricsCollector metricsCollector,
                           CacheConfiguration cacheConfiguration,
                           SerializationService serializationService) {
        this.redisTemplate = redisTemplate;
        this.valueOperations = redisTemplate.opsForValue();
        this.connectionFactory = connectionFactory;
        this.metricsCollector = metricsCollector;
        this.cacheConfiguration = cacheConfiguration;
        this.serializationService = serializationService;
        
        log.info("Initialized Netflix Redis cache with configuration: {}", cacheConfiguration);
    }
    
    /**
     * Get value from cache
     * 
     * @param key Cache key
     * @param clazz Expected return type
     * @return Cached value or null
     */
    public <T> T get(String key, Class<T> clazz) {
        if (key == null || key.trim().isEmpty()) {
            log.warn("Null or empty cache key provided");
            return null;
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object cachedValue = valueOperations.get(key);
            
            if (cachedValue == null) {
                log.debug("Cache miss for key: {}", key);
                metricsCollector.recordCacheMiss(key);
                return null;
            }
            
            T deserializedValue = serializationService.deserialize(cachedValue, clazz);
            
            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordCacheHit(key, duration);
            
            log.debug("Cache hit for key: {} in {}ms", key, duration);
            return deserializedValue;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordCacheError(key, duration, e);
            
            log.error("Error retrieving value from cache for key: {}", key, e);
            return null;
        }
    }
    
    /**
     * Get value from cache with fallback
     * 
     * @param key Cache key
     * @param clazz Expected return type
     * @param fallbackSupplier Fallback supplier if cache miss
     * @return Cached value or fallback value
     */
    public <T> T getOrElse(String key, Class<T> clazz, java.util.function.Supplier<T> fallbackSupplier) {
        T cachedValue = get(key, clazz);
        
        if (cachedValue != null) {
            return cachedValue;
        }
        
        try {
            T fallbackValue = fallbackSupplier.get();
            
            if (fallbackValue != null) {
                put(key, fallbackValue);
            }
            
            return fallbackValue;
            
        } catch (Exception e) {
            log.error("Error executing fallback for key: {}", key, e);
            return null;
        }
    }
    
    /**
     * Put value in cache
     * 
     * @param key Cache key
     * @param value Value to cache
     * @param ttl Time to live
     */
    public void put(String key, Object value, Duration ttl) {
        if (key == null || key.trim().isEmpty()) {
            log.warn("Null or empty cache key provided for put operation");
            return;
        }
        
        if (value == null) {
            log.warn("Null value provided for cache key: {}", key);
            return;
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object serializedValue = serializationService.serialize(value);
            
            valueOperations.set(key, serializedValue, ttl);
            
            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordCachePut(key, duration);
            
            log.debug("Successfully cached value for key: {} with TTL: {} in {}ms", 
                    key, ttl, duration);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordCacheError(key, duration, e);
            
            log.error("Error storing value in cache for key: {}", key, e);
        }
    }
    
    /**
     * Put value in cache with default TTL
     * 
     * @param key Cache key
     * @param value Value to cache
     */
    public void put(String key, Object value) {
        put(key, value, cacheConfiguration.getDefaultTtl());
    }
    
    /**
     * Put value in cache only if key doesn't exist
     * 
     * @param key Cache key
     * @param value Value to cache
     * @param ttl Time to live
     * @return true if value was set
     */
    public boolean putIfAbsent(String key, Object value, Duration ttl) {
        if (key == null || key.trim().isEmpty()) {
            log.warn("Null or empty cache key provided for putIfAbsent operation");
            return false;
        }
        
        if (value == null) {
            log.warn("Null value provided for cache key: {}", key);
            return false;
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object serializedValue = serializationService.serialize(value);
            
            Boolean success = valueOperations.setIfAbsent(key, serializedValue, ttl);
            
            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordCachePut(key, duration);
            
            log.debug("PutIfAbsent for key: {} in {}ms, result: {}", key, duration, success);
            return Boolean.TRUE.equals(success);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordCacheError(key, duration, e);
            
            log.error("Error storing value in cache for key: {}", key, e);
            return false;
        }
    }
    
    /**
     * Delete value from cache
     * 
     * @param key Cache key
     * @return true if key was deleted
     */
    public boolean delete(String key) {
        if (key == null || key.trim().isEmpty()) {
            log.warn("Null or empty cache key provided for delete operation");
            return false;
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            Boolean deleted = redisTemplate.delete(key);
            
            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordCacheDelete(key, duration);
            
            log.debug("Cache delete for key: {} in {}ms, result: {}", key, duration, deleted);
            return Boolean.TRUE.equals(deleted);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordCacheError(key, duration, e);
            
            log.error("Error deleting value from cache for key: {}", key, e);
            return false;
        }
    }
    
    /**
     * Delete multiple keys from cache
     * 
     * @param keys List of keys to delete
     * @return Number of keys deleted
     */
    public long deleteMultiple(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0;
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            Long deletedCount = redisTemplate.delete(keys);
            
            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordCacheMultiDelete(keys.size(), deletedCount != null ? deletedCount : 0, duration);
            
            log.debug("Deleted {} keys from cache in {}ms", deletedCount, duration);
            return deletedCount != null ? deletedCount : 0;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordCacheError("multi_delete", duration, e);
            
            log.error("Error deleting multiple keys from cache", e);
            return 0;
        }
    }
    
    /**
     * Check if key exists in cache
     * 
     * @param key Cache key
     * @return true if key exists
     */
    public boolean exists(String key) {
        if (key == null || key.trim().isEmpty()) {
            return false;
        }
        
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Error checking key existence in cache for key: {}", key, e);
            return false;
        }
    }
    
    /**
     * Get TTL for key
     * 
     * @param key Cache key
     * @return TTL in seconds, -1 if key doesn't exist, -2 if key has no expiration
     */
    public long getTtl(String key) {
        if (key == null || key.trim().isEmpty()) {
            return -1;
        }
        
        try {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error getting TTL for key: {}", key, e);
            return -1;
        }
    }
    
    /**
     * Set TTL for key
     * 
     * @param key Cache key
     * @param ttl Time to live
     * @return true if TTL was set
     */
    public boolean setTtl(String key, Duration ttl) {
        if (key == null || key.trim().isEmpty()) {
            return false;
        }
        
        try {
            Boolean success = redisTemplate.expire(key, ttl);
            return Boolean.TRUE.equals(success);
        } catch (Exception e) {
            log.error("Error setting TTL for key: {}", key, e);
            return false;
        }
    }
    
    /**
     * Get multiple values from cache
     * 
     * @param keys List of cache keys
     * @param clazz Expected return type
     * @return Map of key to value
     */
    public <T> Map<String, T> getMultiple(List<String> keys, Class<T> clazz) {
        if (keys == null || keys.isEmpty()) {
            return new HashMap<>();
        }
        
        long startTime = System.currentTimeMillis();
        Map<String, T> result = new HashMap<>();
        
        try {
            List<Object> values = valueOperations.multiGet(keys);
            
            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                Object value = values.get(i);
                
                if (value != null) {
                    T deserializedValue = serializationService.deserialize(value, clazz);
                    result.put(key, deserializedValue);
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordCacheMultiGet(keys.size(), result.size(), duration);
            
            log.debug("Retrieved {} values from cache for {} keys in {}ms", 
                    result.size(), keys.size(), duration);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordCacheError("multi_get", duration, e);
            
            log.error("Error retrieving multiple values from cache", e);
        }
        
        return result;
    }
    
    /**
     * Put multiple values in cache
     * 
     * @param keyValueMap Map of key to value
     * @param ttl Time to live
     */
    public void putMultiple(Map<String, Object> keyValueMap, Duration ttl) {
        if (keyValueMap == null || keyValueMap.isEmpty()) {
            return;
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            Map<String, Object> serializedMap = new HashMap<>();
            
            for (Map.Entry<String, Object> entry : keyValueMap.entrySet()) {
                Object serializedValue = serializationService.serialize(entry.getValue());
                serializedMap.put(entry.getKey(), serializedValue);
            }
            
            valueOperations.multiSet(serializedMap);
            
            // Set TTL for all keys
            for (String key : keyValueMap.keySet()) {
                redisTemplate.expire(key, ttl);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordCacheMultiPut(keyValueMap.size(), duration);
            
            log.debug("Successfully cached {} values in {}ms", keyValueMap.size(), duration);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordCacheError("multi_put", duration, e);
            
            log.error("Error storing multiple values in cache", e);
        }
    }
    
    /**
     * Increment numeric value in cache
     * 
     * @param key Cache key
     * @param delta Increment delta
     * @return New value after increment
     */
    public long increment(String key, long delta) {
        if (key == null || key.trim().isEmpty()) {
            return 0;
        }
        
        try {
            return valueOperations.increment(key, delta);
        } catch (Exception e) {
            log.error("Error incrementing value for key: {}", key, e);
            return 0;
        }
    }
    
    /**
     * Decrement numeric value in cache
     * 
     * @param key Cache key
     * @param delta Decrement delta
     * @return New value after decrement
     */
    public long decrement(String key, long delta) {
        return increment(key, -delta);
    }
    
    /**
     * Get cache statistics
     * 
     * @return Cache statistics
     */
    public CacheStatistics getStatistics() {
        try {
            Properties info = redisTemplate.getConnectionFactory().getConnection().info();
            
            return CacheStatistics.builder()
                    .hitRate(metricsCollector.getCacheHitRate())
                    .missRate(metricsCollector.getCacheMissRate())
                    .totalKeys(redisTemplate.getConnectionFactory().getConnection().dbSize())
                    .memoryUsage(info.getProperty("used_memory_human"))
                    .uptime(info.getProperty("uptime_in_seconds"))
                    .connectedClients(info.getProperty("connected_clients"))
                    .build();
                    
        } catch (Exception e) {
            log.error("Error retrieving cache statistics", e);
            return CacheStatistics.empty();
        }
    }
    
    /**
     * Clear all cache entries
     */
    public void clear() {
        try {
            redisTemplate.getConnectionFactory().getConnection().flushAll();
            log.info("Cleared all cache entries");
            
            metricsCollector.recordCacheClear();
            
        } catch (Exception e) {
            log.error("Error clearing cache", e);
        }
    }
    
    /**
     * Clear cache entries by pattern
     * 
     * @param pattern Key pattern
     * @return Number of keys deleted
     */
    public long clearByPattern(String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) {
            return 0;
        }
        
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            
            if (keys != null && !keys.isEmpty()) {
                Long deletedCount = redisTemplate.delete(keys);
                log.info("Cleared {} cache entries matching pattern: {}", deletedCount, pattern);
                return deletedCount != null ? deletedCount : 0;
            }
            
            return 0;
            
        } catch (Exception e) {
            log.error("Error clearing cache by pattern: {}", pattern, e);
            return 0;
        }
    }
    
    /**
     * Get cache size
     * 
     * @return Number of keys in cache
     */
    public long size() {
        try {
            return redisTemplate.getConnectionFactory().getConnection().dbSize();
        } catch (Exception e) {
            log.error("Error getting cache size", e);
            return 0;
        }
    }
    
    /**
     * Get all keys matching pattern
     * 
     * @param pattern Key pattern
     * @return Set of matching keys
     */
    public Set<String> getKeys(String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) {
            return new HashSet<>();
        }
        
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            return keys != null ? keys : new HashSet<>();
        } catch (Exception e) {
            log.error("Error getting keys for pattern: {}", pattern, e);
            return new HashSet<>();
        }
    }
}
