package com.backend.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Step 10: HashMap Mechanics (L7 Systems Thinking)
 * 
 * L7 Principles:
 * 1. Bucket Distribution: Why bad hashCode() turns O(1) into O(n).
 * 2. Treeification: Java 8 optimized O(n) into O(log n) using Red-Black trees for collisions.
 * 3. Resize Cost: Why map capacity should be predefined to avoid rehashing overhead.
 * 4. Load Factor: The 0.75 trade-off between memory and speed.
 */
public class Step10_HashMapMechanics {

    static class BadHashKey {
        private final String id;
        public BadHashKey(String id) { this.id = id; }
        @Override public int hashCode() { return 42; } // Forced collision
        @Override public boolean equals(Object o) { 
            return o instanceof BadHashKey && id.equals(((BadHashKey)o).id); 
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Step 10: HashMap Mechanics (Collision Handling) ===");

        Map<BadHashKey, String> map = new HashMap<>();
        
        // Simulating high-collision bucket
        for (int i = 0; i < 10; i++) {
            map.put(new BadHashKey("key-" + i), "value-" + i);
        }

        System.out.println("Map size: " + map.size());
        System.out.println("L5 Insight: All entries are in bucket 42.");
        System.out.println("L7 Mastery: Since size > 8, this bucket has 'treeified' into a Red-Black tree.");
        
        // Initial Capacity planning
        Map<String, String> plannedMap = new HashMap<>(1024, 0.75f);
        System.out.println("L7 Tip: Pre-sizing prevents expensive data reshuffling during high-throughput ingestion.");
    }
}
