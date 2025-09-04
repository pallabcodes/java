package com.algorithmpractice.solid;

import java.util.Objects;

/**
 * Class representing details about a login attempt.
 * 
 * <p>This class demonstrates Single Responsibility Principle by only
 * containing login attempt data. It provides a clean, immutable
 * interface for login details and includes proper validation.</p>
 * 
 * <p>Key benefits:</p>
 * <ul>
 *   <li><strong>Immutable</strong>: Once created, cannot be modified</li>
 *   <li><strong>Validated</strong>: Includes input validation</li>
 *   <li><strong>Focused</strong>: Only contains login attempt data</li>
 *   <li><strong>Type-safe</strong>: Uses proper types for all fields</li>
 * </ul>
 * 
 * @author Algorithm Practice Team
 * @version 1.0.0
 */
public final class LoginDetails {

    private final String ipAddress;
    private final String userAgent;
    private final String deviceId;
    private final String location;
    private final long timestamp;
    private final String sessionId;

    /**
     * Constructs a new LoginDetails with the specified data.
     * 
     * @param ipAddress the IP address of the login attempt
     * @param userAgent the user agent string from the browser/client
     * @param deviceId the unique identifier for the device
     * @param location the geographic location of the login attempt
     * @param timestamp the timestamp of the login attempt
     * @param sessionId the session identifier
     * @throws IllegalArgumentException if required fields are null or empty
     */
    public LoginDetails(
            final String ipAddress,
            final String userAgent,
            final String deviceId,
            final String location,
            final long timestamp,
            final String sessionId) {
        
        // Validate required fields
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("IP address is required");
        }
        if (userAgent == null || userAgent.trim().isEmpty()) {
            throw new IllegalArgumentException("User agent is required");
        }
        if (timestamp <= 0) {
            throw new IllegalArgumentException("Timestamp must be positive");
        }

        // Store validated data
        this.ipAddress = ipAddress.trim();
        this.userAgent = userAgent.trim();
        this.deviceId = deviceId != null ? deviceId.trim() : null;
        this.location = location != null ? location.trim() : null;
        this.timestamp = timestamp;
        this.sessionId = sessionId != null ? sessionId.trim() : null;
    }

    /**
     * Gets the IP address of the login attempt.
     * 
     * @return the IP address
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Gets the user agent string from the browser/client.
     * 
     * @return the user agent string
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Gets the unique identifier for the device.
     * 
     * @return the device ID, or null if not provided
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Gets the geographic location of the login attempt.
     * 
     * @return the location, or null if not provided
     */
    public String getLocation() {
        return location;
    }

    /**
     * Gets the timestamp of the login attempt.
     * 
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the session identifier.
     * 
     * @return the session ID, or null if not provided
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Checks if a device ID was provided.
     * 
     * @return true if device ID is present, false otherwise
     */
    public boolean hasDeviceId() {
        return deviceId != null && !deviceId.trim().isEmpty();
    }

    /**
     * Checks if a location was provided.
     * 
     * @return true if location is present, false otherwise
     */
    public boolean hasLocation() {
        return location != null && !location.trim().isEmpty();
    }

    /**
     * Checks if a session ID was provided.
     * 
     * @return true if session ID is present, false otherwise
     */
    public boolean hasSessionId() {
        return sessionId != null && !sessionId.trim().isEmpty();
    }

    /**
     * Gets a human-readable representation of the login details.
     * 
     * @return a formatted string with login details
     */
    public String getDisplayString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Login from ").append(ipAddress);
        
        if (hasLocation()) {
            sb.append(" (").append(location).append(")");
        }
        
        if (hasDeviceId()) {
            sb.append(" on device ").append(deviceId);
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
        final LoginDetails other = (LoginDetails) obj;
        return timestamp == other.timestamp &&
               Objects.equals(ipAddress, other.ipAddress) &&
               Objects.equals(userAgent, other.userAgent) &&
               Objects.equals(deviceId, other.deviceId) &&
               Objects.equals(location, other.location) &&
               Objects.equals(sessionId, other.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ipAddress, userAgent, deviceId, location, timestamp, sessionId);
    }

    @Override
    public String toString() {
        return "LoginDetails{" +
                "ipAddress='" + ipAddress + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", location='" + location + '\'' +
                ", timestamp=" + timestamp +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }

    /**
     * Builder class for creating LoginDetails instances.
     * 
     * <p>This builder provides a fluent API for creating login details
     * with proper validation and optional fields.</p>
     */
    public static class Builder {
        private String ipAddress;
        private String userAgent;
        private String deviceId;
        private String location;
        private long timestamp;
        private String sessionId;

        public Builder ipAddress(final String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder userAgent(final String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder deviceId(final String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public Builder location(final String location) {
            this.location = location;
            return this;
        }

        public Builder timestamp(final long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder sessionId(final String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public LoginDetails build() {
            return new LoginDetails(ipAddress, userAgent, deviceId, location, timestamp, sessionId);
        }
    }
}
