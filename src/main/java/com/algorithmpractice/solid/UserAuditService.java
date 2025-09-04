package com.algorithmpractice.solid;

/**
 * Interface for user audit operations.
 * 
 * <p>This interface demonstrates Interface Segregation Principle by providing
 * only audit-related methods. It's focused on a single concern, making
 * it easy to implement different audit strategies and comply with various
 * regulatory requirements.</p>
 * 
 * <p>Key benefits:</p>
 * <ul>
 *   <li><strong>Single responsibility</strong>: Only handles audit logging</li>
 *   <li><strong>Easy to test</strong>: Audit logic can be tested independently</li>
 *   <li><strong>Easy to extend</strong>: New audit implementations can be added without affecting other code</li>
 *   <li><strong>Easy to mock</strong>: Simple interface makes mocking straightforward</li>
 *   <li><strong>Compliance ready</strong>: Can implement various regulatory requirements (GDPR, SOX, etc.)</li>
 * </ul>
 * 
 * <p>This interface follows the Open/Closed Principle - it's open for extension
 * (new audit implementations) but closed for modification (existing code
 * doesn't need to change when new audit requirements are added).</p>
 * 
 * @author Algorithm Practice Team
 * @version 1.0.0
 */
public interface UserAuditService {

    /**
     * Audits the creation of a new user.
     * 
     * <p>This method logs all the details about user creation for compliance
     * and security purposes, including:</p>
     * <ul>
     *   <li>User details (ID, email, names)</li>
     *   <li>Creation timestamp</li>
     *   <li>Request source (IP, user agent, etc.)</li>
     *   <li>Request details</li>
     *   <li>Audit trail for compliance</li>
     * </ul>
     * 
     * @param user the user that was created
     * @param request the original creation request
     * @throws IllegalArgumentException if the user or request is null
     * @throws RuntimeException if the audit logging fails
     */
    void auditUserCreation(User user, CreateUserRequest request);

    /**
     * Audits the update of an existing user.
     * 
     * <p>This method logs all changes made to user profiles for:</p>
     * <ul>
     *   <li>Change tracking</li>
     *   <li>Security monitoring</li>
     *   <li>Compliance requirements</li>
     *   <li>User activity analysis</li>
     * </ul>
     * 
     * @param oldUser the user before the update
     * @param newUser the user after the update
     * @param request the update request that caused the changes
     * @throws IllegalArgumentException if any parameter is null
     * @throws RuntimeException if the audit logging fails
     */
    void auditUserUpdate(User oldUser, User newUser, UpdateUserRequest request);

    /**
     * Audits the deactivation of a user account.
     * 
     * <p>This method logs account deactivation events for:</p>
     * <ul>
     *   <li>Security monitoring</li>
     *   <li>Compliance tracking</li>
     *   <li>User lifecycle management</li>
     *   <li>Data retention policies</li>
     * </ul>
     * 
     * @param user the user that was deactivated
     * @throws IllegalArgumentException if the user is null
     * @throws RuntimeException if the audit logging fails
     */
    void auditUserDeactivation(User user);

    /**
     * Audits the deletion of a user account.
     * 
     * <p>This method logs account deletion events for:</p>
     * <ul>
     *   <li>Security monitoring</li>
     *   <li>Compliance tracking</li>
     *   <li>Data retention policies</li>
     *   <li>Legal requirements</li>
     * </ul>
     * 
     * @param user the user that was deleted
     * @throws IllegalArgumentException if the user is null
     * @throws RuntimeException if the audit logging fails
     */
    void auditUserDeletion(User user);

    /**
     * Audits user login attempts.
     * 
     * <p>This method logs login attempts for security monitoring and
     * compliance purposes, including:</p>
     * <ul>
     *   <li>Login success/failure</li>
     *   <li>Timestamp and location</li>
     *   <li>Device information</li>
     *   <li>IP address and user agent</li>
     * </ul>
     * 
     * @param userId the ID of the user attempting to login
     * @param success whether the login was successful
     * @param loginDetails additional details about the login attempt
     * @throws IllegalArgumentException if the userId or loginDetails is null
     * @throws RuntimeException if the audit logging fails
     */
    void auditLoginAttempt(String userId, boolean success, LoginDetails loginDetails);

    /**
     * Audits user password changes.
     * 
     * <p>This method logs password change events for security compliance
     * and monitoring purposes.</p>
     * 
     * @param userId the ID of the user whose password was changed
     * @param changeSource the source of the password change (user, admin, reset)
     * @param changeDetails additional details about the password change
     * @throws IllegalArgumentException if any parameter is null
     * @throws RuntimeException if the audit logging fails
     */
    void auditPasswordChange(String userId, PasswordChangeSource changeSource, String changeDetails);

    /**
     * Audits user data access.
     * 
     * <p>This method logs when user data is accessed for compliance
     * and security monitoring purposes.</p>
     * 
     * @param userId the ID of the user whose data was accessed
     * @param accessType the type of access (view, export, etc.)
     * @param accessDetails additional details about the access
     * @param requesterId the ID of the user/system requesting the access
     * @throws IllegalArgumentException if any parameter is null
     * @throws RuntimeException if the audit logging fails
     */
    void auditDataAccess(String userId, DataAccessType accessType, String accessDetails, String requesterId);

    /**
     * Retrieves audit logs for a specific user.
     * 
     * <p>This method allows retrieval of audit logs for compliance
     * reporting and security investigations.</p>
     * 
     * @param userId the ID of the user to retrieve audit logs for
     * @param startTime the start time for the audit log range
     * @param endTime the end time for the audit log range
     * @return a list of audit log entries
     * @throws IllegalArgumentException if the userId is null or time range is invalid
     * @throws RuntimeException if the audit log retrieval fails
     */
    java.util.List<AuditLogEntry> getAuditLogs(String userId, long startTime, long endTime);
}
