package com.netflix.productivity.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RevokedTokenStore {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "revoked:jti:";

    public void revokeJti(String jti) {
        redisTemplate.opsForValue().set(KEY_PREFIX + jti, "1", Duration.ofHours(12));
    }

    public boolean isRevoked(String jti) {
        Boolean exists = redisTemplate.hasKey(KEY_PREFIX + jti);
        return exists != null && exists;
    }
}


