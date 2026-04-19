package com.backend.core;

// ScopedValue is in java.lang, no import needed or use import java.lang.ScopedValue;
// However, since it is a preview feature, it might be in different packages depending on build.
// In JDK 21 it is in java.lang.

/**
 * Step 09: Scoped Values (JEP 446)
 * 
 * L7 Principles:
 * 1. Immutability: Unlike ThreadLocal, ScopedValues are immutable.
 * 2. Performance: Efficiently shared across millions of virtual threads.
 * 3. Lifetime: Context is automatically cleared after the scope closes.
 */
public class Step09_ScopedValues {

    // Define a ScopedValue for the current transaction ID
    private static final ScopedValue<String> TRANSACTION_ID = ScopedValue.newInstance();

    public static void main(String[] args) {
        System.out.println("=== Step 09: Scoped Values (Context Propagation) ===");

        // Binding a value to a scope
        ScopedValue.where(TRANSACTION_ID, "TX-9999").run(() -> {
            System.out.println("Entering high-level service call...");
            deepInternalService();
        });

        System.out.println("Outside scope: " + (TRANSACTION_ID.isBound() ? TRANSACTION_ID.get() : "UNBOUND"));
    }

    private static void deepInternalService() {
        System.out.println("Reading transaction in deep internal service: " + TRANSACTION_ID.get());
        
        // At L7, we avoid 'ThreadLocal.set()' which is prone to memory leaks.
        // ScopedValue handles this safely at a system level.
    }
}
