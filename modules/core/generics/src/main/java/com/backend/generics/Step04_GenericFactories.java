package com.backend.generics;

/**
 * Step 04: Generic Factories (Type Inference Mastery)
 * 
 * L7 Principles:
 * 1. Type Inference: Removing redundant type declarations in client code.
 * 2. Singleton Factory Pattern: Reusing a single instance of a generic class for all types (e.g., Collections.emptyList()).
 * 3. Covariant Returns: Using wildcards in return types for maximum flexibility.
 */
public class Step04_GenericFactories {

    public interface Identity<T> { T identify(T input); }

    // Mastery: A singleton factory that works for ANY type T
    private static final Identity<Object> IDENTITY_INSTANCE = input -> input;

    @SuppressWarnings("unchecked")
    public static <T> Identity<T> getIdentity() {
        return (Identity<T>) IDENTITY_INSTANCE;
    }

    public static void main(String[] args) {
        System.out.println("=== Step 04: Generic Factories (Inference Demo) ===");

        // Mastery: No need to explicitly say <String> in the call; Java infers it from the assignment.
        Identity<String> stringId = getIdentity();
        Identity<Integer> intId = getIdentity();

        System.out.println("String Result: " + stringId.identify("L7-Engineer"));
        System.out.println("Integer Result: " + intId.identify(42));
        
        System.out.println("\nsameInstance (String vs Integer): " + ((Object)stringId == (Object)intId));

        System.out.println("\nL5 Insight: Type inference makes APIs feel 'sovereign' and clean.");
        System.out.println("L7 Tip: Use singleton factories for immutable generic behaviors to save heap space.");
    }
}
