package com.backend.core.language;

import java.util.*;

/**
 * Step 12: Sequenced Collections (JEP 431)
 * 
 * L7 Mastery:
 * 1. Unified API: Finally, a common interface for collections with a defined order.
 * 2. First/Last Access: No more 'iterator().next()' or 'size() - 1' hacks.
 * 3. Reversed Views: Zero-copy views of the collection in reverse order.
 */
public class Step12_SequencedCollections {

    public static void demonstrateSequencedList() {
        System.out.println(">>> Sequenced List Operations");
        SequencedCollection<String> list = new ArrayList<>(List.of("Alpha", "Beta", "Gamma"));

        list.addFirst("Start");
        list.addLast("End");

        System.out.println("First element: " + list.getFirst());
        System.out.println("Last element: " + list.getLast());
        System.out.println("Reversed view: " + list.reversed());
    }

    public static void demonstrateSequencedMap() {
        System.out.println("\n>>> Sequenced Map Operations (LinkedHashMap)");
        SequencedMap<String, Integer> map = new LinkedHashMap<>();
        map.put("First", 1);
        map.put("Second", 2);
        map.put("Third", 3);

        System.out.println("Original: " + map);
        System.out.println("First Entry: " + map.firstEntry());
        System.out.println("Last Entry: " + map.lastEntry());

        // Polling (L7 Note: Useful for ordered task queues)
        Map.Entry<String, Integer> polled = map.pollFirstEntry();
        System.out.println("Polled First: " + polled);
        System.out.println("After Poll: " + map);
    }

    public static void main(String[] args) {
        System.out.println("=== Step 12: Sequenced Collections (Java 21) ===");
        demonstrateSequencedList();
        demonstrateSequencedMap();
        
        System.out.println("\nL5 Insight: This JEP added 'SequencedCollection', 'SequencedSet', and 'SequencedMap'.");
        System.out.println("L7 Depth: The '.reversed()' method returns a VIEW, not a copy. It's O(1) space.");
    }
}
