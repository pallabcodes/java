package com.backend.math;

import java.util.BitSet;

/**
 * Step 04: Space-Efficient Indexing (Bloom Filter)
 * 
 * L5 Principles:
 * 1. Membership Testing: Probabilistic "is it in the set?" check.
 * 2. False Positive (Yes): It might be in the set (needs further checking).
 * 3. False Negative (No): It definitely is NOT in the set (skips further work).
 * 4. Cache Shaving: Saving secondary lookups (Bigtable/Spanner) for absent keys.
 */
public class Step04_BloomFilter {

    private final BitSet bits;
    private final int size;

    public Step04_BloomFilter(int size) {
        this.size = size;
        this.bits = new BitSet(size);
    }

    public void add(String item) {
        bits.set(hash1(item));
        bits.set(hash2(item));
    }

    public boolean mightContain(String item) {
        return bits.get(hash1(item)) && bits.get(hash2(item));
    }

    private int hash1(String item) { return Math.abs(item.hashCode()) % size; }
    private int hash2(String item) { return Math.abs(item.hashCode() * 31) % size; }

    public static void main(String[] args) {
        System.out.println("=== Step 04: Bloom Filter (Indexing Math) ===");

        Step04_BloomFilter index = new Step04_BloomFilter(1024);
        index.add("google.com/search?q=l5-java");
        index.add("google.com/search?q=bloom-filters");

        System.out.println("Contains 'l5-java'? " + index.mightContain("google.com/search?q=l5-java"));
        System.out.println("Contains 'unknown'? " + index.mightContain("google.com/search?q=unknown"));

        System.out.println("\nL5 Tip: Bloom filters are essential in Bigtable to avoid disk I/O for missing keys.");
    }
}
