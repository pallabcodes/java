package com.backend.designpatterns.creational.factory;

import java.util.concurrent.ConcurrentHashMap; // A thread-safe map (dictionary). Multiple threads can safely read/write.
import java.util.function.Function;

/**
 * PRODUCTION-GRADE PROVIDER UTILITIES
 * Used final to prvent subclassing so can't be extended so no inheritance. This is a standalone/fixed utlity class
 * Provides lifecycle management patterns (Singletons, Prototypes).
 */
public final class Providers {

    private Providers() {} // Private constructor prevents instantiation and we did so because this class supposed to hold static helper methods, not objects.

    /**
     * Converts any provider into a thread-safe Singleton (Memoized).
     * This is a standard L5/Principal pattern for lifecycle management.
     * So, here we have used Generics where T = input, R = output and it returns a function of type Function<T, R>
     */
    public static <T, R> Function<T, R> memoize(Function<T, R> base) {
        // returning a function that creates an anonymous class so off course we can define properties and methods like below.
        return new Function<T, R>() {
            // A thread-safe map (dictionary). Multiple threads can safely read/write.
            private final ConcurrentHashMap<T, R> cache = new ConcurrentHashMap<>();

            @Override
            public R apply(T config) {
                // computeIfAbsent method ensures thread-safety and atomic initialization
                /**
                 * what below method does as follows:
                 * ---------------------------------
                 * check if the config within cache, if yes return cached value otherwise hypothetically
                 * result = base.apply(config) and store result in cache and return result
                 * 
                */
                return cache.computeIfAbsent(config, base);
            }
        };
    }
}

// memoize function: This is the "secret sauce." It takes a regular provider (constructor) and wraps it in a thread-safe cache (ConcurrentHashMap).
// This allows the system to decide if an object should be a Singleton (shared) or a Prototype (new every time) without changing the concrete class code.