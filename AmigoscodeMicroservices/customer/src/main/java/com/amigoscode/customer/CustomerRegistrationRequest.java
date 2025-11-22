package com.amigoscode.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Customer registration request with input validation
 */
public record CustomerRegistrationRequest(
        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 100, message = "Email must not exceed 100 characters")
        String email
) {

    /**
     * Security: Sanitize input to prevent XSS attacks
     */
    public CustomerRegistrationRequest {
        firstName = sanitizeInput(firstName);
        lastName = sanitizeInput(lastName);
        email = sanitizeEmail(email);
    }

    private static String sanitizeInput(String input) {
        if (input == null) return null;
        return input.trim()
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#x27;")
                .replaceAll("&", "&amp;");
    }

    private static String sanitizeEmail(String email) {
        if (email == null) return null;
        return email.trim().toLowerCase();
    }
}
