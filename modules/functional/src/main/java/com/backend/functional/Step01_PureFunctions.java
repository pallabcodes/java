package com.backend.functional;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Step 01: Pure Functions & Immutability
 * 
 * L5 Principles:
 * 1. Purity: No side effects, output depends solely on input.
 * 2. Immutability: Objects cannot be changed after creation (Java Records).
 * 3. Thread-safety: Pure functions are inherently thread-safe.
 */
public class Step01_PureFunctions {

    // Domain object using Java Record for absolute immutability
    public record Email(String address, String sender, List<String> labels) {}

    // Pure validation logic
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@google\\.com$");

    /**
     * A pure function to validate if an email belongs to the internal google.com domain.
     * It does not change any state or interact with any external systems.
     */
    public static boolean isInternalEmail(Email email) {
        if (email == null || email.address() == null) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.address()).matches();
    }

    /**
     * A pure function that returns a NEW email with an added label.
     * It doesn't modify the existing record (which is impossible anyway).
     */
    public static Email addLabel(Email email, String newLabel) {
        List<String> updatedLabels = new java.util.ArrayList<>(email.labels());
        updatedLabels.add(newLabel);
        // We return a completely new instance
        return new Email(email.address(), email.sender(), List.copyOf(updatedLabels));
    }

    public static void main(String[] args) {
        System.out.println("=== Step 01: Pure Functions & Immutability ===");

        Email original = new Email("antigravity@google.com", "System", List.of("Inbox"));
        
        System.out.println("Original: " + original);
        System.out.println("Is Internal? " + isInternalEmail(original));

        Email tagged = addLabel(original, "Refactored");
        System.out.println("Tagged: " + tagged);
        System.out.println("Original still untouched: " + original);
    }
}
