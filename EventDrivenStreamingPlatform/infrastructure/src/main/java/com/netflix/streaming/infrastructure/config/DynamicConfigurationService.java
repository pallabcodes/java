package com.netflix.streaming.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Dynamic Configuration Service.
 *
 * Provides runtime configuration updates and feature flags.
 * Supports environment-specific overrides and gradual rollouts.
 */
@Service
public class DynamicConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(DynamicConfigurationService.class);

    private final Map<String, Object> configurations = new ConcurrentHashMap<>();
    private final Map<String, FeatureFlag> featureFlags = new ConcurrentHashMap<>();

    // Static configuration values (could be loaded from database/external source)
    @Value("${app.dynamic.config.enabled:true}")
    private boolean dynamicConfigEnabled;

    @Value("${app.dynamic.config.refresh.interval:300000}") // 5 minutes
    private long refreshIntervalMs;

    public DynamicConfigurationService() {
        // Initialize default feature flags
        initializeDefaultFeatureFlags();

        // Initialize default configurations
        initializeDefaultConfigurations();
    }

    /**
     * Get configuration value with type safety.
     */
    @SuppressWarnings("unchecked")
    public <T> T getConfig(String key, T defaultValue) {
        Object value = configurations.get(key);
        if (value == null) {
            return defaultValue;
        }

        try {
            return (T) value;
        } catch (ClassCastException e) {
            logger.warn("Configuration type mismatch for key: {}, returning default", key);
            return defaultValue;
        }
    }

    /**
     * Set configuration value.
     */
    public void setConfig(String key, Object value) {
        if (!dynamicConfigEnabled) {
            logger.warn("Dynamic configuration is disabled, ignoring config update: {}", key);
            return;
        }

        Object oldValue = configurations.put(key, value);
        logger.info("Updated configuration: {} = {} (was: {})", key, value, oldValue);

        // Notify listeners (could be implemented with events)
        notifyConfigChange(key, oldValue, value);
    }

    /**
     * Check if feature flag is enabled.
     */
    public boolean isFeatureEnabled(String featureName) {
        FeatureFlag flag = featureFlags.get(featureName);
        return flag != null && flag.isEnabled();
    }

    /**
     * Check if feature is enabled for specific user/client.
     */
    public boolean isFeatureEnabledFor(String featureName, String userId) {
        FeatureFlag flag = featureFlags.get(featureName);
        if (flag == null) {
            return false;
        }

        // Check percentage rollout
        if (flag.getRolloutPercentage() < 100) {
            int userHash = Math.abs(userId.hashCode()) % 100;
            if (userHash >= flag.getRolloutPercentage()) {
                return false;
            }
        }

        return flag.isEnabled();
    }

    /**
     * Set feature flag.
     */
    public void setFeatureFlag(String featureName, boolean enabled) {
        featureFlags.compute(featureName, (k, v) -> {
            if (v == null) {
                return new FeatureFlag(featureName, enabled, 100);
            } else {
                v.setEnabled(enabled);
                return v;
            }
        });

        logger.info("Updated feature flag: {} = {}", featureName, enabled);
    }

    /**
     * Set feature flag with rollout percentage.
     */
    public void setFeatureFlag(String featureName, boolean enabled, int rolloutPercentage) {
        FeatureFlag flag = new FeatureFlag(featureName, enabled, rolloutPercentage);
        featureFlags.put(featureName, flag);

        logger.info("Updated feature flag with rollout: {} = {} ({}%)",
            featureName, enabled, rolloutPercentage);
    }

    /**
     * Get all configurations.
     */
    public Map<String, Object> getAllConfigurations() {
        return new ConcurrentHashMap<>(configurations);
    }

    /**
     * Get all feature flags.
     */
    public Map<String, FeatureFlag> getAllFeatureFlags() {
        return new ConcurrentHashMap<>(featureFlags);
    }

    /**
     * Refresh configurations from external source.
     * This would typically be called by a scheduled task.
     */
    public void refreshConfigurations() {
        if (!dynamicConfigEnabled) {
            return;
        }

        try {
            // In a real implementation, this would fetch from:
            // - Database
            // - Configuration service (Consul, etcd, etc.)
            // - External API
            // - Feature flag service (LaunchDarkly, etc.)

            logger.debug("Refreshing dynamic configurations");

            // Example: Update some configurations
            setConfig("api.rate.limit.enabled", true);
            setConfig("cdc.enabled", true);
            setConfig("backpressure.enabled", true);

        } catch (Exception e) {
            logger.error("Failed to refresh configurations", e);
        }
    }

    /**
     * Initialize default feature flags.
     */
    private void initializeDefaultFeatureFlags() {
        // Core features (always enabled)
        setFeatureFlag("api.analytics", true, 100);
        setFeatureFlag("cdc.triggers", true, 100);
        setFeatureFlag("backpressure.filter", true, 100);

        // Experimental features (gradual rollout)
        setFeatureFlag("advanced.metrics", true, 50); // 50% rollout
        setFeatureFlag("ai.insights", false, 0); // Disabled
        setFeatureFlag("real.time.analytics", true, 25); // 25% rollout
    }

    /**
     * Initialize default configurations.
     */
    private void initializeDefaultConfigurations() {
        // API configurations
        configurations.put("api.rate.limit.requests.per.minute", 1000);
        configurations.put("api.timeout.ms", 30000);
        configurations.put("api.compression.enabled", true);

        // CDC configurations
        configurations.put("cdc.batch.size", 100);
        configurations.put("cdc.poll.interval.ms", 5000);
        configurations.put("cdc.retry.attempts", 3);

        // Kafka configurations
        configurations.put("kafka.batch.size", 16384);
        configurations.put("kafka.linger.ms", 5);
        configurations.put("kafka.compression.type", "lz4");

        // Database configurations
        configurations.put("db.connection.pool.size", 20);
        configurations.put("db.connection.timeout.ms", 20000);
        configurations.put("db.idle.timeout.ms", 300000);
    }

    /**
     * Notify configuration change listeners.
     */
    private void notifyConfigChange(String key, Object oldValue, Object newValue) {
        // In a real implementation, this would:
        // - Publish configuration change event
        // - Update dependent services
        // - Trigger configuration reload
        // - Update metrics

        logger.debug("Configuration changed: {} from {} to {}", key, oldValue, newValue);
    }

    /**
     * Feature flag with rollout support.
     */
    public static class FeatureFlag {
        private final String name;
        private final AtomicBoolean enabled = new AtomicBoolean(false);
        private volatile int rolloutPercentage;

        public FeatureFlag(String name, boolean enabled, int rolloutPercentage) {
            this.name = name;
            this.enabled.set(enabled);
            this.rolloutPercentage = rolloutPercentage;
        }

        public String getName() { return name; }
        public boolean isEnabled() { return enabled.get(); }
        public void setEnabled(boolean enabled) { this.enabled.set(enabled); }
        public int getRolloutPercentage() { return rolloutPercentage; }
        public void setRolloutPercentage(int percentage) { this.rolloutPercentage = percentage; }
    }
}
