package com.netflix.productivity.config;

import com.netflix.archaius.api.Config;
import com.netflix.archaius.api.Property;
import com.netflix.archaius.api.config.CompositeConfig;
import com.netflix.archaius.api.config.SettableConfig;
import com.netflix.archaius.api.exceptions.ConfigException;
import com.netflix.archaius.config.DefaultCompositeConfig;
import com.netflix.archaius.config.DefaultSettableConfig;
import com.netflix.archaius.config.EnvironmentConfig;
import com.netflix.archaius.config.SystemConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class DynamicConfigService {
    
    private CompositeConfig compositeConfig;
    private SettableConfig settableConfig;
    
    @PostConstruct
    public void initialize() {
        try {
            // Create a composite configuration with multiple sources
            compositeConfig = new DefaultCompositeConfig();
            settableConfig = new DefaultSettableConfig();
            
            // Add configuration sources in order of precedence
            compositeConfig.addConfig("system", SystemConfig.INSTANCE);
            compositeConfig.addConfig("environment", EnvironmentConfig.INSTANCE);
            compositeConfig.addConfig("runtime", settableConfig);
            
            // Add polling configuration for external sources
            addPollingConfig();
            
            log.info("Dynamic configuration initialized successfully");
        } catch (ConfigException e) {
            log.error("Failed to initialize dynamic configuration", e);
            throw new RuntimeException("Configuration initialization failed", e);
        }
    }
    
    private void addPollingConfig() {
        // Add polling configuration for external config sources
        // This would typically connect to external configuration services
        // like Consul, etcd, or AWS Parameter Store
        log.info("Polling configuration added");
    }
    
    @Bean
    @Primary
    public Config getConfig() {
        return compositeConfig;
    }
    
    @Bean
    public SettableConfig getSettableConfig() {
        return settableConfig;
    }
    
    // Dynamic property accessors
    public String getString(String key, String defaultValue) {
        return compositeConfig.getString(key, defaultValue);
    }
    
    public Integer getInteger(String key, Integer defaultValue) {
        return compositeConfig.getInteger(key, defaultValue);
    }
    
    public Long getLong(String key, Long defaultValue) {
        return compositeConfig.getLong(key, defaultValue);
    }
    
    public Boolean getBoolean(String key, Boolean defaultValue) {
        return compositeConfig.getBoolean(key, defaultValue);
    }
    
    public Double getDouble(String key, Double defaultValue) {
        return compositeConfig.getDouble(key, defaultValue);
    }
    
    // Dynamic property with change listeners
    public Property<String> getStringProperty(String key, String defaultValue) {
        return compositeConfig.getStringProperty(key, defaultValue);
    }
    
    public Property<Integer> getIntegerProperty(String key, Integer defaultValue) {
        return compositeConfig.getIntegerProperty(key, defaultValue);
    }
    
    public Property<Long> getLongProperty(String key, Long defaultValue) {
        return compositeConfig.getLongProperty(key, defaultValue);
    }
    
    public Property<Boolean> getBooleanProperty(String key, Boolean defaultValue) {
        return compositeConfig.getBooleanProperty(key, defaultValue);
    }
    
    public Property<Double> getDoubleProperty(String key, Double defaultValue) {
        return compositeConfig.getDoubleProperty(key, defaultValue);
    }
    
    // Runtime configuration updates
    public void setProperty(String key, String value) {
        settableConfig.setProperty(key, value);
        log.info("Updated configuration property {} = {}", key, value);
    }
    
    public void setProperty(String key, Integer value) {
        settableConfig.setProperty(key, value);
        log.info("Updated configuration property {} = {}", key, value);
    }
    
    public void setProperty(String key, Long value) {
        settableConfig.setProperty(key, value);
        log.info("Updated configuration property {} = {}", key, value);
    }
    
    public void setProperty(String key, Boolean value) {
        settableConfig.setProperty(key, value);
        log.info("Updated configuration property {} = {}", key, value);
    }
    
    public void setProperty(String key, Double value) {
        settableConfig.setProperty(key, value);
        log.info("Updated configuration property {} = {}", key, value);
    }
    
    // Configuration validation
    public boolean validateConfiguration() {
        try {
            // Validate critical configuration properties
            String[] criticalProps = {
                "app.jwt.secret",
                "app.database.url",
                "app.redis.host"
            };
            
            for (String prop : criticalProps) {
                if (compositeConfig.getString(prop) == null) {
                    log.error("Critical configuration property {} is missing", prop);
                    return false;
                }
            }
            
            log.info("Configuration validation passed");
            return true;
        } catch (Exception e) {
            log.error("Configuration validation failed", e);
            return false;
        }
    }
}