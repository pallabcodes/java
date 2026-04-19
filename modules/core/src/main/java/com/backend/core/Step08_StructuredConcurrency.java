package com.backend.core;

import java.util.concurrent.StructuredTaskScope;
import java.util.function.Supplier;

/**
 * Step 08: Structured Concurrency (JEP 453)
 * 
 * L7 Principles:
 * 1. Error Propagation: If one sub-task fails, others are cancelled automatically.
 * 2. Observability: Thread dumps show the relationship between parent and child tasks.
 * 3. Short-circuiting: 'ShutdownOnFailure' ensures we don't waste CPU on dead branches.
 */
public class Step08_StructuredConcurrency {

    public record UserProfile(String id, String role) {}
    public record UserPermissions(String id, boolean isAdmin) {}

    public static void main(String[] args) {
        System.out.println("=== Step 08: Structured Concurrency (Loom Beyond) ===");

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            // Forking sub-tasks
            StructuredTaskScope.Subtask<UserProfile> userTask = scope.fork(() -> {
                Thread.sleep(100);
                return new UserProfile("u1", "L7_ENGINEER");
            });

            StructuredTaskScope.Subtask<UserPermissions> permTask = scope.fork(() -> {
                Thread.sleep(50);
                return new UserPermissions("u1", true);
            });

            // Wait for all to finish or the first to fail
            scope.join();
            scope.throwIfFailed();

            // Result processing
            System.out.println("Profile: " + userTask.get().role());
            System.out.println("Is Admin: " + permTask.get().isAdmin());

        } catch (Exception e) {
            System.err.println("Task failed: " + e.getMessage());
        }
    }
}
