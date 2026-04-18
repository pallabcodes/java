package com.backend.designpatterns.creational.factory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Step 7: PRODUCTION-GRADE PROVIDER UTILITIES (LIFECYCLE)
 */
public final class Step07_Providers {

    private Step07_Providers() {}

    /**
     * Converts any provider into a thread-safe Singleton (Memoized).
     */
    public static <T, R> Function<T, R> memoize(Function<T, R> base) {
        return new Function<T, R>() {
            private final ConcurrentHashMap<T, R> cache = new ConcurrentHashMap<>();

            @Override
            public R apply(T config) {
                return cache.computeIfAbsent(config, base);
            }
        };
    }
}

// memoize function: This is the "secret sauce." It takes a regular provider (constructor) and wraps it in a thread-safe cache (ConcurrentHashMap).
// This allows the system to decide if an object should be a Singleton (shared) or a Prototype (new every time) without changing the concrete class code.