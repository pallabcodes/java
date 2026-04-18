package com.backend.designpatterns.creational.factory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * PRODUCTION-GRADE PROVIDER UTILITIES
 *
 * Provides lifecycle management patterns (Singletons, Prototypes).
 */
public final class Providers {

    private Providers() {} // Static utility class

    /**
     * Converts any provider into a thread-safe Singleton (Memoized).
     * This is a standard L5/Principal pattern for lifecycle management.
     */
    public static <T, R> Function<T, R> memoize(Function<T, R> base) {
        return new Function<T, R>() {
            private final ConcurrentHashMap<T, R> cache = new ConcurrentHashMap<>();

            @Override
            public R apply(T config) {
                // computeIfAbsent ensures thread-safety and atomic initialization
                return cache.computeIfAbsent(config, base);
            }
        };
    }
}
