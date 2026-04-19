package com.backend.metaprogramming;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Step 03: Method Handles (High-Performance Meta)
 * 
 * L7 Principles:
 * 1. Type Safety: More safe and strictly typed than Reflection's Object[].
 * 2. Performance: MethodHandles are optimized by JIT; close to direct invocation speed.
 * 3. Lookups: Direct vs Bindings to instances.
 */
public class Step03_MethodHandles {

    public static class ComputeEngine {
        public int square(int x) { return x * x; }
    }

    public static void main(String[] args) throws Throwable {
        System.out.println("=== Step 03: Method Handles (Modern Meta API) ===");

        ComputeEngine engine = new ComputeEngine();
        
        // 1. Defining the 'signature' we want to find: returns int, takes int
        MethodType type = MethodType.methodType(int.class, int.class);
        
        // 2. Finding the handle
        MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        MethodHandle handle = lookup.findVirtual(ComputeEngine.class, "square", type);
        
        // 3. High-speed invocation
        int result = (int) handle.invokeExact(engine, 10);
        
        System.out.println("10 squared via MethodHandle: " + result);
        
        System.out.println("\nL5 Insight: MethodHandles allow the JVM to optimize the call via inlining.");
        System.out.println("L7 Tip: Use MethodHandles for high-frequency dynamic calls in your infrastructure.");
    }
}
