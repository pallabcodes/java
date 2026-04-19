package com.backend.metaprogramming;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Step 04: Type Tokens (Beyond Erasure)
 * 
 * L7 Principles:
 * 1. Type Erasure: Java removes Generic types at runtime (e.g., List<String> becomes List).
 * 2. Gafter's Gadget: Subclassing an abstract generic class to 'freeze' the type information.
 * 3. Reflection on Generics: Using 'getGenericSuperclass()' to retrieve actual type arguments.
 */
public class Step04_TypeTokens {

    /**
     * L7 Mastery: The Super Type Token pattern.
     * By creating an anonymous class extends TypeReference<T>, T is preserved in bytecode.
     */
    public abstract static class TypeReference<T> {
        private final Type type;

        protected TypeReference() {
            Type superclass = getClass().getGenericSuperclass();
            if (superclass instanceof Class) {
                throw new RuntimeException("Missing type parameter.");
            }
            this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
        }

        public Type getType() { return type; }
    }

    public static void main(String[] args) {
        System.out.println("=== Step 04: Type Tokens (Preserving Generics) ===");

        // Preserving List<String>
        TypeReference<List<String>> token = new TypeReference<List<String>>() {};
        
        System.out.println("Runtime Generic Type: " + token.getType());
        
        System.out.println("\nL5 Insight: This is how Jackson (TypeReference) and Guice (TypeLiteral) work.");
        System.out.println("L7 Tip: Use this when building serializing/deserializing infrastructure.");
    }
}
