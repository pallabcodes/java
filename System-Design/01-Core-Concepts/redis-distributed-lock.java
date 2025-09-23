package com.netflix.systemdesign.locking;

import org.springframework.data.redis.core.StringRedisTemplate;
import java.time.Duration;
import java.util.UUID;

/**
 * RedisDistributedLock: SET NX PX with Lua unlock and renewal.
 */
public class RedisDistributedLock implements AutoCloseable {
    private static final String UNLOCK_LUA = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    private final StringRedisTemplate redis;
    private final String key;
    private final String ownerId;
    private final Duration ttl;
    private boolean acquired;

    public RedisDistributedLock(StringRedisTemplate redis, String key, Duration ttl) {
        this.redis = redis;
        this.key = key;
        this.ttl = ttl;
        this.ownerId = UUID.randomUUID().toString();
    }

    public static RedisDistributedLock tryLock(StringRedisTemplate redis, String key, Duration ttl) {
        RedisDistributedLock lock = new RedisDistributedLock(redis, key, ttl);
        Boolean ok = redis.opsForValue().setIfAbsent(key, lock.ownerId, ttl);
        lock.acquired = Boolean.TRUE.equals(ok);
        return lock;
    }

    public boolean acquired() { return acquired; }

    public boolean renew(Duration newTtl) {
        if (!acquired) return false;
        String current = redis.opsForValue().get(key);
        if (!ownerId.equals(current)) return false;
        redis.expire(key, newTtl);
        return true;
    }

    @Override
    public void close() {
        if (!acquired) return;
        try {
            redis.execute((connection) -> connection.scriptingCommands().eval(
                    UNLOCK_LUA.getBytes(), ReturnType.INTEGER, 1,
                    key.getBytes(), ownerId.getBytes()
            ), false);
        } catch (Exception ignored) {
        }
    }
}


