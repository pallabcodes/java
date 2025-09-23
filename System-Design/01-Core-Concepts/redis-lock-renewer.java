package com.netflix.systemdesign.locking;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * RedisLockRenewer periodically refreshes TTL for held locks.
 */
public class RedisLockRenewer implements AutoCloseable {
    private final StringRedisTemplate redis;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Map<String, Duration> held = new ConcurrentHashMap<>();

    public RedisLockRenewer(StringRedisTemplate redis) {
        this.redis = redis;
        scheduler.scheduleAtFixedRate(this::tick, 500, 500, TimeUnit.MILLISECONDS);
    }

    public void track(String key, Duration ttl) {
        held.put(key, ttl);
    }

    public void untrack(String key) {
        held.remove(key);
    }

    private void tick() {
        held.forEach((k, ttl) -> {
            try { redis.expire(k, ttl); } catch (Exception ignored) {}
        });
    }

    @Override
    public void close() {
        scheduler.shutdownNow();
        held.clear();
    }
}


