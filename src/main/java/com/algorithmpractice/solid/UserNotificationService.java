package com.algorithmpractice.solid;

/**
 * Interface for user notification operations.
 * 
 * <p>This interface demonstrates Interface Segregation Principle by providing
 * only notification-related methods. It's focused on a single concern, making
 * it easy to implement different notification strategies (email, SMS, push, etc.).</p>
 * 
 * <p>Key benefits:</p>
 * <ul>
 *   <li><strong>Single responsibility</strong>: Only handles notification logic</li>
 *   <li><strong>Easy to test</strong>: Notification logic can be tested independently</li>
 *   <li><strong>Easy to extend</strong>: New notification channels can be added without affecting other code</li>
 *   <li><strong>Easy to mock</strong>: Simple interface makes mocking straightforward</li>
 * </ul>
 * 
 * <p>This interface follows the Open/Closed Principle - it's open for extension
 * (new notification implementations) but closed for modification (existing code
 * doesn't need to change when new notification channels are added).</p>
 * 
 * @author Algorithm Practice Team
 * @version 1.0.0
 */
public interface UserNotificationService {

    /**
     * Sends a welcome notification to a newly created user.
     * 
     * <p>This method handles the initial communication with new users,
     * typically including:</p>
     * <ul>
     *   <li>Welcome message</li>
     *   <li>Account activation instructions</li>
     *   <li>Getting started guide</li>
     *   <li>Support contact information</li>
     * </ul>
     * 
     * @param user the user to send the welcome notification to
     * @throws IllegalArgumentException if the user is null
     * @throws RuntimeException if the notification fails to send
     */
    void sendWelcomeNotification(User user);

    /**
     * Sends a profile update notification to a user.
     * 
     * <p>This method notifies users when their profile information has been
     * updated, which is important for:</p>
     * <ul>
     *   <li>Security awareness</li>
     *   <li>Data transparency</li>
     *   <li>Fraud prevention</li>
     * </ul>
     * 
     * @param user the user to send the profile update notification to
     * @throws IllegalArgumentException if the user is null
     * @throws RuntimeException if the notification fails to send
     */
    void sendProfileUpdateNotification(User user);

    /**
     * Sends a deactivation notification to a user.
     * 
     * <p>This method notifies users when their account has been deactivated,
     * which is important for:</p>
     * <ul>
     *   <li>Account security</li>
     *   <li>User awareness</li>
     *   <li>Reactivation instructions</li>
     * </ul>
     * 
     * @param user the user to send the deactivation notification to
     * @throws IllegalArgumentException if the user is null
     * @throws RuntimeException if the notification fails to send
     */
    void sendDeactivationNotification(User user);

    /**
     * Sends a password reset notification to a user.
     * 
     * <p>This method handles password reset requests, typically including:</p>
     * <ul>
     *   <li>Reset link or code</li>
     *   <li>Security warnings</li>
     *   <li>Expiration information</li>
     * </ul>
     * 
     * @param user the user to send the password reset notification to
     * @param resetToken the password reset token
     * @throws IllegalArgumentException if the user or resetToken is null
     * @throws RuntimeException if the notification fails to send
     */
    void sendPasswordResetNotification(User user, String resetToken);

    /**
     * Sends a security alert notification to a user.
     * 
     * <p>This method handles security-related notifications, such as:</p>
     * <ul>
     *   <li>Failed login attempts</li>
     *   <li>Suspicious activity</li>
     *   <li>Account lockouts</li>
     *   <li>Password changes</li>
     * </ul>
     * 
     * @param user the user to send the security alert to
     * @param alertType the type of security alert
     * @param details additional details about the security event
     * @throws IllegalArgumentException if the user or alertType is null
     * @throws RuntimeException if the notification fails to send
     */
    void sendSecurityAlert(User user, SecurityAlertType alertType, String details);

    /**
     * Checks if a user has enabled a specific notification type.
     * 
     * <p>This method allows the system to respect user preferences
     * for different types of notifications.</p>
     * 
     * @param user the user to check preferences for
     * @param notificationType the type of notification to check
     * @return true if the user has enabled this notification type, false otherwise
     * @throws IllegalArgumentException if the user or notificationType is null
     */
    boolean isNotificationEnabled(User user, NotificationType notificationType);
}
