package com.backend.lowlevel;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Step 08: Reference Types (Weak, Soft, Phantom)
 * 
 * L7 Principles:
 * 1. Memory Pressure: Understanding how to build caches that don't cause OOM.
 * 2. GC Interaction: Knowing when a reference will be reaped.
 * 3. Cache Design: SoftReferences are for memory-sensitive caches; WeakReferences are for mappings.
 */
public class Step08_ReferenceTypes {

    public static void main(String[] args) {
        System.out.println("=== Step 08: Reference Types (Memory Mastery) ===");

        // 1. WeakReference
        // Cleared aggressively as soon as the referent has no strong references.
        Object heavyObject = new Object();
        WeakReference<Object> weakRef = new WeakReference<>(heavyObject);
        System.out.println("WeakRef (Strong exists): " + weakRef.get());
        
        heavyObject = null; // Remove strong reference
        System.gc(); // Suggest GC
        System.out.println("WeakRef (After GC): " + weakRef.get() + " (Expect null)");

        // 2. SoftReference
        // Cleared only when the JVM is low on memory (Heap pressure).
        String cacheData = "Netflix-Playback-Metadata-Chunk-001";
        SoftReference<String> softRef = new SoftReference<>(cacheData);
        System.out.println("SoftRef: " + softRef.get());
        
        System.out.println("\nL5 Insight: L7 engineers use SoftReferences for playback caches to avoid OOM.");
    }
}
