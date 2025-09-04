package com.algorithmpractice.solid;

/**
 * Enum representing different types of notifications that can be sent to users.
 * 
 * <p>This enum demonstrates the Open/Closed Principle by providing
 * a fixed set of notification types that can be extended without modifying
 * existing code. It also provides utility methods for notification management.</p>
 * 
 * @author Algorithm Practice Team
 * @version 1.0.0
 */
public enum NotificationType {

    /**
     * Welcome notification sent to new users.
     */
    WELCOME("Welcome", "Welcome notifications for new users"),

    /**
     * Profile update notifications.
     */
    PROFILE_UPDATE("Profile Update", "Notifications when profile information changes"),

    /**
     * Security-related notifications.
     */
    SECURITY("Security", "Security alerts and warnings"),

    /**
     * Password reset notifications.
     */
    PASSWORD_RESET("Password Reset", "Password reset instructions and confirmations"),

    /**
     * Account deactivation notifications.
     */
    ACCOUNT_DEACTIVATION("Account Deactivation", "Notifications when accounts are deactivated"),

    /**
     * Marketing and promotional notifications.
     */
    MARKETING("Marketing", "Promotional and marketing communications"),

    /**
     * System maintenance notifications.
     */
    SYSTEM_MAINTENANCE("System Maintenance", "System maintenance and outage notifications"),

    /**
     * Newsletter and updates.
     */
    NEWSLETTER("Newsletter", "Regular newsletters and updates");

    private final String displayName;
    private final String description;

    /**
     * Constructs a new NotificationType with the specified display name and description.
     * 
     * @param displayName the human-readable name for the notification type
     * @param description the detailed description of what the notification type is for
     */
    NotificationType(final String displayName, final String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Gets the human-readable display name for this notification type.
     * 
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the detailed description of this notification type.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks if this notification type is security-related.
     * 
     * @return true if the notification type is security-related, false otherwise
     */
    public boolean isSecurityRelated() {
        return this == SECURITY || this == PASSWORD_RESET || this == ACCOUNT_DEACTIVATION;
    }

    /**
     * Checks if this notification type is marketing-related.
     * 
     * @return true if the notification type is marketing-related, false otherwise
     */
    public boolean isMarketingRelated() {
        return this == MARKETING || this == NEWSLETTER;
    }

    /**
     * Checks if this notification type is system-related.
     * 
     * @return true if the notification type is system-related, false otherwise
     */
    public boolean isSystemRelated() {
        return this == SYSTEM_MAINTENANCE;
    }

    /**
     * Checks if this notification type is user lifecycle-related.
     * 
     * @return true if the notification type is user lifecycle-related, false otherwise
     */
    public boolean isUserLifecycleRelated() {
        return this == WELCOME || this == PROFILE_UPDATE || this == ACCOUNT_DEACTIVATION;
    }

    /**
     * Gets a NotificationType from its display name.
     * 
     * @param displayName the display name to search for
     * @return the matching NotificationType, or null if not found
     */
    public static NotificationType fromDisplayName(final String displayName) {
        if (displayName == null) {
            return null;
        }
        
        for (final NotificationType type : values()) {
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
