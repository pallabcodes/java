package com.backend.core.concurrency;

import java.util.concurrent.StructuredTaskScope;
import java.util.function.Supplier;

/**
 * Step 04: Structured Concurrency (L7 Mastery)
 * 
 * CONCEPT:
 * Traditional 'ExecutorService' is "unstructured"—you submit tasks and they have 
 * no formal relationship. If the parent thread fails, children keep running (leak).
 * 
 * L7 AWARENESS:
 * 1. SCOPE: StructuredTaskScope treats a group of tasks as a single unit of work.
 * 2. ERROR PROPAGATION: ShutdownOnFailure policy ensures that if one task fails, 
 *    all other sibling tasks are cancelled immediately.
 */
public class Step04_StructuredConcurrency {

    public record UserProfile(String name, int score) {}

    public static UserProfile fetchUserProfile() throws Exception {
        System.out.println("--- Demonstrating Structured Concurrency (Fan-out) ---");

        // StructuredTaskScope implements AutoCloseable
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            
            // Forking subtasks
            Supplier<String> nameSubtask = scope.fork(() -> {
                Thread.sleep(100); // Simulate network latency
                return "L7_Engineer";
            });

            Supplier<Integer> scoreSubtask = scope.fork(() -> {
                Thread.sleep(50); // Simulate DB latency
                return 9000;
            });

            // Join all forks and propagate first failure (if any)
            scope.join();
            scope.throwIfFailed();

            // At this point, both subtasks are guaranteed to be complete
            UserProfile profile = new UserProfile(nameSubtask.get(), scoreSubtask.get());
            System.out.println("Fetched Profile: " + profile);
            return profile;
        }
    }

    public static void main(String[] args) {
        try {
            fetchUserProfile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
