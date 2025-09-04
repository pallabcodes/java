package com.algorithmpractice.solid;

/**
 * Enum representing different types of data access operations.
 * 
 * <p>This enum demonstrates the Open/Closed Principle by providing
 * a fixed set of data access types that can be extended without modifying
 * existing code. It also provides utility methods for data access management.</p>
 * 
 * @author Algorithm Practice Team
 * @version 1.0.0
 */
public enum DataAccessType {

    /**
     * Data viewed by the user.
     */
    VIEW("View", "Data was viewed by the user"),

    /**
     * Data exported by the user.
     */
    EXPORT("Export", "Data was exported by the user"),

    /**
     * Data shared by the user.
     */
    SHARE("Share", "Data was shared by the user"),

    /**
     * Data accessed by an administrator.
     */
    ADMIN_ACCESS("Admin Access", "Data was accessed by an administrator"),

    /**
     * Data accessed for audit purposes.
     */
    AUDIT("Audit", "Data was accessed for audit purposes"),

    /**
     * Data accessed for backup purposes.
     */
    BACKUP("Backup", "Data was accessed for backup purposes"),

    /**
     * Data accessed for system maintenance.
     */
    MAINTENANCE("Maintenance", "Data was accessed for system maintenance"),

    /**
     * Data accessed for analytics.
     */
    ANALYTICS("Analytics", "Data was accessed for analytics purposes"),

    /**
     * Data accessed for compliance reporting.
     */
    COMPLIANCE("Compliance", "Data was accessed for compliance reporting"),

    /**
     * Data accessed for debugging.
     */
    DEBUG("Debug", "Data was accessed for debugging purposes");

    private final String displayName;
    private final String description;

    /**
     * Constructs a new DataAccessType with the specified display name and description.
     * 
     * @param displayName the human-readable name for the data access type
     * @param description the detailed description of what the data access type means
     */
    DataAccessType(final String displayName, final String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Gets the human-readable display name for this data access type.
     * 
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the detailed description of this data access type.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks if this data access type is user-initiated.
     * 
     * @return true if the data access type is user-initiated, false otherwise
     */
    public boolean isUserInitiated() {
        return this == VIEW || this == EXPORT || this == SHARE;
    }

    /**
     * Checks if this data access type is system-initiated.
     * 
     * @return true if the data access type is system-initiated, false otherwise
     */
    public boolean isSystemInitiated() {
        return this == BACKUP || this == MAINTENANCE || this == ANALYTICS || this == DEBUG;
    }

    /**
     * Checks if this data access type is administrative.
     * 
     * @return true if the data access type is administrative, false otherwise
     */
    public boolean isAdministrative() {
        return this == ADMIN_ACCESS || this == AUDIT || this == COMPLIANCE;
    }

    /**
     * Checks if this data access type requires special permissions.
     * 
     * @return true if the data access type requires special permissions, false otherwise
     */
    public boolean requiresSpecialPermissions() {
        return this == ADMIN_ACCESS || this == AUDIT || this == COMPLIANCE || this == DEBUG;
    }

    /**
     * Checks if this data access type is for compliance purposes.
     * 
     * @return true if the data access type is for compliance purposes, false otherwise
     */
    public boolean isComplianceRelated() {
        return this == AUDIT || this == COMPLIANCE;
    }

    /**
     * Gets a DataAccessType from its display name.
     * 
     * @param displayName the display name to search for
     * @return the matching DataAccessType, or null if not found
     */
    public static DataAccessType fromDisplayName(final String displayName) {
        if (displayName == null) {
            return null;
        }
        
        for (final DataAccessType type : values()) {
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
