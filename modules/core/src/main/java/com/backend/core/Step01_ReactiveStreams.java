package com.backend.core;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Step 01: Advanced & Reactive Streams
 * 
 * L5/L7 Principles:
 * 1. Declarative Processing: Moving from "how" to "what".
 * 2. Laziness: Performance benefit of only processing what is needed.
 * 3. Parallel Streams: Scalability with ForkJoinPool awareness.
 */
public class Step01_ReactiveStreams {

    public record StreamEvent(String id, String type, long priority) {}

    public static void main(String[] args) {
        System.out.println("=== Step 01: Advanced Streams (Google Context) ===");

        List<StreamEvent> events = List.of(
            new StreamEvent("ev1", "LOG", 1),
            new StreamEvent("ev2", "METRIC", 5),
            new StreamEvent("ev3", "LOG", 3),
            new StreamEvent("ev4", "TRACE", 2)
        );

        // High-priority Log events processing
        List<String> processedIds = events.stream()
                .filter(e -> "LOG".equals(e.type()))
                .filter(e -> e.priority() > 1)
                .map(e -> e.id().toUpperCase())
                .sorted()
                .collect(Collectors.toList());

        System.out.println("Processed High-Priority Logs: " + processedIds);
    }
}
