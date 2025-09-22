package com.netflix.systemdesign.caching;

import lombok.extern.slf4j.Slf4j;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.MemcachedClientIF;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Netflix Production-Grade Memcached Cache Implementation
 * 
 * This class demonstrates Netflix production standards for Memcached caching including:
 * 1. Connection pooling and optimization
 * 2. Binary protocol support
 * 3. Compression and serialization
 * 4. Performance monitoring
 * 5. Error handling and fallback
 * 6. Security and access control
 * 7. Memory optimization
 * 8. Cluster support
 * 
 * For C/C++ engineers:
 * - Memcached is like a high-performance distributed memory cache
 * - Cache operations are like hash table operations with network calls
 * - TTL is like automatic expiration timers
 * - Serialization is like converting objects to bytes
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@Slf4j
@Component
public class NetflixMemcachedCache {
    
    private final MemcachedClientIF memcachedClient;
    private final MetricsCollector metricsCollector;
    private final CacheConfiguration cacheConfiguration;
    private final SerializationService serializationService;
    private final CompressionService compressionService;
    
    /**
     * Constructor for Memcached cache
     * 
     * @param memcachedClient Memcached client
     * @param metricsCollector Metrics collection service
     * @param cacheConfiguration Cache configuration
     * @param serializationService Serialization service
     * @param compressionService Compression service
     */
    public NetflixMemcachedCache(MemcachedClientIF memcachedClient,
                               MetricsCollector metricsCollector,
                               CacheConfiguration cacheConfiguration,
                               SerializationService serializationService,
                               CompressionService compressionService) {
        this.memcachedClient = memcachedClient;
        this.metricsCollector = metricsCollector;
        this.cacheConfiguration = cacheConfiguration;
        this.serializationService = serializationService;
        this.compressionService = compressionService;
        
        log.info("Initialized Netflix Memcached cache with configuration: {}", cacheConfiguration);
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
            Object cachedValue = memcachedClient.get(key);
            
            if (cachedValue == null) {
                log.debug("Cache miss for key: {}", key);
                metricsCollector.recordCacheMiss(key);
                return null;
            }
            
            // Decompress if needed
            Object decompressedValue = decompressIfNeeded(cachedValue);
            
            // Deserialize
            T deserializedValue = serializationService.deserialize(decompressedValue, clazz);
            
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
     * @param ttl Time to live in seconds
     */
    public void put(String key, Object value, int ttl) {
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
            // Serialize value
            Object serializedValue = serializationService.serialize(value);
            
            // Compress if needed
            Object compressedValue = compressIfNeeded(serializedValue);
            
            // Store in cache
            Future<Boolean> future = memcachedClient.set(key, ttl, compressedValue);
            Boolean success = future.get(cacheConfiguration.getOperationTimeout(), TimeUnit.MILLISECONDS);
            
            if (Boolean.TRUE.equals(success)) {
                long duration = System.currentTimeMillis() - startTime;
                metricsCollector.recordCachePut(key, duration);
                
                log.debug("Successfully cached value for key: {} with TTL: {}s in {}ms", 
                        key, ttl, duration);
            } else {
                log.warn("Failed to cache value for key: {}", key);
            }
            
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
        put(key, value, (int) cacheConfiguration.getDefaultTtl().getSeconds());
    }
    
    /**
     * Put value in cache only if key doesn't exist
     * 
     * @param key Cache key
     * @param value Value to cache
     * @param ttl Time to live in seconds
     * @return true if value was set
     */
    public boolean putIfAbsent(String key, Object value, int ttl) {
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
            // Serialize value
            Object serializedValue = serializationService.serialize(value);
            
            // Compress if needed
            Object compressedValue = compressIfNeeded(serializedValue);
            
            // Store in cache only if not exists
            Future<Boolean> future = memcachedClient.add(key, ttl, compressedValue);
            Boolean success = future.get(cacheConfiguration.getOperationTimeout(), TimeUnit.MILLISECONDS);
            
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
            Future<Boolean> future = memcachedClient.delete(key);
            Boolean deleted = future.get(cacheConfiguration.getOperationTimeout(), TimeUnit.MILLISECONDS);
            
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
            Object value = memcachedClient.get(key);
            return value != null;
        } catch (Exception e) {
            log.error("Error checking key existence in cache for key: {}", key, e);
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
            Map<String, Object> values = memcachedClient.getBulk(keys);
            
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                if (value != null) {
                    // Decompress if needed
                    Object decompressedValue = decompressIfNeeded(value);
                    
                    // Deserialize
                    T deserializedValue = serializationService.deserialize(decompressedValue, clazz);
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
     * @param ttl Time to live in seconds
     */
    public void putMultiple(Map<String, Object> keyValueMap, int ttl) {
        if (keyValueMap == null || keyValueMap.isEmpty()) {
            return;
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            Map<String, Object> serializedMap = new HashMap<>();
            
            for (Map.Entry<String, Object> entry : keyValueMap.entrySet()) {
                // Serialize value
                Object serializedValue = serializationService.serialize(entry.getValue());
                
                // Compress if needed
                Object compressedValue = compressIfNeeded(serializedValue);
                
                serializedMap.put(entry.getKey(), compressedValue);
            }
            
            // Store all values
            for (Map.Entry<String, Object> entry : serializedMap.entrySet()) {
                memcachedClient.set(entry.getKey(), ttl, entry.getValue());
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
            Future<Long> future = memcachedClient.asyncIncr(key, delta);
            return future.get(cacheConfiguration.getOperationTimeout(), TimeUnit.MILLISECONDS);
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
            Map<net.spy.memcached.SocketAddress, Map<String, String>> stats = memcachedClient.getStats();
            
            long totalItems = 0;
            long totalBytes = 0;
            long totalConnections = 0;
            
            for (Map<String, String> serverStats : stats.values()) {
                totalItems += Long.parseLong(serverStats.getOrDefault("curr_items", "0"));
                totalBytes += Long.parseLong(serverStats.getOrDefault("bytes", "0"));
                totalConnections += Long.parseLong(serverStats.getOrDefault("curr_connections", "0"));
            }
            
            return CacheStatistics.builder()
                    .hitRate(metricsCollector.getCacheHitRate())
                    .missRate(metricsCollector.getCacheMissRate())
                    .totalKeys(totalItems)
                    .memoryUsage(String.valueOf(totalBytes))
                    .totalConnections(totalConnections)
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
            memcachedClient.flush();
            log.info("Cleared all cache entries");
            
            metricsCollector.recordCacheClear();
            
        } catch (Exception e) {
            log.error("Error clearing cache", e);
        }
    }
    
    /**
     * Compress value if needed
     * 
     * @param value Value to compress
     * @return Compressed value
     */
    private Object compressIfNeeded(Object value) {
        if (cacheConfiguration.isCompressionEnabled() && shouldCompress(value)) {
            return compressionService.compress(value);
        }
        return value;
    }
    
    /**
     * Decompress value if needed
     * 
     * @param value Value to decompress
     * @return Decompressed value
     */
    private Object decompressIfNeeded(Object value) {
        if (cacheConfiguration.isCompressionEnabled() && isCompressed(value)) {
            return compressionService.decompress(value);
        }
        return value;
    }
    
    /**
     * Check if value should be compressed
     * 
     * @param value Value to check
     * @return true if should be compressed
     */
    private boolean shouldCompress(Object value) {
        // Simple size-based compression decision
        if (value instanceof byte[]) {
            return ((byte[]) value).length > cacheConfiguration.getCompressionThreshold();
        }
        return false;
    }
    
    /**
     * Check if value is compressed
     * 
     * @param value Value to check
     * @return true if compressed
     */
    private boolean isCompressed(Object value) {
        // Simple check for compressed data
        return value instanceof byte[] && ((byte[]) value).length > 0;
    }
}
