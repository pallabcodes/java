package com.algorithmpractice.solid;

import java.util.Objects;

/**
 * Class representing an audit log entry.
 * 
 * <p>This class demonstrates Single Responsibility Principle by only
 * containing audit log data. It provides a clean, immutable
 * interface for audit entries and includes proper validation.</p>
 * 
 * <p>Key benefits:</p>
 * <ul>
 *   <li><strong>Immutable</strong>: Once created, cannot be modified</li>
 *   <li><strong>Validated</strong>: Includes input validation</li>
 *   <li><strong>Focused</strong>: Only contains audit log data</li>
 *   <li><strong>Type-safe</strong>: Uses proper types for all fields</li>
 * </ul>
 * 
 * @author Algorithm Practice Team
 * @version 1.0.0
 */
public final class AuditLogEntry {

    private final String id;
    private final String userId;
    private final String action;
    private final String details;
    private final String requesterId;
    private final long timestamp;
    private final String ipAddress;
    private final String userAgent;
    private final AuditLogLevel level;

    /**
     * Constructs a new AuditLogEntry with the specified data.
     * 
     * @param id the unique identifier for the audit log entry
     * @param userId the ID of the user being audited
     * @param action the action being audited
     * @param details additional details about the action
     * @param requesterId the ID of the user/system requesting the action
     * @param timestamp the timestamp when the action occurred
     * @param ipAddress the IP address where the action originated
     * @param userAgent the user agent string from the client
     * @param level the severity level of the audit entry
     * @throws IllegalArgumentException if required fields are null or empty
     */
    public AuditLogEntry(
            final String id,
            final String userId,
            final String action,
            final String details,
            final String requesterId,
            final long timestamp,
            final String ipAddress,
            final String userAgent,
            final AuditLogLevel level) {
        
        // Validate required fields
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID is required");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (action == null || action.trim().isEmpty()) {
            throw new IllegalArgumentException("Action is required");
        }
        if (timestamp <= 0) {
            throw new IllegalArgumentException("Timestamp must be positive");
        }
        if (level == null) {
            throw new IllegalArgumentException("Audit log level is required");
        }

        // Store validated data
        this.id = id.trim();
        this.userId = userId.trim();
        this.action = action.trim();
        this.details = details != null ? details.trim() : null;
        this.requesterId = requesterId != null ? requesterId.trim() : null;
        this.timestamp = timestamp;
        this.ipAddress = ipAddress != null ? ipAddress.trim() : null;
        this.userAgent = userAgent != null ? userAgent.trim() : null;
        this.level = level;
    }

    /**
     * Gets the unique identifier for the audit log entry.
     * 
     * @return the ID
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the ID of the user being audited.
     * 
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Gets the action being audited.
     * 
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * Gets additional details about the action.
     * 
     * @return the details, or null if not provided
     */
    public String getDetails() {
        return details;
    }

    /**
     * Gets the ID of the user/system requesting the action.
     * 
     * @return the requester ID, or null if not provided
     */
    public String getRequesterId() {
        return requesterId;
    }

    /**
     * Gets the timestamp when the action occurred.
     * 
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the IP address where the action originated.
     * 
     * @return the IP address, or null if not provided
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Gets the user agent string from the client.
     * 
     * @return the user agent string, or null if not provided
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Gets the severity level of the audit entry.
     * 
     * @return the audit log level
     */
    public AuditLogLevel getLevel() {
        return level;
    }

    /**
     * Checks if details were provided.
     * 
     * @return true if details are present, false otherwise
     */
    public boolean hasDetails() {
        return details != null && !details.trim().isEmpty();
    }

    /**
     * Checks if a requester ID was provided.
     * 
     * @return true if requester ID is present, false otherwise
     */
    public boolean hasRequesterId() {
        return requesterId != null && !requesterId.trim().isEmpty();
    }

    /**
     * Checks if an IP address was provided.
     * 
     * @return true if IP address is present, false otherwise
     */
    public boolean hasIpAddress() {
        return ipAddress != null && !ipAddress.trim().isEmpty();
    }

    /**
     * Checks if a user agent was provided.
     * 
     * @return true if user agent is present, false otherwise
     */
    public boolean hasUserAgent() {
        return userAgent != null && !userAgent.trim().isEmpty();
    }

    /**
     * Gets a human-readable representation of the audit log entry.
     * 
     * @return a formatted string with audit log details
     */
    public String getDisplayString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("[").append(level.getDisplayName()).append("] ");
        sb.append(action).append(" for user ").append(userId);
        
        if (hasDetails()) {
            sb.append(": ").append(details);
        }
        
        if (hasIpAddress()) {
            sb.append(" from ").append(ipAddress);
        }
        
        return sb.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final AuditLogEntry other = (AuditLogEntry) obj;
        return timestamp == other.timestamp &&
               Objects.equals(id, other.id) &&
               Objects.equals(userId, other.userId) &&
               Objects.equals(action, other.action) &&
               Objects.equals(details, other.details) &&
               Objects.equals(requesterId, other.requesterId) &&
               Objects.equals(ipAddress, other.ipAddress) &&
               Objects.equals(userAgent, other.userAgent) &&
               Objects.equals(level, other.level);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, action, details, requesterId, timestamp, ipAddress, userAgent, level);
    }

    @Override
    public String toString() {
        return "AuditLogEntry{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", action='" + action + '\'' +
                ", details='" + details + '\'' +
                ", requesterId='" + requesterId + '\'' +
                ", timestamp=" + timestamp +
                ", ipAddress='" + ipAddress + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", level=" + level +
                '}';
    }

    /**
     * Builder class for creating AuditLogEntry instances.
     * 
     * <p>This builder provides a fluent API for creating audit log entries
     * with proper validation and optional fields.</p>
     */
    public static class Builder {
        private String id;
        private String userId;
        private String action;
        private String details;
        private String requesterId;
        private long timestamp;
        private String ipAddress;
        private String userAgent;
        private AuditLogLevel level;

        public Builder id(final String id) {
            this.id = id;
            return this;
        }

        public Builder userId(final String userId) {
            this.userId = userId;
            return this;
        }

        public Builder action(final String action) {
            this.action = action;
            return this;
        }

        public Builder details(final String details) {
            this.details = details;
            return this;
        }

        public Builder requesterId(final String requesterId) {
            this.requesterId = requesterId;
            return this;
        }

        public Builder timestamp(final long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder ipAddress(final String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder userAgent(final String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder level(final AuditLogLevel level) {
            this.level = level;
            return this;
        }

        public AuditLogEntry build() {
            return new AuditLogEntry(id, userId, action, details, requesterId, timestamp, ipAddress, userAgent, level);
        }
    }
}
