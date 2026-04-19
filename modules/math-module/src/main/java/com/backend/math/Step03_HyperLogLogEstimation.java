package com.backend.math;

/**
 * Step 03: Probabilistic Counting (HyperLogLog)
 * 
 * L5 Principles:
 * 1. Space Efficiency: Estimating cardinality of billions of items with constant memory (KB).
 * 2. Probabilistic Trade-offs: Sacrificing ~1% accuracy for 99.9% memory savings.
 * 3. Hashing for Uniqueness: Using leading zeros of hash as a stochastic indicator.
 */
public class Step03_HyperLogLogEstimation {

    public static void main(String[] args) {
        System.out.println("=== Step 03: HyperLogLog Concept (Search Math) ===");

        // Simplified HLL simulation
        // The core idea: Hash a string, find the number of leading zeros.
        // Max leading zeros observed across many items correlates to log2(UniqueItems).
        
        String[] searchQueries = {
            "google search", "gmail", "gmaps", "google search", "gmail", "gmeet", "gcp"
        };
        
        System.out.println("Input (with duplicates): " + String.join(", ", searchQueries));
        
        // In a real HLL, we use 2^k registers. This is a 1-register toy example for demo.
        int maxLeadingZeros = 0;
        for (String query : searchQueries) {
            int hash = query.hashCode();
            int leadingZeros = Integer.numberOfLeadingZeros(Math.abs(hash));
            maxLeadingZeros = Math.max(maxLeadingZeros, leadingZeros);
        }

        // Estimate = 2 ^ maxLeadingZeros
        double estimate = Math.pow(2, maxLeadingZeros);
        
        System.out.println("Max Leading Zeros observed: " + maxLeadingZeros);
        System.out.println("Rough estimated cardinality: ~" + (int)estimate);
        System.out.println("L5 Note: Real HLL uses many registers and harmonic means for ~1-2% error.");
    }
}
