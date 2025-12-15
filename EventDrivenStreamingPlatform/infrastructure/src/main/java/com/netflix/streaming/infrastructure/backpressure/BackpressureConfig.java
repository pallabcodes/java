package com.netflix.streaming.infrastructure.backpressure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Backpressure configuration properties.
 */
@Configuration
@ConfigurationProperties(prefix = "app.backpressure")
public class BackpressureConfig {
    
    private int maxConcurrentRequests = 100;
    private int maxQueueSize = 50;
    private int retryAfterSeconds = 60;
    private boolean enabled = true;

    // Getters and setters
    public int getMaxConcurrentRequests() { return maxConcurrentRequests; }
    public void setMaxConcurrentRequests(int maxConcurrentRequests) { this.maxConcurrentRequests = maxConcurrentRequests; }
    
    public int getMaxQueueSize() { return maxQueueSize; }
    public void setMaxQueueSize(int maxQueueSize) { this.maxQueueSize = maxQueueSize; }
    
    public int getRetryAfterSeconds() { return retryAfterSeconds; }
    public void setRetryAfterSeconds(int retryAfterSeconds) { this.retryAfterSeconds = retryAfterSeconds; }
    
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}

