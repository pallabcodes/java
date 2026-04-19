package com.backend.functional;

import java.util.function.Supplier;

/**
 * Step 05: Lazy Evaluation
 * 
 * L5 Principles:
 * 1. Efficiency: Defers computation until the result is actually requested.
 * 2. Idempotency: The heavy logic only runs once, no matter how many times 'get()' is called.
 * 3. Thread-safety: Standard double-checked locking for secure execution.
 */
public class Step05_LazyResource {

    public static class Lazy<T> implements Supplier<T> {
        private final Supplier<T> supplier;
        private volatile T value;

        public Lazy(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            T result = value;
            if (result == null) {
                synchronized (this) {
                    result = value;
                    if (result == null) {
                        result = value = supplier.get();
                    }
                }
            }
            return result;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Step 05: Lazy Evaluation ===");

        // Expensive asset loading (e.g. GMeet blurring background ML model)
        Lazy<String> backgroundBlurModel = new Lazy<>(() -> {
            System.out.println(">>> LOADING: Heavy ML Model for background blur (2s)...");
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            return "ML_MODEL_V4_BLUR_ACTIVE";
        });

        System.out.println("App started. Blur model is initialized but NOT loaded yet.");
        
        System.out.println("\nUser toggled blur on...");
        System.out.println("Blur Status: " + backgroundBlurModel.get());

        System.out.println("\nUser toggled blur off/on again...");
        System.out.println("Blur Status: " + backgroundBlurModel.get() + " (Instant, already loaded)");
    }
}
