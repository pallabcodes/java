package com.algorithmpractice.solid;

/**
 * Interface for user validation operations.
 * 
 * <p>This interface demonstrates Interface Segregation Principle by providing
 * only validation-related methods. It's focused on a single concern, making
 * it easy to implement different validation strategies.</p>
 * 
 * <p>Key benefits:</p>
 * <ul>
 *   <li><strong>Single responsibility</strong>: Only handles validation logic</li>
 *   <li><strong>Easy to test</strong>: Validation logic can be tested independently</li>
 *   <li><strong>Easy to extend</strong>: New validation rules can be added without affecting other code</li>
 *   <li><strong>Easy to mock</strong>: Simple interface makes mocking straightforward</li>
 * </ul>
 * 
 * <p>This interface follows the Open/Closed Principle - it's open for extension
 * (new validation implementations) but closed for modification (existing code
 * doesn't need to change when new validators are added).</p>
 * 
 * @author Algorithm Practice Team
 * @version 1.0.0
 */
public interface UserValidator {

    /**
     * Validates a user creation request.
     * 
     * <p>This method checks all the requirements for creating a new user,
     * including:</p>
     * <ul>
     *   <li>Required field presence</li>
     *   <li>Data format validation (email, names, etc.)</li>
     *   <li>Business rule validation</li>
     *   <li>Uniqueness constraints</li>
     * </ul>
     * 
     * @param request the user creation request to validate
     * @return a validation result indicating success/failure and any error messages
     * @throws IllegalArgumentException if the request is null
     */
    ValidationResult validateCreateRequest(CreateUserRequest request);

    /**
     * Validates a user update request.
     * 
     * <p>This method validates updates to existing users, which may have
     * different rules than user creation:</p>
     * <ul>
     *   <li>Field format validation</li>
     *   <li>Business rule validation</li>
     *   <li>Update-specific constraints</li>
     * </ul>
     * 
     * @param request the user update request to validate
     * @return a validation result indicating success/failure and any error messages
     * @throws IllegalArgumentException if the request is null
     */
    ValidationResult validateUpdateRequest(UpdateUserRequest request);

    /**
     * Validates a user email address.
     * 
     * <p>This method provides focused email validation that can be used
     * independently of other validation operations.</p>
     * 
     * @param email the email address to validate
     * @return a validation result indicating success/failure and any error messages
     * @throws IllegalArgumentException if the email is null
     */
    ValidationResult validateEmail(String email);

    /**
     * Validates a user's name fields.
     * 
     * <p>This method validates first and last names according to business rules,
     * such as length constraints, character restrictions, etc.</p>
     * 
     * @param firstName the first name to validate
     * @param lastName the last name to validate
     * @return a validation result indicating success/failure and any error messages
     * @throws IllegalArgumentException if either name is null
     */
    ValidationResult validateNames(String firstName, String lastName);

    /**
     * Checks if a user ID is valid.
     * 
     * <p>This method validates the format and structure of user IDs,
     * which is useful for input sanitization and error handling.</p>
     * 
     * @param userId the user ID to validate
     * @return a validation result indicating success/failure and any error messages
     * @throws IllegalArgumentException if the user ID is null
     */
    ValidationResult validateUserId(String userId);
}
