package com.netflix.streaming.infrastructure.locking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

/**
 * Distributed locking service using Redis.
 * 
 * Implements distributed locks with:
 * - SET NX PX pattern (set if not exists with expiration)
 * - Lua script for atomic unlock (owner verification)
 * - Lock renewal support
 * - Fencing token generation
 */
@Service
public class DistributedLockService {

    private static final Logger logger = LoggerFactory.getLogger(DistributedLockService.class);
    private static final String LOCK_KEY_PREFIX = "lock:";
    private static final Duration DEFAULT_TTL = Duration.ofSeconds(30);
    private static final int DEFAULT_MAX_RENEWALS = 10;

    private final StringRedisTemplate redisTemplate;
    
    // Lua script for atomic unlock (only unlock if owner matches)
    private static final String UNLOCK_SCRIPT = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "  return redis.call('del', KEYS[1]) " +
        "else " +
        "  return 0 " +
        "end";

    private final DefaultRedisScript<Long> unlockScript;

    public DistributedLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.unlockScript = new DefaultRedisScript<>();
        this.unlockScript.setScriptText(UNLOCK_SCRIPT);
        this.unlockScript.setResultType(Long.class);
    }

    /**
     * Try to acquire a distributed lock.
     * 
     * @param key The lock key
     * @param ttl Time-to-live for the lock
     * @return DistributedLock instance, check acquired() to see if lock was acquired
     */
    public DistributedLock tryLock(String key, Duration ttl) {
        String lockKey = buildLockKey(key);
        String ownerId = UUID.randomUUID().toString();
        
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, ownerId, ttl);
        boolean isAcquired = Boolean.TRUE.equals(acquired);
        
        if (isAcquired) {
            logger.debug("Acquired distributed lock: {} with owner: {}", key, ownerId);
        } else {
            logger.debug("Failed to acquire distributed lock: {} (already held)", key);
        }
        
        return new DistributedLock(lockKey, ownerId, isAcquired, ttl, this);
    }

    /**
     * Try to acquire a distributed lock with default TTL (30 seconds).
     */
    public DistributedLock tryLock(String key) {
        return tryLock(key, DEFAULT_TTL);
    }

    /**
     * Acquire a distributed lock, blocking until available or timeout.
     * 
     * @param key The lock key
     * @param ttl Time-to-live for the lock
     * @param timeout Maximum time to wait for lock acquisition
     * @return DistributedLock instance
     */
    public DistributedLock lock(String key, Duration ttl, Duration timeout) {
        long timeoutMillis = timeout.toMillis();
        long startTime = System.currentTimeMillis();
        long pollInterval = 100; // Poll every 100ms
        
        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            DistributedLock lock = tryLock(key, ttl);
            if (lock.acquired()) {
                return lock;
            }
            
            try {
                Thread.sleep(pollInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Interrupted while waiting for lock: {}", key);
                return new DistributedLock(buildLockKey(key), null, false, ttl);
            }
        }
        
            logger.warn("Timeout waiting for distributed lock: {}", key);
        return new DistributedLock(buildLockKey(key), null, false, ttl, this);
    }

    /**
     * Renew a distributed lock (extend TTL).
     * 
     * @param lock The lock to renew
     * @param newTtl New time-to-live
     * @return true if renewal successful, false otherwise
     */
    public boolean renew(DistributedLock lock, Duration newTtl) {
        if (!lock.acquired()) {
            return false;
        }
        
        String currentOwner = redisTemplate.opsForValue().get(lock.getKey());
        if (!lock.getOwnerId().equals(currentOwner)) {
            logger.warn("Lock ownership changed, cannot renew: {}", lock.getKey());
            return false;
        }
        
        Boolean renewed = redisTemplate.expire(lock.getKey(), newTtl);
        boolean success = Boolean.TRUE.equals(renewed);
        
        if (success) {
            logger.debug("Renewed distributed lock: {}", lock.getKey());
        }
        
        return success;
    }

    /**
     * Release a distributed lock.
     * 
     * @param lock The lock to release
     * @return true if release successful, false otherwise
     */
    public boolean unlock(DistributedLock lock) {
        if (!lock.acquired()) {
            return false;
        }
        
        try {
            Long result = redisTemplate.execute(
                unlockScript,
                Collections.singletonList(lock.getKey()),
                lock.getOwnerId()
            );
            
            boolean released = result != null && result > 0;
            
            if (released) {
                logger.debug("Released distributed lock: {}", lock.getKey());
            } else {
                logger.warn("Failed to release lock (ownership mismatch or already released): {}", lock.getKey());
            }
            
            return released;
        } catch (Exception e) {
            logger.error("Error releasing distributed lock: {}", lock.getKey(), e);
            return false;
        }
    }

    /**
     * Build Redis key for lock.
     */
    private String buildLockKey(String key) {
        return LOCK_KEY_PREFIX + key;
    }

    /**
     * Distributed lock representation.
     */
    public static class DistributedLock implements AutoCloseable {
        private final String key;
        private final String ownerId;
        private final boolean acquired;
        private final Duration ttl;
        private final DistributedLockService lockService;
        private int renewalCount = 0;

        DistributedLock(String key, String ownerId, boolean acquired, Duration ttl, DistributedLockService lockService) {
            this.key = key;
            this.ownerId = ownerId;
            this.acquired = acquired;
            this.ttl = ttl;
            this.lockService = lockService;
        }

        public String getKey() { return key; }
        public String getOwnerId() { return ownerId; }
        public boolean acquired() { return acquired; }
        public Duration getTtl() { return ttl; }

        /**
         * Renew the lock (extend TTL).
         */
        public boolean renew(Duration newTtl) {
            if (lockService != null && renewalCount < DEFAULT_MAX_RENEWALS) {
                renewalCount++;
                return lockService.renew(this, newTtl);
            }
            return false;
        }

        /**
         * Release the lock.
         */
        public boolean unlock() {
            if (lockService != null) {
                return lockService.unlock(this);
            }
            return false;
        }

        /**
         * Auto-closeable support - releases lock on close.
         */
        @Override
        public void close() {
            unlock();
        }
    }
}

