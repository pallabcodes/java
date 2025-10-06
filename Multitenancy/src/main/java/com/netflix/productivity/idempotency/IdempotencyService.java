package com.netflix.productivity.idempotency;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class IdempotencyService {

    private final StringRedisTemplate redis;

    public IdempotencyService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public boolean claim(String tenantId, String key, Duration ttl) {
        String namespaced = namespace(tenantId, key);
        Boolean ok = redis.opsForValue().setIfAbsent(namespaced, "1", ttl);
        return ok != null && ok;
    }

    private String namespace(String tenantId, String key) {
        return "idem:" + tenantId + ":" + key;
    }
}


