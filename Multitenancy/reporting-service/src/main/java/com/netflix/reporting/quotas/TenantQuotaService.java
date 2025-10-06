package com.netflix.reporting.quotas;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TenantQuotaService {

    private static class Bucket {
        double tokens;
        long lastRefillEpochMs;
        double capacity;
        double refillPerSecond;
    }

    private final Map<String, Bucket> tenantBuckets = new ConcurrentHashMap<>();
    private final Environment env;

    public TenantQuotaService(Environment env) {
        this.env = env;
    }

    public boolean tryConsume(String tenantId, double costUnits) {
        Bucket b = tenantBuckets.computeIfAbsent(tenantId, this::createBucket);
        refill(b);
        if (b.tokens >= costUnits) {
            b.tokens -= costUnits;
            return true;
        }
        return false;
    }

    private Bucket createBucket(String tenantId) {
        Bucket b = new Bucket();
        double capacity = getDouble("tenant." + tenantId + ".quota.capacity", 100.0);
        double rps = getDouble("tenant." + tenantId + ".quota.refill_per_second", 10.0);
        b.capacity = capacity;
        b.refillPerSecond = rps;
        b.tokens = capacity;
        b.lastRefillEpochMs = Instant.now().toEpochMilli();
        return b;
    }

    private void refill(Bucket b) {
        long now = Instant.now().toEpochMilli();
        long deltaMs = now - b.lastRefillEpochMs;
        if (deltaMs <= 0) return;
        double toAdd = (deltaMs / 1000.0) * b.refillPerSecond;
        b.tokens = Math.min(b.capacity, b.tokens + toAdd);
        b.lastRefillEpochMs = now;
    }

    private double getDouble(String key, double def) {
        String v = env.getProperty(key);
        if (v == null) return def;
        try { return Double.parseDouble(v); } catch (Exception e) { return def; }
    }
}


