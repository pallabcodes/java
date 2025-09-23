package com.netflix.systemdesign.locking;

import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * FencingTokenService: monotonically increasing token per resource.
 * Use token at write time to reject stale owners downstream.
 */
public class FencingTokenService {
    private final StringRedisTemplate redis;

    public FencingTokenService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public long nextToken(String resourceKey) {
        Long v = redis.opsForValue().increment("fencing:" + resourceKey);
        return v == null ? 0L : v;
    }
}


