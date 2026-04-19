package com.backend.metaprogramming;

import java.lang.reflect.Field;

/**
 * Step 01: Reflection Mastery (L7 Depth)
 * 
 * L7 Principles:
 * 1. Accessibility: Overriding Java access modifiers for introspection.
 * 2. String Interning: Understanding how interning impacts memory and equality.
 * 3. Security Manager: Why Reflection is a controlled/dangerous operation.
 */
public class Step01_ReflectionDeepDive {

    public static class SensitiveVault {
        @SuppressWarnings("unused")
        private String secretKey = "GOOGLE_L5_TOKEN";
    }

    public static void demonstrateFieldAccess() throws Exception {
        SensitiveVault vault = new SensitiveVault();
        System.out.println(">>> Reflection: Accessing Private Fields");

        // Accessing private field
        Field field = SensitiveVault.class.getDeclaredField("secretKey");
        field.setAccessible(true); // L7 Note: This is an expensive, security-sensitive call
        
        String key = (String) field.get(vault);
        System.out.println("Found Secret Key: " + key);
    }

    public static void demonstrateStringInterning() {
        System.out.println("\n>>> String Interning: Pool Dynamics");
        
        String s1 = "google"; // Created in the string pool
        String s2 = new String("google"); // Created on the heap
        String s3 = s2.intern(); // Reuses the pool instance

        System.out.println("s1 == s2: " + (s1 == s2) + " (Pool vs Heap)");
        System.out.println("s1 == s3: " + (s1 == s3) + " (Pool vs Interned)");
        
        System.out.println("\nL5 Insight: String interning saves memory but can cause 'intern() OOM' if misused.");
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Step 01: Reflection & Strings (L7 Fundamentals) ===");
        demonstrateFieldAccess();
        demonstrateStringInterning();
    }
}
