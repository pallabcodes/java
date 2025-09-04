package com.algorithmpractice.solid;

/**
 * Enum representing different severity levels for audit log entries.
 * 
 * <p>This enum demonstrates the Open/Closed Principle by providing
 * a fixed set of audit log levels that can be extended without modifying
 * existing code. It also provides utility methods for audit level management.</p>
 * 
 * @author Algorithm Practice Team
 * @version 1.0.0
 */
public enum AuditLogLevel {

    /**
     * Informational audit entries.
     */
    INFO("Info", "Informational audit entries", 1),

    /**
     * Warning audit entries.
     */
    WARNING("Warning", "Warning audit entries", 2),

    /**
     * Error audit entries.
     */
    ERROR("Error", "Error audit entries", 3),

    /**
     * Critical audit entries.
     */
    CRITICAL("Critical", "Critical audit entries", 4),

    /**
     * Security audit entries.
     */
    SECURITY("Security", "Security-related audit entries", 5);

    private final String displayName;
    private final String description;
    private final int severity;

    /**
     * Constructs a new AuditLogLevel with the specified display name, description, and severity.
     * 
     * @param displayName the human-readable name for the audit log level
     * @param description the detailed description of what the audit log level means
     * @param severity the numeric severity level (higher = more severe)
     */
    AuditLogLevel(final String displayName, final String description, final int severity) {
        this.displayName = displayName;
        this.description = description;
        this.severity = severity;
    }

    /**
     * Gets the human-readable display name for this audit log level.
     * 
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the detailed description of this audit log level.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the numeric severity level.
     * 
     * @return the severity level (higher = more severe)
     */
    public int getSeverity() {
        return severity;
    }

    /**
     * Checks if this audit log level is informational.
     * 
     * @return true if the audit log level is informational, false otherwise
     */
    public boolean isInformational() {
        return this == INFO;
    }

    /**
     * Checks if this audit log level is a warning.
     * 
     * @return true if the audit log level is a warning, false otherwise
     */
    public boolean isWarning() {
        return this == WARNING;
    }

    /**
     * Checks if this audit log level is an error.
     * 
     * @return true if the audit log level is an error, false otherwise
     */
    public boolean isError() {
        return this == ERROR;
    }

    /**
     * Checks if this audit log level is critical.
     * 
     * @return true if the audit log level is critical, false otherwise
     */
    public boolean isCritical() {
        return this == CRITICAL;
    }

    /**
     * Checks if this audit log level is security-related.
     * 
     * @return true if the audit log level is security-related, false otherwise
     */
    public boolean isSecurityRelated() {
        return this == SECURITY;
    }

    /**
     * Checks if this audit log level requires immediate attention.
     * 
     * @return true if the audit log level requires immediate attention, false otherwise
     */
    public boolean requiresImmediateAttention() {
        return this == CRITICAL || this == SECURITY;
    }

    /**
     * Checks if this audit log level requires escalation.
     * 
     * @return true if the audit log level requires escalation, false otherwise
     */
    public boolean requiresEscalation() {
        return this == CRITICAL || this == SECURITY || this == ERROR;
    }

    /**
     * Compares this audit log level with another for severity.
     * 
     * @param other the other audit log level to compare with
     * @return negative if this is less severe, positive if more severe, zero if equal
     */
    public int compareSeverity(final AuditLogLevel other) {
        if (other == null) {
            return 1; // This is more severe than null
        }
        return Integer.compare(this.severity, other.severity);
    }

    /**
     * Checks if this audit log level is more severe than another.
     * 
     * @param other the other audit log level to compare with
     * @return true if this is more severe than the other, false otherwise
     */
    public boolean isMoreSevereThan(final AuditLogLevel other) {
        return compareSeverity(other) > 0;
    }

    /**
     * Checks if this audit log level is less severe than another.
     * 
     * @param other the other audit log level to compare with
     * @return true if this is less severe than the other, false otherwise
     */
    public boolean isLessSevereThan(final AuditLogLevel other) {
        return compareSeverity(other) < 0;
    }

    /**
     * Gets an AuditLogLevel from its display name.
     * 
     * @param displayName the display name to search for
     * @return the matching AuditLogLevel, or null if not found
     */
    public static AuditLogLevel fromDisplayName(final String displayName) {
        if (displayName == null) {
            return null;
        }
        
        for (final AuditLogLevel level : values()) {
            if (level.displayName.equalsIgnoreCase(displayName)) {
                return level;
            }
        }
        return null;
    }

    /**
     * Gets an AuditLogLevel from its severity level.
     * 
     * @param severity the severity level to search for
     * @return the matching AuditLogLevel, or null if not found
     */
    public static AuditLogLevel fromSeverity(final int severity) {
        for (final AuditLogLevel level : values()) {
            if (level.severity == severity) {
                return level;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
