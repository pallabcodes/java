package com.netflix.streaming.infrastructure.locking;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Aspect for distributed locking using @DistributedLock annotation.
 * 
 * Provides declarative distributed locking for methods.
 */
@Aspect
@Component
public class LockAspect {

    private static final Logger logger = LoggerFactory.getLogger(LockAspect.class);

    private final DistributedLockService lockService;

    public LockAspect(DistributedLockService lockService) {
        this.lockService = lockService;
    }

    @Around("@annotation(distributedLock)")
    public Object executeWithLock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String lockKey = distributedLock.key();
        Duration ttl = Duration.ofSeconds(distributedLock.ttlSeconds());
        Duration timeout = Duration.ofSeconds(distributedLock.timeoutSeconds());

        // Try to acquire lock
        DistributedLockService.DistributedLock lock = lockService.tryLock(lockKey, ttl);
        
        if (!lock.acquired() && distributedLock.waitForLock()) {
            // Wait for lock if configured
            lock = lockService.lock(lockKey, ttl, timeout);
        }

        if (!lock.acquired()) {
            throw new LockAcquisitionException("Failed to acquire distributed lock: " + lockKey);
        }

        try (DistributedLockService.DistributedLock autoCloseableLock = lock) {
            // Execute the method
            return joinPoint.proceed();
        }
    }

    /**
     * Exception thrown when lock acquisition fails.
     */
    public static class LockAcquisitionException extends RuntimeException {
        public LockAcquisitionException(String message) {
            super(message);
        }
    }
}

