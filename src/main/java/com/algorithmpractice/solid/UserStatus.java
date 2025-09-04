package com.algorithmpractice.solid;

/**
 * Enum representing the possible statuses of a user account.
 * 
 * <p>This enum demonstrates the Open/Closed Principle by providing
 * a fixed set of statuses that can be extended without modifying
 * existing code. It also provides utility methods for status management.</p>
 * 
 * @author Algorithm Practice Team
 * @version 1.0.0
 */
public enum UserStatus {

    /**
     * User account is active and can access the system.
     */
    ACTIVE("Active", "User account is active and can access the system"),

    /**
     * User account has been deactivated and cannot access the system.
     */
    DEACTIVATED("Deactivated", "User account has been deactivated and cannot access the system"),

    /**
     * User account is suspended temporarily.
     */
    SUSPENDED("Suspended", "User account is temporarily suspended"),

    /**
     * User account is pending activation.
     */
    PENDING("Pending", "User account is pending activation"),

    /**
     * User account has been locked due to security concerns.
     */
    LOCKED("Locked", "User account has been locked due to security concerns");

    private final String displayName;
    private final String description;

    /**
     * Constructs a new UserStatus with the specified display name and description.
     * 
     * @param displayName the human-readable name for the status
     * @param description the detailed description of what the status means
     */
    UserStatus(final String displayName, final String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Gets the human-readable display name for this status.
     * 
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the detailed description of this status.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks if this status allows the user to access the system.
     * 
     * @return true if the user can access the system, false otherwise
     */
    public boolean allowsAccess() {
        return this == ACTIVE;
    }

    /**
     * Checks if this status prevents the user from accessing the system.
     * 
     * @return true if the user cannot access the system, false otherwise
     */
    public boolean preventsAccess() {
        return this == DEACTIVATED || this == SUSPENDED || this == LOCKED;
    }

    /**
     * Checks if this status requires action from the user or administrator.
     * 
     * @return true if action is required, false otherwise
     */
    public boolean requiresAction() {
        return this == PENDING || this == SUSPENDED || this == LOCKED;
    }

    /**
     * Gets a UserStatus from its display name.
     * 
     * @param displayName the display name to search for
     * @return the matching UserStatus, or null if not found
     */
    public static UserStatus fromDisplayName(final String displayName) {
        if (displayName == null) {
            return null;
        }
        
        for (final UserStatus status : values()) {
            if (status.displayName.equalsIgnoreCase(displayName)) {
                return status;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
