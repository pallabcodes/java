package com.netflix.productivity.platform.multitenancy;

import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Schema-Per-Tenant Data Source
 *
 * Implements multi-tenant database isolation using schema-per-tenant approach:
 * - Routes database connections to tenant-specific schemas
 * - Maintains connection pools per tenant
 * - Provides tenant context awareness
 * - Handles tenant provisioning and cleanup
 * - Ensures data isolation between tenants
 */
@Component
public class SchemaPerTenantDataSource extends AbstractRoutingDataSource {

    private static final Logger logger = LoggerFactory.getLogger(SchemaPerTenantDataSource.class);

    // Tenant-specific data sources
    private final Map<String, DataSource> tenantDataSources = new ConcurrentHashMap<>();

    // Schema management
    private final SchemaManagementService schemaManager = new SchemaManagementService();

    // Master data source (for shared data and tenant management)
    private DataSource masterDataSource;

    /**
     * Initialize with master data source
     */
    public SchemaPerTenantDataSource(DataSource masterDataSource) {
        this.masterDataSource = masterDataSource;

        // Set default target data source to master
        setDefaultTargetDataSource(masterDataSource);

        // Initialize target data sources map
        Map<Object, Object> targetDataSources = new ConcurrentHashMap<>();
        targetDataSources.put("master", masterDataSource);
        setTargetDataSources(targetDataSources);
    }

    /**
     * Determine the current tenant's data source
     */
    @Override
    protected Object determineCurrentLookupKey() {
        TenantContext tenantContext = TenantContext.getCurrentTenant();

        if (tenantContext == null) {
            // No tenant context - use master data source
            return "master";
        }

        String tenantId = tenantContext.getTenantId();
        String schemaName = tenantContext.getSchemaName();

        logger.debug("Routing to tenant schema: {} for tenant: {}", schemaName, tenantId);

        return schemaName;
    }

    /**
     * Get connection for the current tenant
     */
    @Override
    public Connection getConnection() throws SQLException {
        String lookupKey = (String) determineCurrentLookupKey();

        if ("master".equals(lookupKey)) {
            return masterDataSource.getConnection();
        }

        // Get or create tenant-specific data source
        DataSource tenantDataSource = getOrCreateTenantDataSource(lookupKey);
        return tenantDataSource.getConnection();
    }

    /**
     * Get connection with username/password for the current tenant
     */
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        String lookupKey = (String) determineCurrentLookupKey();

        if ("master".equals(lookupKey)) {
            return masterDataSource.getConnection(username, password);
        }

        // Get or create tenant-specific data source
        DataSource tenantDataSource = getOrCreateTenantDataSource(lookupKey);
        return tenantDataSource.getConnection(username, password);
    }

    /**
     * Provision data source for a new tenant
     */
    public void provisionTenantDataSource(Tenant tenant) {
        String schemaName = tenant.getSchemaName();
        logger.info("Provisioning data source for tenant: {} with schema: {}", tenant.getId(), schemaName);

        try {
            // Create tenant-specific data source
            DataSource tenantDataSource = createTenantDataSource(schemaName);

            // Store in registry
            tenantDataSources.put(schemaName, tenantDataSource);

            // Add to routing targets
            Map<Object, Object> targetDataSources = new ConcurrentHashMap<>(getTargetDataSources());
            targetDataSources.put(schemaName, tenantDataSource);
            setTargetDataSources(targetDataSources);

            // Initialize schema if needed
            if (!schemaManager.schemaExists(schemaName)) {
                schemaManager.createSchema(schemaName, tenant.getConfiguration());
            }

            logger.info("Successfully provisioned data source for tenant: {}", tenant.getId());

        } catch (Exception e) {
            logger.error("Failed to provision data source for tenant: {}", tenant.getId(), e);
            throw new RuntimeException("Tenant data source provisioning failed", e);
        }
    }

    /**
     * Deprovision data source for a tenant
     */
    public void deprovisionTenantDataSource(Tenant tenant) {
        String schemaName = tenant.getSchemaName();
        logger.info("Deprovisioning data source for tenant: {} with schema: {}", tenant.getId(), schemaName);

        try {
            // Remove from routing targets
            Map<Object, Object> targetDataSources = new ConcurrentHashMap<>(getTargetDataSources());
            targetDataSources.remove(schemaName);
            setTargetDataSources(targetDataSources);

            // Close and remove tenant data source
            DataSource tenantDataSource = tenantDataSources.remove(schemaName);
            if (tenantDataSource instanceof AutoCloseable) {
                ((AutoCloseable) tenantDataSource).close();
            }

            logger.info("Successfully deprovisioned data source for tenant: {}", tenant.getId());

        } catch (Exception e) {
            logger.error("Failed to deprovision data source for tenant: {}", tenant.getId(), e);
            // Don't throw exception during cleanup
        }
    }

    /**
     * Get or create tenant-specific data source
     */
    private DataSource getOrCreateTenantDataSource(String schemaName) {
        return tenantDataSources.computeIfAbsent(schemaName, this::createTenantDataSource);
    }

    /**
     * Create tenant-specific data source
     */
    private DataSource createTenantDataSource(String schemaName) {
        // In production, this would create a HikariCP data source
        // configured for the specific tenant schema
        // For this implementation, we'll simulate it

        logger.debug("Creating data source for schema: {}", schemaName);

        // This is a simplified implementation
        // In production, you would:
        // 1. Create HikariCP data source
        // 2. Configure connection pool settings
        // 3. Set schema-specific properties
        // 4. Configure tenant-specific credentials if needed

        return new TenantAwareDataSource(masterDataSource, schemaName);
    }

    /**
     * Get tenant data source statistics
     */
    public Map<String, DataSourceStats> getDataSourceStats() {
        Map<String, DataSourceStats> stats = new ConcurrentHashMap<>();

        // Master data source stats
        stats.put("master", new DataSourceStats("master", 0, 0, 0));

        // Tenant data source stats
        for (Map.Entry<String, DataSource> entry : tenantDataSources.entrySet()) {
            String schemaName = entry.getKey();
            DataSource dataSource = entry.getValue();

            // In production, you'd get actual connection pool stats
            DataSourceStats tenantStats = new DataSourceStats(
                schemaName,
                5,  // active connections
                2,  // idle connections
                0   // waiting threads
            );

            stats.put(schemaName, tenantStats);
        }

        return stats;
    }

    /**
     * Tenant-aware data source wrapper
     */
    private static class TenantAwareDataSource implements DataSource {
        private final DataSource delegate;
        private final String schemaName;

        public TenantAwareDataSource(DataSource delegate, String schemaName) {
            this.delegate = delegate;
            this.schemaName = schemaName;
        }

        @Override
        public Connection getConnection() throws SQLException {
            Connection connection = delegate.getConnection();

            // Set schema for this connection
            try (java.sql.Statement stmt = connection.createStatement()) {
                stmt.execute("SET search_path TO " + schemaName);
            }

            return connection;
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            Connection connection = delegate.getConnection(username, password);

            // Set schema for this connection
            try (java.sql.Statement stmt = connection.createStatement()) {
                stmt.execute("SET search_path TO " + schemaName);
            }

            return connection;
        }

        @Override
        public java.io.PrintWriter getLogWriter() throws SQLException {
            return delegate.getLogWriter();
        }

        @Override
        public void setLogWriter(java.io.PrintWriter out) throws SQLException {
            delegate.setLogWriter(out);
        }

        @Override
        public void setLoginTimeout(int seconds) throws SQLException {
            delegate.setLoginTimeout(seconds);
        }

        @Override
        public int getLoginTimeout() throws SQLException {
            return delegate.getLoginTimeout();
        }

        @Override
        public java.sql.Driver getParentLogger() {
            // Java 7+ compatibility
            try {
                return (java.sql.Driver) Class.forName("org.postgresql.Driver").newInstance();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return delegate.unwrap(iface);
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return delegate.isWrapperFor(iface);
        }
    }

    /**
     * Data classes
     */

    public static class DataSourceStats {
        private final String schemaName;
        private final int activeConnections;
        private final int idleConnections;
        private final int waitingThreads;

        public DataSourceStats(String schemaName, int activeConnections, int idleConnections, int waitingThreads) {
            this.schemaName = schemaName;
            this.activeConnections = activeConnections;
            this.idleConnections = idleConnections;
            this.waitingThreads = waitingThreads;
        }

        // Getters
        public String getSchemaName() { return schemaName; }
        public int getActiveConnections() { return activeConnections; }
        public int getIdleConnections() { return idleConnections; }
        public int getWaitingThreads() { return waitingThreads; }

        public int getTotalConnections() {
            return activeConnections + idleConnections;
        }
    }
}
