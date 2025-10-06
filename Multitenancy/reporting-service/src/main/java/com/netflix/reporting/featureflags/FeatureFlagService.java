package com.netflix.reporting.featureflags;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class FeatureFlagService {

    private final Environment environment;

    public FeatureFlagService(Environment environment) {
        this.environment = environment;
    }

    public boolean isEnabled(String tenantId, String flagKey, boolean defaultValue) {
        String key = String.format("tenant.%s.flags.%s", tenantId, flagKey);
        String value = environment.getProperty(key);
        if (value == null) return defaultValue;
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1") || value.equalsIgnoreCase("on");
    }
}


