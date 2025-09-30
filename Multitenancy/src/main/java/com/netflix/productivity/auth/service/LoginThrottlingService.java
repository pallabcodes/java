package com.netflix.productivity.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginThrottlingService {

    private final boolean enabled;
    private final int maxAttempts;
    private final long windowSeconds;
    private final long blockSeconds;

    private final Map<String, Window> attempts = new ConcurrentHashMap<>();

    public LoginThrottlingService(
            @Value("${app.login.throttling.enabled:true}") boolean enabled,
            @Value("${app.login.throttling.max-attempts:5}") int maxAttempts,
            @Value("${app.login.throttling.window-seconds:300}") long windowSeconds,
            @Value("${app.login.throttling.block-seconds:900}") long blockSeconds) {
        this.enabled = enabled;
        this.maxAttempts = maxAttempts;
        this.windowSeconds = windowSeconds;
        this.blockSeconds = blockSeconds;
    }

    public boolean isBlocked(String key) {
        if (!enabled) return false;
        Window w = attempts.get(key);
        if (w == null) return false;
        long now = Instant.now().getEpochSecond();
        if (w.blockUntil > now) return true;
        if (now - w.start >= windowSeconds) {
            attempts.remove(key);
            return false;
        }
        return false;
    }

    public void recordFailure(String key) {
        if (!enabled) return;
        long now = Instant.now().getEpochSecond();
        Window w = attempts.computeIfAbsent(key, k -> new Window(now, 0, 0));
        if (now - w.start >= windowSeconds) {
            w.start = now;
            w.count = 0;
            w.blockUntil = 0;
        }
        w.count++;
        if (w.count >= maxAttempts) {
            w.blockUntil = now + blockSeconds;
        }
    }

    public void recordSuccess(String key) {
        attempts.remove(key);
    }

    private static class Window {
        long start;
        int count;
        long blockUntil;

        Window(long start, int count, long blockUntil) {
            this.start = start;
            this.count = count;
            this.blockUntil = blockUntil;
        }
    }
}

