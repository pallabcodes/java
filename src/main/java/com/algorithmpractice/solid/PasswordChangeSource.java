package com.algorithmpractice.solid;

/**
 * Enum representing different sources of password changes.
 * 
 * <p>This enum demonstrates the Open/Closed Principle by providing
 * a fixed set of password change sources that can be extended without modifying
 * existing code. It also provides utility methods for password change management.</p>
 * 
 * @author Algorithm Practice Team
 * @version 1.0.0
 */
public enum PasswordChangeSource {

    /**
     * Password changed by the user themselves.
     */
    USER("User", "Password changed by the user"),

    /**
     * Password changed by an administrator.
     */
    ADMIN("Admin", "Password changed by an administrator"),

    /**
     * Password changed through a reset process.
     */
    RESET("Reset", "Password changed through password reset"),

    /**
     * Password changed due to security requirements.
     */
    SECURITY_REQUIREMENT("Security Requirement", "Password changed due to security requirements"),

    /**
     * Password changed due to expiration.
     */
    EXPIRATION("Expiration", "Password changed due to expiration"),

    /**
     * Password changed due to suspicious activity.
     */
    SUSPICIOUS_ACTIVITY("Suspicious Activity", "Password changed due to suspicious activity"),

    /**
     * Password changed due to account compromise.
     */
    ACCOUNT_COMPROMISE("Account Compromise", "Password changed due to account compromise");

    private final String displayName;
    private final String description;

    /**
     * Constructs a new PasswordChangeSource with the specified display name and description.
     * 
     * @param displayName the human-readable name for the password change source
     * @param description the detailed description of what the password change source means
     */
    PasswordChangeSource(final String displayName, final String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Gets the human-readable display name for this password change source.
     * 
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the detailed description of this password change source.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks if this password change source is user-initiated.
     * 
     * @return true if the password change source is user-initiated, false otherwise
     */
    public boolean isUserInitiated() {
        return this == USER;
    }

    /**
     * Checks if this password change source is admin-initiated.
     * 
     * @return true if the password change source is admin-initiated, false otherwise
     */
    public boolean isAdminInitiated() {
        return this == ADMIN;
    }

    /**
     * Checks if this password change source is system-initiated.
     * 
     * @return true if the password change source is system-initiated, false otherwise
     */
    public boolean isSystemInitiated() {
        return this == RESET || this == SECURITY_REQUIREMENT || this == EXPIRATION ||
               this == SUSPICIOUS_ACTIVITY || this == ACCOUNT_COMPROMISE;
    }

    /**
     * Checks if this password change source is security-related.
     * 
     * @return true if the password change source is security-related, false otherwise
     */
    public boolean isSecurityRelated() {
        return this == SECURITY_REQUIREMENT || this == SUSPICIOUS_ACTIVITY || this == ACCOUNT_COMPROMISE;
    }

    /**
     * Checks if this password change source requires notification.
     * 
     * @return true if the password change source requires notification, false otherwise
     */
    public boolean requiresNotification() {
        return this == ADMIN || this == SUSPICIOUS_ACTIVITY || this == ACCOUNT_COMPROMISE;
    }

    /**
     * Gets a PasswordChangeSource from its display name.
     * 
     * @param displayName the display name to search for
     * @return the matching PasswordChangeSource, or null if not found
     */
    public static PasswordChangeSource fromDisplayName(final String displayName) {
        if (displayName == null) {
            return null;
        }
        
        for (final PasswordChangeSource source : values()) {
            if (source.displayName.equalsIgnoreCase(displayName)) {
                return source;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
