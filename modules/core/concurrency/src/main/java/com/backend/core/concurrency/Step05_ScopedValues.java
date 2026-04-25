package com.backend.core.concurrency;

/**
 * Step 05: Scoped Values (L7 Mastery)
 * 
 * CONCEPT:
 * ThreadLocal is problematic for millions of Virtual Threads because it's mutable 
 * and often leads to memory leaks if not explicitly removed.
 * 
 * L7 AWARENESS:
 * 1. IMMUTABILITY: ScopedValues are immutable. Once bound, they cannot be changed in that scope.
 * 2. PERFORMANCE: ScopedValues are extremely lightweight. Data is stored once and shared 
 *    across child threads created within the scope.
 */
public class Step05_ScopedValues {

    // L7 Standard: Replacing ThreadLocal with ScopedValue
    public static final ScopedValue<String> CONTEXT = ScopedValue.newInstance();

    public static void processRequest(String requestId) {
        System.out.println("--- Demonstrating Scoped Values (Lightweight Context) ---");

        // Binding the value to a scope
        ScopedValue.where(CONTEXT, requestId).run(() -> {
            System.out.println("[Scope Start] Request ID: " + CONTEXT.get());
            
            // Calling a deep service method without passing parameters
            deepServiceCall();
            
            // Values are automatically unbound after the run() block completes
        });
        
        if (!CONTEXT.isBound()) {
            System.out.println("[Scope End] Context is now unbound (Safe from leaks).");
        }
    }

    private static void deepServiceCall() {
        // Accessing context value deep in the call stack
        System.out.println("[Deep Call] Processing for: " + CONTEXT.get());
    }

    public static void main(String[] args) {
        processRequest("REQ-12345-L7");
    }
}
