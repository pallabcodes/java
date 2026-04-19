package com.backend.solid;

/**
 * Step 01: Single Responsibility Principle (SRP)
 * 
 * L5 Principles:
 * 1. Cohesion: A class should have only one reason to change.
 * 2. Decoupling: Logic for validation, storage, and notification should be separate.
 * 3. Testability: Smaller, focused classes are easier to unit test.
 */
public class Step01_SRP {

    // Domain Record
    public record User(String email, String name) {}

    // 🏆 L5 Way: Specific components each doing ONE thing
    
    public static class UserValidator {
        public boolean isValid(User user) {
            System.out.println("Validating user: " + user.email());
            return user.email() != null && user.email().endsWith("@google.com");
        }
    }

    public static class UserRepository {
        public void save(User user) {
            System.out.println("Saving user to Cloud Spanner: " + user.email());
        }
    }

    public static class NotificationService {
        public void sendWelcomeEmail(User user) {
            System.out.println("Sending Gmail welcome to: " + user.email());
        }
    }

    // The orchestrator that coordinates but doesn't implement the logic
    public static class UserService {
        private final UserValidator validator = new UserValidator();
        private final UserRepository repository = new UserRepository();
        private final NotificationService notification = new NotificationService();

        public void registerUser(User user) {
            if (validator.isValid(user)) {
                repository.save(user);
                notification.sendWelcomeEmail(user);
            } else {
                System.err.println("Registration failed: Invalid user domain.");
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Step 01: Single Responsibility Principle (Gmail Service) ===");
        UserService service = new UserService();
        
        service.registerUser(new User("antigravity@google.com", "L5 Engineer"));
        service.registerUser(new User("hacker@external.com", "Unknown"));
    }
}
