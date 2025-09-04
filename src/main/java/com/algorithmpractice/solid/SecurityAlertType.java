package com.algorithmpractice.solid;

/**
 * Enum representing different types of security alerts that can be sent to users.
 * 
 * <p>This enum demonstrates the Open/Closed Principle by providing
 * a fixed set of security alert types that can be extended without modifying
 * existing code. It also provides utility methods for security alert management.</p>
 * 
 * @author Algorithm Practice Team
 * @version 1.0.0
 */
public enum SecurityAlertType {

    /**
     * Failed login attempt alert.
     */
    FAILED_LOGIN("Failed Login", "Multiple failed login attempts detected"),

    /**
     * Suspicious activity alert.
     */
    SUSPICIOUS_ACTIVITY("Suspicious Activity", "Unusual account activity detected"),

    /**
     * Account lockout alert.
     */
    ACCOUNT_LOCKOUT("Account Lockout", "Account has been locked due to security concerns"),

    /**
     * Password change alert.
     */
    PASSWORD_CHANGE("Password Change", "Password has been changed"),

    /**
     * New device login alert.
     */
    NEW_DEVICE_LOGIN("New Device Login", "Login from a new device detected"),

    /**
     * Location change alert.
     */
    LOCATION_CHANGE("Location Change", "Login from a new location detected"),

    /**
     * Unusual access time alert.
     */
    UNUSUAL_ACCESS_TIME("Unusual Access Time", "Login at an unusual time detected"),

    /**
     * Multiple account access alert.
     */
    MULTIPLE_ACCOUNT_ACCESS("Multiple Account Access", "Multiple accounts accessed from same device"),

    /**
     * Data export alert.
     */
    DATA_EXPORT("Data Export", "Large amount of data exported"),

    /**
     * Admin action alert.
     */
    ADMIN_ACTION("Admin Action", "Administrative action performed on account");

    private final String displayName;
    private final String description;

    /**
     * Constructs a new SecurityAlertType with the specified display name and description.
     * 
     * @param displayName the human-readable name for the security alert type
     * @param description the detailed description of what the security alert type means
     */
    SecurityAlertType(final String displayName, final String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Gets the human-readable display name for this security alert type.
     * 
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the detailed description of this security alert type.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks if this security alert type is critical.
     * 
     * @return true if the security alert type is critical, false otherwise
     */
    public boolean isCritical() {
        return this == ACCOUNT_LOCKOUT || this == SUSPICIOUS_ACTIVITY || this == MULTIPLE_ACCOUNT_ACCESS;
    }

    /**
     * Checks if this security alert type is informational.
     * 
     * @return true if the security alert type is informational, false otherwise
     */
    public boolean isInformational() {
        return this == PASSWORD_CHANGE || this == NEW_DEVICE_LOGIN || this == LOCATION_CHANGE;
    }

    /**
     * Checks if this security alert type requires immediate action.
     * 
     * @return true if the security alert type requires immediate action, false otherwise
     */
    public boolean requiresImmediateAction() {
        return this == ACCOUNT_LOCKOUT || this == SUSPICIOUS_ACTIVITY;
    }

    /**
     * Gets a SecurityAlertType from its display name.
     * 
     * @param displayName the display name to search for
     * @return the matching SecurityAlertType, or null if not found
     */
    public static SecurityAlertType fromDisplayName(final String displayName) {
        if (displayName == null) {
            return null;
        }
        
        for (final SecurityAlertType type : values()) {
            if (type.displayName.equalsIgnoreCase(displayName)) {
                return type;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
