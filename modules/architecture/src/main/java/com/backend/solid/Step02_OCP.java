package com.backend.solid;

import java.util.List;

/**
 * Step 02: Open/Closed Principle (OCP)
 * 
 * L5 Principles:
 * 1. Extensibility: Software entities should be open for extension but closed for modification.
 * 2. Polymorphism: Use interfaces/abstractions to allow different behaviors.
 * 3. Future-proofing: Adding GChat notifications shouldn't require changing GMeet code.
 */
public class Step02_OCP {

    // The abstraction that is CLOSED for modification but OPEN for extension
    public interface NotificationProvider {
        void notify(String message);
    }

    // Concrete extensions
    public static class GmailProvider implements NotificationProvider {
        public void notify(String message) {
            System.out.println("[Gmail] New Alert: " + message);
        }
    }

    public static class GMeetProvider implements NotificationProvider {
        public void notify(String message) {
            System.out.println("[GMeet] In-call notification: " + message);
        }
    }

    // NEW extension added WITHOUT changing the engine below
    public static class SlackProvider implements NotificationProvider {
        public void notify(String message) {
            System.out.println("[External-Slack] Webhook: " + message);
        }
    }

    // The Notification Engine (Closed for Modification)
    public static class NotificationEngine {
        private final List<NotificationProvider> providers;

        public NotificationEngine(List<NotificationProvider> providers) {
            this.providers = providers;
        }

        public void broadcast(String message) {
            providers.forEach(p -> p.notify(message));
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Step 02: Open/Closed Principle (GMeet Alert System) ===");
        
        NotificationEngine engine = new NotificationEngine(List.of(
            new GmailProvider(),
            new GMeetProvider(),
            new SlackProvider() // We extended behavior without touching the broadcast logic
        ));

        engine.broadcast("Meeting 'Architecture Sync' starts in 5 minutes.");
    }
}
