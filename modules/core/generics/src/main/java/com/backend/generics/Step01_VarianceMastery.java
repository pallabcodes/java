package com.backend.generics;

import java.util.ArrayList;
import java.util.List;

/**
 * Step 01: Variance Mastery (PECS Principle)
 * 
 * L7 Principles:
 * 1. Covariance (extends): 'Producer Extends' - Used for reading from a collection.
 * 2. Contravariance (super): 'Consumer Super' - Used for writing into a collection.
 * 3. Invariance: Standard List<T> is invariant; you cannot assign List<Integer> to List<Number>.
 */
public class Step01_VarianceMastery {

    interface Media { String getName(); }
    static class Task implements Media { 
        private final String name;
        public Task(String name) { this.name = name; }
        @Override public String getName() { return name; }
    }

    /**
     * L7 Mastery: A pipeline that handles variance.
     * @param source Producer (provides media)
     * @param sink Consumer (accepts media)
     */
    public static void processMedia(List<? extends Media> source, List<? super Media> sink) {
        for (Media media : source) {
            System.out.println("Processing: " + media.getName());
            sink.add(media); // Valid because sink is 'super Media'
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Step 01: Variance Mastery (PECS) ===");

        List<Task> sourceTasks = List.of(new Task("L7-Deployment"), new Task("Core-Refactor"));
        List<Object> sinkResults = new ArrayList<>();

        // Covariance allows us to pass List<Task> to List<? extends Media>
        // Contravariance allows us to pass List<Object> to List<? super Media>
        processMedia(sourceTasks, sinkResults);

        System.out.println("Sink Size: " + sinkResults.size());
        System.out.println("\nL5 Insight: List<? extends T> is read-only; List<? super T> is write-only.");
    }
}
