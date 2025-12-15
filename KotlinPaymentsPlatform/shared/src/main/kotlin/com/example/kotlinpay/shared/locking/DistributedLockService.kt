package com.example.kotlinpay.shared.locking

import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.UUID

/**
 * Distributed locking service using Redis.
 */
@Service
class DistributedLockService(
    private val redisTemplate: StringRedisTemplate
) {
    private val logger = LoggerFactory.getLogger(DistributedLockService::class.java)
    
    companion object {
        private const val LOCK_KEY_PREFIX = "lock:"
        private val DEFAULT_TTL = Duration.ofSeconds(30)
        private const val DEFAULT_MAX_RENEWALS = 10
        private const val UNLOCK_SCRIPT = """
            if redis.call('get', KEYS[1]) == ARGV[1] then 
              return redis.call('del', KEYS[1]) 
            else 
              return 0 
            end
        """
    }

    private val unlockScript: DefaultRedisScript<Long> = DefaultRedisScript<Long>().apply {
        scriptText = UNLOCK_SCRIPT
        resultType = Long::class.java
    }

    /**
     * Try to acquire a distributed lock.
     */
    fun tryLock(key: String, ttl: Duration = DEFAULT_TTL): DistributedLock {
        val lockKey = buildLockKey(key)
        val ownerId = UUID.randomUUID().toString()
        
        val acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, ownerId, ttl) == true
        
        if (acquired) {
            logger.debug("Acquired distributed lock: {} with owner: {}", key, ownerId)
        } else {
            logger.debug("Failed to acquire distributed lock: {} (already held)", key)
        }
        
        return DistributedLock(lockKey, ownerId, acquired, ttl, this)
    }

    /**
     * Renew a distributed lock.
     */
    fun renew(lock: DistributedLock, newTtl: Duration): Boolean {
        if (!lock.acquired) {
            return false
        }
        
        val currentOwner = redisTemplate.opsForValue().get(lock.key)
        if (lock.ownerId != currentOwner) {
            logger.warn("Lock ownership changed, cannot renew: {}", lock.key)
            return false
        }
        
        val renewed = redisTemplate.expire(lock.key, newTtl) == true
        
        if (renewed) {
            logger.debug("Renewed distributed lock: {}", lock.key)
        }
        
        return renewed
    }

    /**
     * Release a distributed lock.
     */
    fun unlock(lock: DistributedLock): Boolean {
        if (!lock.acquired) {
            return false
        }
        
        return try {
            val result = redisTemplate.execute(
                unlockScript,
                listOf(lock.key),
                lock.ownerId
            )
            
            val released = result != null && result > 0
            
            if (released) {
                logger.debug("Released distributed lock: {}", lock.key)
            } else {
                logger.warn("Failed to release lock (ownership mismatch or already released): {}", lock.key)
            }
            
            released
        } catch (e: Exception) {
            logger.error("Error releasing distributed lock: {}", lock.key, e)
            false
        }
    }

    private fun buildLockKey(key: String): String {
        return "$LOCK_KEY_PREFIX$key"
    }

    /**
     * Distributed lock representation.
     */
    class DistributedLock(
        val key: String,
        val ownerId: String,
        val acquired: Boolean,
        val ttl: Duration,
        private val lockService: DistributedLockService? = null
    ) : AutoCloseable {
        private var renewalCount = 0

        fun renew(newTtl: Duration): Boolean {
            if (lockService != null && renewalCount < DEFAULT_MAX_RENEWALS) {
                renewalCount++
                return lockService.renew(this, newTtl)
            }
            return false
        }

        fun unlock(): Boolean {
            return lockService?.unlock(this) ?: false
        }

        override fun close() {
            unlock()
        }
    }
}

