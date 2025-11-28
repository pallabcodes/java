package com.netflix.productivity.platform.multitenancy;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Multi-Tenancy Domain Entities
 *
 * Core entities and value objects for multi-tenant architecture:
 * - TenantId: Unique tenant identifier
 * - Tenant: Tenant aggregate
 * - Supporting enums and configurations
 */

/**
 * Tenant ID - Unique identifier for tenants
 */
public final class TenantId {
    private final String value;

    private TenantId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Tenant ID cannot be null or empty");
        }
        this.value = value.trim();
    }

    public static TenantId of(String value) {
        return new TenantId(value);
    }

    public static TenantId generate() {
        return new TenantId(UUID.randomUUID().toString());
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TenantId tenantId = (TenantId) o;
        return java.util.Objects.equals(value, tenantId.value);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}

/**
 * Tenant Type - Different service tiers
 */
public enum TenantType {
    STARTUP("Startup Plan"),
    BUSINESS("Business Plan"),
    ENTERPRISE("Enterprise Plan");

    private final String displayName;

    TenantType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

/**
 * Tenant Status - Lifecycle states
 */
public enum TenantStatus {
    PROVISIONING("Being provisioned"),
    ACTIVE("Active and operational"),
    SUSPENDED("Temporarily suspended"),
    DEPROVISIONING("Being deprovisioned"),
    DEPROVISIONING_FAILED("Deprovisioning failed");

    private final String description;

    TenantStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isSuspended() {
        return this == SUSPENDED;
    }
}

/**
 * Tenant Configuration - Resource limits and features
 */
public class TenantConfiguration {
    private long storageQuotaGb;
    private int maxUsers;
    private long monthlyApiCalls;
    private boolean basicFeatures;
    private boolean advancedFeatures;
    private boolean premiumSupport;

    public TenantConfiguration(long storageQuotaGb, int maxUsers, long monthlyApiCalls,
                             boolean basicFeatures, boolean advancedFeatures, boolean premiumSupport) {
        this.storageQuotaGb = storageQuotaGb;
        this.maxUsers = maxUsers;
        this.monthlyApiCalls = monthlyApiCalls;
        this.basicFeatures = basicFeatures;
        this.advancedFeatures = advancedFeatures;
        this.premiumSupport = premiumSupport;
    }

    // Getters and setters
    public long getStorageQuotaGb() { return storageQuotaGb; }
    public void setStorageQuotaGb(long storageQuotaGb) { this.storageQuotaGb = storageQuotaGb; }

    public int getMaxUsers() { return maxUsers; }
    public void setMaxUsers(int maxUsers) { this.maxUsers = maxUsers; }

    public long getMonthlyApiCalls() { return monthlyApiCalls; }
    public void setMonthlyApiCalls(long monthlyApiCalls) { this.monthlyApiCalls = monthlyApiCalls; }

    public boolean isBasicFeatures() { return basicFeatures; }
    public void setBasicFeatures(boolean basicFeatures) { this.basicFeatures = basicFeatures; }

    public boolean isAdvancedFeatures() { return advancedFeatures; }
    public void setAdvancedFeatures(boolean advancedFeatures) { this.advancedFeatures = advancedFeatures; }

    public boolean isPremiumSupport() { return premiumSupport; }
    public void setPremiumSupport(boolean premiumSupport) { this.premiumSupport = premiumSupport; }

    @Override
    public String toString() {
        return "TenantConfiguration{" +
                "storageQuotaGb=" + storageQuotaGb +
                ", maxUsers=" + maxUsers +
                ", monthlyApiCalls=" + monthlyApiCalls +
                ", basicFeatures=" + basicFeatures +
                ", advancedFeatures=" + advancedFeatures +
                ", premiumSupport=" + premiumSupport +
                '}';
    }
}

/**
 * Tenant Aggregate - Core tenant entity
 */
public class Tenant {
    private final TenantId id;
    private String name;
    private final TenantType type;
    private TenantStatus status;
    private final String adminUserId;
    private TenantConfiguration configuration;
    private String schemaName;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime suspendedAt;
    private String suspensionReason;

    private Tenant(TenantId id, String name, TenantType type, TenantStatus status,
                  String adminUserId, TenantConfiguration configuration, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.status = status;
        this.adminUserId = adminUserId;
        this.configuration = configuration;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public static TenantBuilder builder() {
        return new TenantBuilder();
    }

    // Getters
    public TenantId getId() { return id; }
    public String getName() { return name; }
    public TenantType getType() { return type; }
    public TenantStatus getStatus() { return status; }
    public String getAdminUserId() { return adminUserId; }
    public TenantConfiguration getConfiguration() { return configuration; }
    public String getSchemaName() { return schemaName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getSuspendedAt() { return suspendedAt; }
    public String getSuspensionReason() { return suspensionReason; }

    // Setters (for internal use)
    public void setName(String name) { this.name = name; }
    public void setStatus(TenantStatus status) { this.status = status; }
    public void setConfiguration(TenantConfiguration configuration) { this.configuration = configuration; }
    public void setSchemaName(String schemaName) { this.schemaName = schemaName; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setSuspendedAt(LocalDateTime suspendedAt) { this.suspendedAt = suspendedAt; }
    public void setSuspensionReason(String suspensionReason) { this.suspensionReason = suspensionReason; }

    public static class TenantBuilder {
        private TenantId id;
        private String name;
        private TenantType type;
        private TenantStatus status;
        private String adminUserId;
        private TenantConfiguration configuration;
        private LocalDateTime createdAt;

        public TenantBuilder id(TenantId id) { this.id = id; return this; }
        public TenantBuilder name(String name) { this.name = name; return this; }
        public TenantBuilder type(TenantType type) { this.type = type; return this; }
        public TenantBuilder status(TenantStatus status) { this.status = status; return this; }
        public TenantBuilder adminUserId(String adminUserId) { this.adminUserId = adminUserId; return this; }
        public TenantBuilder configuration(TenantConfiguration configuration) { this.configuration = configuration; return this; }
        public TenantBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Tenant build() {
            if (id == null) throw new IllegalStateException("Tenant ID is required");
            if (name == null) throw new IllegalStateException("Tenant name is required");
            if (type == null) throw new IllegalStateException("Tenant type is required");
            if (status == null) status = TenantStatus.PROVISIONING;
            if (adminUserId == null) throw new IllegalStateException("Admin user ID is required");
            if (configuration == null) throw new IllegalStateException("Tenant configuration is required");
            if (createdAt == null) createdAt = LocalDateTime.now();

            return new Tenant(id, name, type, status, adminUserId, configuration, createdAt);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tenant tenant = (Tenant) o;
        return java.util.Objects.equals(id, tenant.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Tenant{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", schemaName='" + schemaName + '\'' +
                '}';
    }
}

/**
 * Tenant Request - For tenant provisioning
 */
public class TenantRequest {
    private final String name;
    private final TenantType type;
    private final String adminUserId;

    public TenantRequest(String name, TenantType type, String adminUserId) {
        this.name = name;
        this.type = type;
        this.adminUserId = adminUserId;
    }

    public String getName() { return name; }
    public TenantType getType() { return type; }
    public String getAdminUserId() { return adminUserId; }
}

/**
 * Schema Management Service - Handles database schema operations
 */
public class SchemaManagementService {

    public void createSchema(String schemaName, TenantConfiguration config) {
        // In production, this would execute DDL statements to create tenant schema
        // For now, we'll just log the operation
        System.out.println("Creating schema: " + schemaName + " with config: " + config);
    }

    public void dropSchema(String schemaName) {
        // In production, this would execute DDL statements to drop tenant schema
        System.out.println("Dropping schema: " + schemaName);
    }

    public boolean schemaExists(String schemaName) {
        // In production, this would check if schema exists in database
        return false; // Assume schema doesn't exist for provisioning
    }
}

/**
 * Tenant Context - Thread-local tenant context holder
 */
public class TenantContext {
    private static final ThreadLocal<TenantContext> current = new ThreadLocal<>();

    private final String tenantId;
    private final String schemaName;

    private TenantContext(String tenantId, String schemaName) {
        this.tenantId = tenantId;
        this.schemaName = schemaName;
    }

    public static void setCurrentTenant(String tenantId, String schemaName) {
        current.set(new TenantContext(tenantId, schemaName));
    }

    public static TenantContext getCurrentTenant() {
        return current.get();
    }

    public static void clear() {
        current.remove();
    }

    public String getTenantId() { return tenantId; }
    public String getSchemaName() { return schemaName; }

    @Override
    public String toString() {
        return "TenantContext{tenantId='" + tenantId + "', schemaName='" + schemaName + "'}";
    }
}
