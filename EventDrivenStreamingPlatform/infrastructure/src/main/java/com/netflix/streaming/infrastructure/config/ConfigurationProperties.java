package com.netflix.streaming.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Externalized configuration properties for streaming platform.
 * 
 * In production, these values should come from:
 * - Kubernetes ConfigMaps for non-sensitive configuration
 * - Kubernetes Secrets or external secret management (Vault, AWS Secrets Manager) for sensitive data
 * - Environment variables
 */
@Configuration
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {
    
    public static class Database {
        private String url = "";
        private String username = "";
        private String password = "";
        private int poolSize = 20;
        private long connectionTimeout = 30000;
        
        // Getters and setters
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public int getPoolSize() { return poolSize; }
        public void setPoolSize(int poolSize) { this.poolSize = poolSize; }
        public long getConnectionTimeout() { return connectionTimeout; }
        public void setConnectionTimeout(long connectionTimeout) { this.connectionTimeout = connectionTimeout; }
    }
    
    public static class Kafka {
        private String bootstrapServers = "localhost:9092";
        private String schemaRegistryUrl = "http://localhost:8081";
        private int replicationFactor = 3;
        private int partitions = 6;
        
        // Getters and setters
        public String getBootstrapServers() { return bootstrapServers; }
        public void setBootstrapServers(String bootstrapServers) { this.bootstrapServers = bootstrapServers; }
        public String getSchemaRegistryUrl() { return schemaRegistryUrl; }
        public void setSchemaRegistryUrl(String schemaRegistryUrl) { this.schemaRegistryUrl = schemaRegistryUrl; }
        public int getReplicationFactor() { return replicationFactor; }
        public void setReplicationFactor(int replicationFactor) { this.replicationFactor = replicationFactor; }
        public int getPartitions() { return partitions; }
        public void setPartitions(int partitions) { this.partitions = partitions; }
    }
    
    public static class Security {
        private String jwtSecret = "";
        private long jwtExpiration = 3600000;
        private String encryptionKey = "";
        
        // Getters and setters
        public String getJwtSecret() { return jwtSecret; }
        public void setJwtSecret(String jwtSecret) { this.jwtSecret = jwtSecret; }
        public long getJwtExpiration() { return jwtExpiration; }
        public void setJwtExpiration(long jwtExpiration) { this.jwtExpiration = jwtExpiration; }
        public String getEncryptionKey() { return encryptionKey; }
        public void setEncryptionKey(String encryptionKey) { this.encryptionKey = encryptionKey; }
    }
    
    public static class Compliance {
        private int auditLogRetentionDays = 2555; // 7 years
        private boolean enableGDPR = true;
        private boolean enableSOX = true;
        private boolean enableAuditLogging = true;
    }
    
    public static class Monitoring {
        private boolean enableTracing = true;
        private double tracingSampleRate = 1.0;
        private boolean metricsEnabled = true;
        private String logLevel = "INFO";
    }
    
    private Database database = new Database();
    private Kafka kafka = new Kafka();
    private Security security = new Security();
    private Compliance compliance = new Compliance();
    private Monitoring monitoring = new Monitoring();
    
    // Getters and setters
    public Database getDatabase() { return database; }
    public void setDatabase(Database database) { this.database = database; }
    public Kafka getKafka() { return kafka; }
    public void setKafka(Kafka kafka) { this.kafka = kafka; }
    public Security getSecurity() { return security; }
    public void setSecurity(Security security) { this.security = security; }
    public Compliance getCompliance() { return compliance; }
    public void setCompliance(Compliance compliance) { this.compliance = compliance; }
    public Monitoring getMonitoring() { return monitoring; }
    public void setMonitoring(Monitoring monitoring) { this.monitoring = monitoring; }
}

