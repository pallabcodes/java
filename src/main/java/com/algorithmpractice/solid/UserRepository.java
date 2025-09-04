package com.algorithmpractice.solid;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for user data operations.
 * 
 * <p>This interface demonstrates Interface Segregation Principle by providing
 * only the methods that clients actually need. It's focused and cohesive,
 * making it easy to implement and test.</p>
 * 
 * <p>Key benefits:</p>
 * <ul>
 *   <li><strong>Focused responsibility</strong>: Only handles user data operations</li>
 *   <li><strong>Easy to implement</strong>: Concrete classes only need to implement relevant methods</li>
 *   <li><strong>Easy to test</strong>: Mock implementations are simple to create</li>
 *   <li><strong>Easy to extend</strong>: New implementations can be added without affecting existing code</li>
 * </ul>
 * 
 * @author Algorithm Practice Team
 * @version 1.0.0
 */
public interface UserRepository {

    /**
     * Saves a user to the data store.
     * 
     * <p>This method handles both creation and updates. If the user has an ID,
     * it will update the existing user. If no ID is present, it will create
     * a new user.</p>
     * 
     * @param user the user to save
     * @return the saved user (with generated ID if it was a new user)
     * @throws RuntimeException if the save operation fails
     */
    User save(User user);

    /**
     * Finds a user by their unique identifier.
     * 
     * <p>This method returns an Optional to handle the case where no user
     * is found, making the API more explicit about possible outcomes.</p>
     * 
     * @param id the user ID to search for
     * @return an Optional containing the user if found, empty otherwise
     * @throws RuntimeException if the search operation fails
     */
    Optional<User> findById(String id);

    /**
     * Finds a user by their email address.
     * 
     * <p>Email addresses are typically unique in user systems, making this
     * a common lookup pattern.</p>
     * 
     * @param email the email address to search for
     * @return an Optional containing the user if found, empty otherwise
     * @throws RuntimeException if the search operation fails
     */
    Optional<User> findByEmail(String email);

    /**
     * Retrieves all users from the data store.
     * 
     * <p><strong>Warning:</strong> This method should be used carefully in production
     * systems as it can return large datasets. Consider implementing pagination
     * for better performance.</p>
     * 
     * @return a list of all users
     * @throws RuntimeException if the retrieval operation fails
     */
    List<User> findAll();

    /**
     * Retrieves users with pagination support.
     * 
     * <p>This method provides paginated access to users, which is essential
     * for production systems to handle large datasets efficiently.</p>
     * 
     * @param page the page number (0-based)
     * @param size the page size
     * @return a list of users for the specified page
     * @throws RuntimeException if the retrieval operation fails
     */
    List<User> findAll(int page, int size);

    /**
     * Deletes a user from the data store.
     * 
     * <p>This method permanently removes the user. Consider implementing soft
     * delete if you need to maintain audit trails.</p>
     * 
     * @param id the ID of the user to delete
     * @throws RuntimeException if the deletion operation fails
     */
    void deleteById(String id);

    /**
     * Checks if a user exists with the given ID.
     * 
     * <p>This method is useful for validation without retrieving the full
     * user object, making it more efficient than findById().isPresent().</p>
     * 
     * @param id the user ID to check
     * @return true if a user exists with the given ID, false otherwise
     * @throws RuntimeException if the check operation fails
     */
    boolean existsById(String id);

    /**
     * Counts the total number of users in the system.
     * 
     * <p>This method is useful for analytics and monitoring purposes.</p>
     * 
     * @return the total number of users
     * @throws RuntimeException if the count operation fails
     */
    long count();
}
