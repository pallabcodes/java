package com.netflix.productivity.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class PermissionCache {

    private final Cache<String, Boolean> decisionCache;

    public PermissionCache(
            @Value("${app.permission-cache.ttl-seconds:300}") long ttlSeconds) {
        this.decisionCache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(ttlSeconds))
                .maximumSize(100_000)
                .build();
    }

    public void put(String key, boolean allowed) {
        decisionCache.put(key, allowed);
    }

    public Boolean get(String key) {
        return decisionCache.getIfPresent(key);
    }

    public void invalidateByPrefix(String prefix) {
        decisionCache.asMap().keySet().removeIf(k -> k.startsWith(prefix));
    }
}

