package com.netflix.productivity.platform.multitenancy;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tenant Provisioning Service
 *
 * Handles tenant lifecycle management including:
 * - Tenant creation and initialization
 * - Schema provisioning (schema-per-tenant)
 * - Tenant metadata management
 * - Resource allocation and quotas
 * - Tenant deactivation and cleanup
 */
@Service
public class TenantProvisioningService {

    private static final Logger logger = LoggerFactory.getLogger(TenantProvisioningService.class);

    // In-memory tenant registry (in production, this would be persisted)
    private final Map<TenantId, Tenant> tenants = new ConcurrentHashMap<>();

    // Schema management service (would be injected)
    private final SchemaManagementService schemaManager = new SchemaManagementService();

    // Tenant configuration templates
    private final Map<TenantType, TenantConfiguration> configurationTemplates = initializeTemplates();

    /**
     * Provision a new tenant with schema-per-tenant isolation
     */
    @Transactional
    public TenantProvisioningResult provisionTenant(TenantRequest request) {
        logger.info("Provisioning new tenant: {}", request.getName());

        try {
            // Validate request
            validateTenantRequest(request);

            // Generate tenant ID
            TenantId tenantId = TenantId.generate();

            // Check if tenant name is unique
            if (isTenantNameTaken(request.getName())) {
                throw new IllegalArgumentException("Tenant name already exists: " + request.getName());
            }

            // Get configuration template
            TenantConfiguration config = configurationTemplates.get(request.getType());
            if (config == null) {
                throw new IllegalArgumentException("Unknown tenant type: " + request.getType());
            }

            // Create tenant metadata
            Tenant tenant = Tenant.builder()
                .id(tenantId)
                .name(request.getName())
                .type(request.getType())
                .status(TenantStatus.PROVISIONING)
                .adminUserId(request.getAdminUserId())
                .configuration(config)
                .createdAt(LocalDateTime.now())
                .build();

            // Provision database schema
            String schemaName = provisionDatabaseSchema(tenantId, config);
            tenant.setSchemaName(schemaName);

            // Initialize tenant data
            initializeTenantData(tenant);

            // Register tenant
            tenants.put(tenantId, tenant);
            tenant.setStatus(TenantStatus.ACTIVE);

            logger.info("Successfully provisioned tenant: {} with ID: {}", request.getName(), tenantId);

            return TenantProvisioningResult.success(tenantId, tenant);

        } catch (Exception e) {
            logger.error("Failed to provision tenant: {}", request.getName(), e);
            return TenantProvisioningResult.failure("Provisioning failed: " + e.getMessage());
        }
    }

    /**
     * Get tenant by ID
     */
    public Optional<Tenant> getTenant(TenantId tenantId) {
        return Optional.ofNullable(tenants.get(tenantId));
    }

    /**
     * Get tenant by name
     */
    public Optional<Tenant> getTenantByName(String name) {
        return tenants.values().stream()
            .filter(tenant -> tenant.getName().equals(name))
            .findFirst();
    }

    /**
     * List all tenants with pagination
     */
    public List<Tenant> listTenants(TenantStatus status, int page, int size) {
        return tenants.values().stream()
            .filter(tenant -> status == null || tenant.getStatus() == status)
            .sorted(Comparator.comparing(Tenant::getCreatedAt).reversed())
            .skip((long) page * size)
            .limit(size)
            .toList();
    }

    /**
     * Update tenant configuration
     */
    @Transactional
    public boolean updateTenantConfiguration(TenantId tenantId, TenantConfiguration newConfig) {
        Tenant tenant = tenants.get(tenantId);
        if (tenant == null) {
            return false;
        }

        try {
            // Validate new configuration
            validateConfiguration(newConfig);

            // Update configuration
            tenant.setConfiguration(newConfig);
            tenant.setUpdatedAt(LocalDateTime.now());

            logger.info("Updated configuration for tenant: {}", tenantId);
            return true;

        } catch (Exception e) {
            logger.error("Failed to update tenant configuration: {}", tenantId, e);
            return false;
        }
    }

    /**
     * Suspend tenant
     */
    @Transactional
    public boolean suspendTenant(TenantId tenantId, String reason) {
        Tenant tenant = tenants.get(tenantId);
        if (tenant == null) {
            return false;
        }

        tenant.setStatus(TenantStatus.SUSPENDED);
        tenant.setSuspensionReason(reason);
        tenant.setSuspendedAt(LocalDateTime.now());

        logger.info("Suspended tenant: {} for reason: {}", tenantId, reason);
        return true;
    }

    /**
     * Reactivate suspended tenant
     */
    @Transactional
    public boolean reactivateTenant(TenantId tenantId) {
        Tenant tenant = tenants.get(tenantId);
        if (tenant == null || tenant.getStatus() != TenantStatus.SUSPENDED) {
            return false;
        }

        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setSuspensionReason(null);
        tenant.setSuspendedAt(null);
        tenant.setUpdatedAt(LocalDateTime.now());

        logger.info("Reactivated tenant: {}", tenantId);
        return true;
    }

    /**
     * Deprovision tenant (cleanup)
     */
    @Transactional
    public boolean deprovisionTenant(TenantId tenantId) {
        Tenant tenant = tenants.get(tenantId);
        if (tenant == null) {
            return false;
        }

        try {
            // Mark as deprovisioning
            tenant.setStatus(TenantStatus.DEPROVISIONING);

            // Cleanup tenant data
            cleanupTenantData(tenant);

            // Drop database schema
            schemaManager.dropSchema(tenant.getSchemaName());

            // Remove from registry
            tenants.remove(tenantId);

            logger.info("Successfully deprovisioned tenant: {}", tenantId);
            return true;

        } catch (Exception e) {
            logger.error("Failed to deprovision tenant: {}", tenantId, e);
            tenant.setStatus(TenantStatus.DEPROVISIONING_FAILED);
            return false;
        }
    }

    /**
     * Get tenant statistics
     */
    public TenantStatistics getTenantStatistics() {
        long totalTenants = tenants.size();
        long activeTenants = tenants.values().stream()
            .mapToLong(tenant -> tenant.getStatus() == TenantStatus.ACTIVE ? 1 : 0)
            .sum();
        long suspendedTenants = tenants.values().stream()
            .mapToLong(tenant -> tenant.getStatus() == TenantStatus.SUSPENDED ? 1 : 0)
            .sum();

        Map<TenantType, Long> tenantsByType = tenants.values().stream()
            .collect(java.util.stream.Collectors.groupingBy(
                Tenant::getType,
                java.util.stream.Collectors.counting()
            ));

        LocalDateTime oldestTenant = tenants.values().stream()
            .map(Tenant::getCreatedAt)
            .min(Comparator.naturalOrder())
            .orElse(null);

        return new TenantStatistics(
            totalTenants,
            activeTenants,
            suspendedTenants,
            tenantsByType,
            oldestTenant
        );
    }

    /**
     * Check tenant resource usage against quotas
     */
    public ResourceUsage checkResourceUsage(TenantId tenantId) {
        Tenant tenant = tenants.get(tenantId);
        if (tenant == null) {
            throw new IllegalArgumentException("Tenant not found: " + tenantId);
        }

        // In production, this would query actual resource usage
        // For simulation, return mock data
        TenantConfiguration config = tenant.getConfiguration();

        return new ResourceUsage(
            tenantId,
            45L, // 45% of storage used
            config.getStorageQuotaGb(),
            120, // 120 users
            config.getMaxUsers(),
            85L, // 85% of API calls used
            config.getMonthlyApiCalls()
        );
    }

    /**
     * Private helper methods
     */

    private void validateTenantRequest(TenantRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tenant name cannot be null or empty");
        }
        if (request.getName().length() > 50) {
            throw new IllegalArgumentException("Tenant name cannot exceed 50 characters");
        }
        if (request.getType() == null) {
            throw new IllegalArgumentException("Tenant type cannot be null");
        }
        if (request.getAdminUserId() == null) {
            throw new IllegalArgumentException("Admin user ID cannot be null");
        }
    }

    private boolean isTenantNameTaken(String name) {
        return tenants.values().stream()
            .anyMatch(tenant -> tenant.getName().equalsIgnoreCase(name));
    }

    private void validateConfiguration(TenantConfiguration config) {
        if (config.getStorageQuotaGb() <= 0) {
            throw new IllegalArgumentException("Storage quota must be positive");
        }
        if (config.getMaxUsers() <= 0) {
            throw new IllegalArgumentException("Max users must be positive");
        }
        if (config.getMonthlyApiCalls() <= 0) {
            throw new IllegalArgumentException("Monthly API calls must be positive");
        }
    }

    private String provisionDatabaseSchema(TenantId tenantId, TenantConfiguration config) {
        String schemaName = "tenant_" + tenantId.getValue().replace("-", "_");

        // In production, this would create the actual database schema
        schemaManager.createSchema(schemaName, config);

        logger.info("Provisioned database schema: {} for tenant: {}", schemaName, tenantId);
        return schemaName;
    }

    private void initializeTenantData(Tenant tenant) {
        // Initialize default data for the tenant
        // In production, this would create default projects, users, etc.
        logger.info("Initialized tenant data for: {}", tenant.getId());
    }

    private void cleanupTenantData(Tenant tenant) {
        // Cleanup tenant data before deprovisioning
        logger.info("Cleaned up tenant data for: {}", tenant.getId());
    }

    private Map<TenantType, TenantConfiguration> initializeTemplates() {
        Map<TenantType, TenantConfiguration> templates = new HashMap<>();

        // Startup template
        templates.put(TenantType.STARTUP, new TenantConfiguration(
            10L,    // 10GB storage
            25,     // 25 users
            10000L, // 10K API calls/month
            true,   // Basic features
            false,  // No advanced features
            false   // No premium support
        ));

        // Business template
        templates.put(TenantType.BUSINESS, new TenantConfiguration(
            100L,   // 100GB storage
            500,    // 500 users
            100000L, // 100K API calls/month
            true,   // Basic features
            true,   // Advanced features
            false   // No premium support
        ));

        // Enterprise template
        templates.put(TenantType.ENTERPRISE, new TenantConfiguration(
            1000L,  // 1TB storage
            5000,   // 5000 users
            1000000L, // 1M API calls/month
            true,   // Basic features
            true,   // Advanced features
            true    // Premium support
        ));

        return templates;
    }

    /**
     * Data classes
     */

    public static class TenantProvisioningResult {
        private final boolean success;
        private final TenantId tenantId;
        private final Tenant tenant;
        private final String errorMessage;

        private TenantProvisioningResult(boolean success, TenantId tenantId, Tenant tenant, String errorMessage) {
            this.success = success;
            this.tenantId = tenantId;
            this.tenant = tenant;
            this.errorMessage = errorMessage;
        }

        public static TenantProvisioningResult success(TenantId tenantId, Tenant tenant) {
            return new TenantProvisioningResult(true, tenantId, tenant, null);
        }

        public static TenantProvisioningResult failure(String errorMessage) {
            return new TenantProvisioningResult(false, null, null, errorMessage);
        }

        // Getters
        public boolean isSuccess() { return success; }
        public TenantId getTenantId() { return tenantId; }
        public Tenant getTenant() { return tenant; }
        public String getErrorMessage() { return errorMessage; }
    }

    public static class TenantStatistics {
        private final long totalTenants;
        private final long activeTenants;
        private final long suspendedTenants;
        private final Map<TenantType, Long> tenantsByType;
        private final LocalDateTime oldestTenant;

        public TenantStatistics(long totalTenants, long activeTenants, long suspendedTenants,
                              Map<TenantType, Long> tenantsByType, LocalDateTime oldestTenant) {
            this.totalTenants = totalTenants;
            this.activeTenants = activeTenants;
            this.suspendedTenants = suspendedTenants;
            this.tenantsByType = tenantsByType;
            this.oldestTenant = oldestTenant;
        }

        // Getters
        public long getTotalTenants() { return totalTenants; }
        public long getActiveTenants() { return activeTenants; }
        public long getSuspendedTenants() { return suspendedTenants; }
        public Map<TenantType, Long> getTenantsByType() { return tenantsByType; }
        public LocalDateTime getOldestTenant() { return oldestTenant; }
    }

    public static class ResourceUsage {
        private final TenantId tenantId;
        private final long storageUsedGb;
        private final long storageQuotaGb;
        private final int usersCount;
        private final int maxUsers;
        private final long apiCallsUsed;
        private final long monthlyApiCalls;

        public ResourceUsage(TenantId tenantId, long storageUsedGb, long storageQuotaGb,
                           int usersCount, int maxUsers, long apiCallsUsed, long monthlyApiCalls) {
            this.tenantId = tenantId;
            this.storageUsedGb = storageUsedGb;
            this.storageQuotaGb = storageQuotaGb;
            this.usersCount = usersCount;
            this.maxUsers = maxUsers;
            this.apiCallsUsed = apiCallsUsed;
            this.monthlyApiCalls = monthlyApiCalls;
        }

        // Getters
        public TenantId getTenantId() { return tenantId; }
        public long getStorageUsedGb() { return storageUsedGb; }
        public long getStorageQuotaGb() { return storageQuotaGb; }
        public int getUsersCount() { return usersCount; }
        public int getMaxUsers() { return maxUsers; }
        public long getApiCallsUsed() { return apiCallsUsed; }
        public long getMonthlyApiCalls() { return monthlyApiCalls; }

        public double getStorageUsagePercent() {
            return storageQuotaGb > 0 ? (double) storageUsedGb / storageQuotaGb * 100 : 0;
        }

        public double getUserUsagePercent() {
            return maxUsers > 0 ? (double) usersCount / maxUsers * 100 : 0;
        }

        public double getApiUsagePercent() {
            return monthlyApiCalls > 0 ? (double) apiCallsUsed / monthlyApiCalls * 100 : 0;
        }
    }
}
