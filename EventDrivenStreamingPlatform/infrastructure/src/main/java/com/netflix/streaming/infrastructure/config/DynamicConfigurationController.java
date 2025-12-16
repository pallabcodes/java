package com.netflix.streaming.infrastructure.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Dynamic Configuration Controller.
 *
 * Provides REST endpoints for managing dynamic configurations and feature flags.
 * Supports runtime configuration updates without service restart.
 */
@RestController
@RequestMapping("/api/v1/config")
public class DynamicConfigurationController {

    private final DynamicConfigurationService configService;

    public DynamicConfigurationController(DynamicConfigurationService configService) {
        this.configService = configService;
    }

    /**
     * Get all configurations.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllConfigurations() {
        Map<String, Object> configs = configService.getAllConfigurations();
        return ResponseEntity.ok(configs);
    }

    /**
     * Get specific configuration.
     */
    @GetMapping("/{key}")
    public ResponseEntity<Object> getConfiguration(@PathVariable String key) {
        Object value = configService.getConfig(key, null);
        if (value == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(value);
    }

    /**
     * Set configuration value.
     */
    @PutMapping("/{key}")
    public ResponseEntity<Void> setConfiguration(@PathVariable String key, @RequestBody Object value) {
        configService.setConfig(key, value);
        return ResponseEntity.ok().build();
    }

    /**
     * Get all feature flags.
     */
    @GetMapping("/features")
    public ResponseEntity<Map<String, DynamicConfigurationService.FeatureFlag>> getAllFeatureFlags() {
        Map<String, DynamicConfigurationService.FeatureFlag> flags = configService.getAllFeatureFlags();
        return ResponseEntity.ok(flags);
    }

    /**
     * Check if feature is enabled.
     */
    @GetMapping("/features/{featureName}")
    public ResponseEntity<Map<String, Object>> checkFeatureFlag(@PathVariable String featureName) {
        boolean enabled = configService.isFeatureEnabled(featureName);
        Map<String, Object> response = Map.of(
            "feature", featureName,
            "enabled", enabled
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Check if feature is enabled for specific user.
     */
    @GetMapping("/features/{featureName}/users/{userId}")
    public ResponseEntity<Map<String, Object>> checkFeatureFlagForUser(
            @PathVariable String featureName,
            @PathVariable String userId) {

        boolean enabled = configService.isFeatureEnabledFor(featureName, userId);
        Map<String, Object> response = Map.of(
            "feature", featureName,
            "userId", userId,
            "enabled", enabled
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Enable/disable feature flag.
     */
    @PutMapping("/features/{featureName}")
    public ResponseEntity<Void> setFeatureFlag(
            @PathVariable String featureName,
            @RequestBody Map<String, Object> request) {

        Boolean enabled = (Boolean) request.get("enabled");
        Integer rolloutPercentage = (Integer) request.get("rolloutPercentage");

        if (rolloutPercentage != null) {
            configService.setFeatureFlag(featureName, enabled != null ? enabled : false, rolloutPercentage);
        } else {
            configService.setFeatureFlag(featureName, enabled != null ? enabled : false);
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Refresh configurations from external source.
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshConfigurations() {
        configService.refreshConfigurations();
        Map<String, String> response = Map.of(
            "status", "success",
            "message", "Configurations refreshed"
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get configuration status.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = Map.of(
            "configurations", configService.getAllConfigurations().size(),
            "featureFlags", configService.getAllFeatureFlags().size(),
            "timestamp", java.time.Instant.now()
        );
        return ResponseEntity.ok(status);
    }
}
