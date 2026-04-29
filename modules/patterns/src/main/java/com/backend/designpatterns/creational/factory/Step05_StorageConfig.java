package com.backend.designpatterns.creational.factory;

import java.util.Map;

/**
 * Step 5: PRODUCT CONFIGURATION (Immutable)
 * 
 * This class holds all the "ingredients" needed to create a storage object.
 * Think of it as a "Blueprint" or a "Settings Snapshot."
 * 
 * 🧠 WHY USE A RECORD?
 * 1. IMMUTABILITY: Once created, it cannot be changed. This ensures "Data Integrity"
 *    and makes it 100% thread-safe.
 * 2. NO BOILERPLATE: Java automatically generates getters, equals(), hashCode(), 
 *    and toString().
 * 3. VALUE SEMANTICS: Two different config objects with the same values are 
 *    considered EQUAL.
 * 
 * 🔥 THE HIDDEN SUPERPOWER (For Caching):
 * Because Records have built-in equals/hashCode, they are PERFECT for use as keys 
 * in a Map. This allows our Step07_Providers.memoize() to work perfectly:
 * - Two identical configs → will point to the same cached instance.
 */
public record Step05_StorageConfig(
    String endpoint,
    String region,
    String bucketName,
    Map<String, String> credentials,
    boolean useEncryption
) {
    /**
     * CONVENIENCE FACTORY METHOD (Static Factory)
     * 
     * Provides a shortcut for creating a standard config with default values.
     * This prevents the client from having to specify all 5+ parameters 
     * if they just want a standard setup.
     */
    public static Step05_StorageConfig of(String endpoint, String region) {
        return new Step05_StorageConfig(
            endpoint, 
            region, 
            "default-bucket", 
            Map.of(), 
            true
        );
    }
}

// Using a record/config object instead of raw strings in the factory prevents:
// 1. "Parameter Long Lists" (passing 10 strings to a constructor is messy).
// 2. "Primitive Obsession" (using raw Strings for everything instead of typed objects).
// 3. "Configuration Drift" (grouping data ensures the whole config travels as one unit).
//
// 💡 THE "SHALLOW" IMMUTABILITY TRAP:
// Even if a class is 'final' and its fields are 'private final', you can still 
// modify the CONTENTS of the fields if they are mutable (like a List or Map).
//
// EXAMPLE (Traditional Class):
// final class MyClass {
//    private final List<String> data = new ArrayList<>();
//    public List<String> getData() { return data; }
// }
// MyClass obj = new MyClass();
// obj.getData().add("MODIFIED"); // ❌ The reference is final, but the list changed!
//
// RECORDS focus on "Value Semantics". While they don't automatically freeze 
// a Map, they strongly encourage the use of immutable collections (like Map.of())
// so that the entire object remains DEEPLY immutable and safe.
//
// 🏠 REAL-WORLD ANALOGY:
// Instead of giving a builder 20 separate verbal instructions, you give them a 
// single "Blueprint" (this config record). It's much harder to make a mistake!