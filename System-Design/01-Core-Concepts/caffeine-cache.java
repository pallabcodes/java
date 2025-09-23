package com.netflix.systemdesign.caching;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Local in-process cache with Caffeine.
 */
public class CaffeineLocalCache<K, V> {
    private final AsyncCache<K, V> cache;
    private final Executor executor;

    public CaffeineLocalCache(Duration expireAfterWrite, long maxSize, Executor executor) {
        this.executor = Objects.requireNonNull(executor);
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(expireAfterWrite)
                .maximumSize(maxSize)
                .recordStats()
                .buildAsync();
    }

    public CompletableFuture<V> get(K key, java.util.function.Function<K, V> loader) {
        return cache.get(key, k -> CompletableFuture.supplyAsync(() -> loader.apply(k), executor));
    }

    public void put(K key, V value) { cache.put(key, CompletableFuture.completedFuture(value)); }
    public void invalidate(K key) { cache.synchronous().invalidate(key); }
    public void invalidateAll() { cache.synchronous().invalidateAll(); }
}


