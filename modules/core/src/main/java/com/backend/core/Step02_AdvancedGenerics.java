package com.backend.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Step 02: Advanced Generics & PECE (Producer Extends, Consumer Super)
 * 
 * L5/L7 Principles:
 * 1. Type Safety: Catching errors at compile time.
 * 2. Variance: Understanding how generic types relate (Covariance vs Contravariance).
 * 3. Reusable Frameworks: Writing code that works across domain models.
 */
public class Step02_AdvancedGenerics {

    public interface CloudResource { String getId(); }
    public record GCEInstance(String id) implements CloudResource { @Override public String getId() { return id; } }
    public record GCSBucket(String id) implements CloudResource { @Override public String getId() { return id; } }

    /**
     * Producer Extends: Read resources safely.
     */
    public static void printResourceIds(List<? extends CloudResource> resources) {
        resources.forEach(r -> System.out.println("Resource ID: " + r.getId()));
    }

    /**
     * Consumer Super: Add resources safely.
     */
    public static void addGCEInstance(List<? super GCEInstance> resources, String id) {
        resources.add(new GCEInstance(id));
    }

    public static void main(String[] args) {
        System.out.println("=== Step 02: Advanced Generics (Cloud Context) ===");

        List<GCEInstance> instances = new ArrayList<>();
        addGCEInstance(instances, "instance-1");
        
        List<GCSBucket> buckets = List.of(new GCSBucket("bucket-a"));

        // Covariance in action
        printResourceIds(instances);
        printResourceIds(buckets);
    }
}
