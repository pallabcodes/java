package com.netflix.productivity.config;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class DynamicConfigService {

    private final StringRedisTemplate redis;

    public DynamicConfigService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public int getTenantRateLimitPerMinute(String tenantId) {
        String key = "cfg:ratelimit:" + (tenantId == null ? "default" : tenantId);
        String val = redis.opsForValue().get(key);
        if (val == null) return 200;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return 200;
        }
    }

    public void setTenantRateLimitPerMinute(String tenantId, int value, Duration ttl) {
        String key = "cfg:ratelimit:" + (tenantId == null ? "default" : tenantId);
        redis.opsForValue().set(key, Integer.toString(value), ttl);
    }
}


