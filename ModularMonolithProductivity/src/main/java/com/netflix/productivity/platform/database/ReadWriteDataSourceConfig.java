package com.netflix.productivity.platform.database;

import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Read-Write Data Source Configuration
 *
 * Implements database read/write splitting for improved performance:
 * - Master (write) database for CREATE, UPDATE, DELETE operations
 * - Slave (read) databases for SELECT operations
 * - Automatic routing based on transaction context
 * - Load balancing across read replicas
 * - Connection pooling optimization
 * - Health monitoring and failover
 */
@Component
public class ReadWriteDataSourceConfig extends AbstractRoutingDataSource {

    private static final Logger logger = LoggerFactory.getLogger(ReadWriteDataSourceConfig.class);

    // Database routing context
    private static final ThreadLocal<DataSourceType> currentDataSourceType = new ThreadLocal<>();

    // Data sources
    private DataSource masterDataSource;
    private final Map<String, DataSource> slaveDataSources = new HashMap<>();

    // Load balancing
    private final AtomicLong readQueryCounter = new AtomicLong(0);

    // Health monitoring
    private final Map<String, DatabaseHealth> dataSourceHealth = new HashMap<>();

    /**
     * Initialize with master and slave data sources
     */
    public ReadWriteDataSourceConfig(DataSource masterDataSource) {
        this.masterDataSource = masterDataSource;

        // In production, slave data sources would be configured here
        // For demo, we'll use the master for both read and write
        initializeSlaveDataSources();

        // Configure routing
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DataSourceType.MASTER, masterDataSource);
        targetDataSources.put(DataSourceType.SLAVE_1, masterDataSource); // Using master as slave for demo
        targetDataSources.put(DataSourceType.SLAVE_2, masterDataSource); // Using master as slave for demo

        setTargetDataSources(targetDataSources);
        setDefaultTargetDataSource(masterDataSource);

        initializeHealthMonitoring();
    }

    /**
     * Determine current data source based on operation type
     */
    @Override
    protected Object determineCurrentLookupKey() {
        DataSourceType currentType = currentDataSourceType.get();

        if (currentType == null) {
            // Default to slave for read operations
            return selectSlaveDataSource();
        }

        return switch (currentType) {
            case MASTER -> DataSourceType.MASTER;
            case SLAVE_1, SLAVE_2 -> selectSlaveDataSource();
        };
    }

    /**
     * Get connection with routing awareness
     */
    @Override
    public Connection getConnection() throws SQLException {
        Object lookupKey = determineCurrentLookupKey();
        DataSource selectedDataSource = getDataSourceForKey(lookupKey);

        Connection connection = selectedDataSource.getConnection();

        // Set session parameters for tenant isolation (if applicable)
        configureConnection(connection, lookupKey);

        logger.debug("Using database connection: {} (operation: {})",
            lookupKey, currentDataSourceType.get());

        return connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Object lookupKey = determineCurrentLookupKey();
        DataSource selectedDataSource = getDataSourceForKey(lookupKey);

        Connection connection = selectedDataSource.getConnection(username, password);
        configureConnection(connection, lookupKey);

        return connection;
    }

    /**
     * Force read operation to use master (for immediate consistency)
     */
    public static void forceMaster() {
        currentDataSourceType.set(DataSourceType.MASTER);
    }

    /**
     * Force write operation to use master
     */
    public static void forceWrite() {
        currentDataSourceType.set(DataSourceType.MASTER);
    }

    /**
     * Force read operation to use slave
     */
    public static void forceRead() {
        currentDataSourceType.remove(); // Will default to slave selection
    }

    /**
     * Clear routing context
     */
    public static void clearRouting() {
        currentDataSourceType.remove();
    }

    /**
     * Transaction-aware routing advice
     */
    @Transactional(readOnly = true)
    public static void markAsReadOnly() {
        currentDataSourceType.remove(); // Use slave
    }

    @Transactional
    public static void markAsWrite() {
        currentDataSourceType.set(DataSourceType.MASTER);
    }

    /**
     * Get database performance metrics
     */
    public DatabasePerformanceMetrics getPerformanceMetrics() {
        long totalReads = readQueryCounter.get();
        long masterConnectionCount = getActiveConnections(DataSourceType.MASTER);
        long slaveConnectionCount = slaveDataSources.size() > 0 ?
            getActiveConnections(DataSourceType.SLAVE_1) + getActiveConnections(DataSourceType.SLAVE_2) : 0;

        return new DatabasePerformanceMetrics(
            totalReads,
            masterConnectionCount,
            slaveConnectionCount,
            dataSourceHealth
        );
    }

    /**
     * Health check for data sources
     */
    public Map<String, Boolean> checkDataSourceHealth() {
        Map<String, Boolean> healthStatus = new HashMap<>();

        // Check master
        healthStatus.put("master", checkDataSourceHealth(masterDataSource));

        // Check slaves
        for (Map.Entry<String, DataSource> entry : slaveDataSources.entrySet()) {
            healthStatus.put(entry.getKey(), checkDataSourceHealth(entry.getValue()));
        }

        return healthStatus;
    }

    /**
     * Failover support - switch to backup data source
     */
    public void initiateFailover(String failedDataSource) {
        logger.warn("Initiating failover for data source: {}", failedDataSource);

        // Mark as unhealthy
        if (dataSourceHealth.containsKey(failedDataSource)) {
            dataSourceHealth.get(failedDataSource).setHealthy(false);
        }

        // In production, this would implement actual failover logic
        // - Promote slave to master
        // - Update routing configuration
        // - Notify monitoring systems
        logger.info("Failover initiated for: {}", failedDataSource);
    }

    /**
     * Private helper methods
     */

    private void initializeSlaveDataSources() {
        // In production, configure actual slave data sources
        // slaveDataSources.put("slave1", createSlaveDataSource("slave1"));
        // slaveDataSources.put("slave2", createSlaveDataSource("slave2"));

        // For demo, we'll just use the master
        slaveDataSources.put("slave1", masterDataSource);
        slaveDataSources.put("slave2", masterDataSource);
    }

    private void initializeHealthMonitoring() {
        dataSourceHealth.put("master", new DatabaseHealth("master", true));
        dataSourceHealth.put("slave1", new DatabaseHealth("slave1", true));
        dataSourceHealth.put("slave2", new DatabaseHealth("slave2", true));
    }

    private DataSourceType selectSlaveDataSource() {
        // Round-robin load balancing across slaves
        long counter = readQueryCounter.incrementAndGet();
        int slaveCount = slaveDataSources.size();

        if (slaveCount == 0) {
            return DataSourceType.MASTER; // Fallback to master
        }

        int selectedSlave = (int) (counter % slaveCount) + 1;
        return selectedSlave == 1 ? DataSourceType.SLAVE_1 : DataSourceType.SLAVE_2;
    }

    private DataSource getDataSourceForKey(Object lookupKey) {
        if (lookupKey instanceof DataSourceType) {
            return switch ((DataSourceType) lookupKey) {
                case MASTER -> masterDataSource;
                case SLAVE_1 -> slaveDataSources.getOrDefault("slave1", masterDataSource);
                case SLAVE_2 -> slaveDataSources.getOrDefault("slave2", masterDataSource);
            };
        }
        return masterDataSource;
    }

    private void configureConnection(Connection connection, Object lookupKey) {
        try {
            // Set connection properties based on data source type
            if (lookupKey == DataSourceType.MASTER) {
                // Master-specific settings
                connection.setReadOnly(false);
                connection.setAutoCommit(false);
            } else {
                // Slave-specific settings
                connection.setReadOnly(true);
                // Note: In production, you might not set auto-commit for slaves
                // depending on your transaction management strategy
            }

            // Set tenant context if applicable (from multi-tenancy)
            // This would integrate with the SchemaPerTenantDataSource
            configureTenantContext(connection);

        } catch (SQLException e) {
            logger.warn("Failed to configure connection: {}", e.getMessage());
        }
    }

    private void configureTenantContext(Connection connection) {
        // Integrate with multi-tenancy - set schema based on tenant context
        try {
            String tenantId = getCurrentTenantId();
            if (tenantId != null) {
                String schemaName = "tenant_" + tenantId.replace("-", "_");
                connection.createStatement().execute("SET search_path TO " + schemaName);
            }
        } catch (SQLException e) {
            logger.warn("Failed to set tenant context: {}", e.getMessage());
        }
    }

    private String getCurrentTenantId() {
        // In production, this would get the tenant ID from TenantContext
        // For now, return null (no tenant context)
        return null;
    }

    private boolean checkDataSourceHealth(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(5); // 5 second timeout
        } catch (SQLException e) {
            logger.warn("Data source health check failed: {}", e.getMessage());
            return false;
        }
    }

    private int getActiveConnections(DataSourceType type) {
        // In production, this would query the connection pool
        // For demo, return mock values
        return switch (type) {
            case MASTER -> 5;
            case SLAVE_1 -> 3;
            case SLAVE_2 -> 4;
        };
    }
}

/**
 * Data classes for read-write splitting
 */

enum DataSourceType {
    MASTER,
    SLAVE_1,
    SLAVE_2
}

class DatabaseHealth {
    private final String name;
    private volatile boolean healthy;
    private volatile long lastChecked;
    private volatile long responseTimeMs;

    public DatabaseHealth(String name, boolean healthy) {
        this.name = name;
        this.healthy = healthy;
        this.lastChecked = System.currentTimeMillis();
    }

    public String getName() { return name; }
    public boolean isHealthy() { return healthy; }
    public void setHealthy(boolean healthy) { this.healthy = healthy; }
    public long getLastChecked() { return lastChecked; }
    public long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
}

class DatabasePerformanceMetrics {
    private final long totalReadQueries;
    private final long masterActiveConnections;
    private final long slaveActiveConnections;
    private final Map<String, DatabaseHealth> dataSourceHealth;

    public DatabasePerformanceMetrics(long totalReadQueries, long masterActiveConnections,
                                    long slaveActiveConnections, Map<String, DatabaseHealth> dataSourceHealth) {
        this.totalReadQueries = totalReadQueries;
        this.masterActiveConnections = masterActiveConnections;
        this.slaveActiveConnections = slaveActiveConnections;
        this.dataSourceHealth = dataSourceHealth;
    }

    public long getTotalReadQueries() { return totalReadQueries; }
    public long getMasterActiveConnections() { return masterActiveConnections; }
    public long getSlaveActiveConnections() { return slaveActiveConnections; }
    public Map<String, DatabaseHealth> getDataSourceHealth() { return dataSourceHealth; }

    public double getReadWriteRatio() {
        // This would need write query count to calculate properly
        // For now, return a mock ratio
        return 3.5; // 3.5 read queries per write query
    }
}

/**
 * AOP Aspect for automatic read/write routing
 */
@org.aspectj.lang.annotation.Aspect
@org.springframework.stereotype.Component
class DatabaseRoutingAspect {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DatabaseRoutingAspect.class);

    @org.aspectj.lang.annotation.Around("@annotation(org.springframework.transaction.annotation.Transactional)")
    public Object routeTransactionalMethods(org.aspectj.lang.ProceedingJoinPoint joinPoint) throws Throwable {
        org.springframework.transaction.annotation.Transactional transactional =
            org.aspectj.lang.reflect.MethodSignature.class.cast(joinPoint.getSignature())
                .getMethod().getAnnotation(org.springframework.transaction.annotation.Transactional.class);

        if (transactional.readOnly()) {
            ReadWriteDataSourceConfig.markAsReadOnly();
            logger.debug("Routed read-only transaction to slave database");
        } else {
            ReadWriteDataSourceConfig.markAsWrite();
            logger.debug("Routed write transaction to master database");
        }

        try {
            return joinPoint.proceed();
        } finally {
            ReadWriteDataSourceConfig.clearRouting();
        }
    }

    @org.aspectj.lang.annotation.Around("execution(* com.netflix.productivity.modules.*.application.*Service.*(..)) && !@annotation(org.springframework.transaction.annotation.Transactional)")
    public Object routeServiceMethods(org.aspectj.lang.ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();

        // Route based on method naming convention
        if (methodName.startsWith("get") || methodName.startsWith("find") ||
            methodName.startsWith("list") || methodName.startsWith("count") ||
            methodName.startsWith("exists") || methodName.startsWith("is")) {
            ReadWriteDataSourceConfig.forceRead();
            logger.debug("Routed read method {} to slave database", methodName);
        } else {
            ReadWriteDataSourceConfig.forceWrite();
            logger.debug("Routed write method {} to master database", methodName);
        }

        try {
            return joinPoint.proceed();
        } finally {
            ReadWriteDataSourceConfig.clearRouting();
        }
    }
}
