package com.netflix.streaming.infrastructure.locking;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for distributed locking on methods.
 * 
 * Ensures only one instance processes the method at a time across the cluster.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    /**
     * The lock key. Can use SpEL expressions like "#aggregateId".
     */
    String key();

    /**
     * Time-to-live for the lock in seconds.
     */
    int ttlSeconds() default 30;

    /**
     * Timeout for lock acquisition in seconds (if waitForLock is true).
     */
    int timeoutSeconds() default 10;

    /**
     * Whether to wait for lock if not immediately available.
     */
    boolean waitForLock() default false;
}

