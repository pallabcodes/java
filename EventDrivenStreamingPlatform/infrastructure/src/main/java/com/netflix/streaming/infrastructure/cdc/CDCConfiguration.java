package com.netflix.streaming.infrastructure.cdc;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Change Data Capture configuration properties.
 */
@Configuration
@ConfigurationProperties(prefix = "app.cdc")
public class CDCConfiguration {
    
    private boolean enabled = true;
    private int pollIntervalMs = 5000;
    private int batchSize = 100;
    private boolean useTriggers = true;
    private boolean useOutbox = true;
    private boolean useDebezium = false;
    private String debeziumTopic = "debezium.public.playback_sessions";

    // Getters and setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public int getPollIntervalMs() { return pollIntervalMs; }
    public void setPollIntervalMs(int pollIntervalMs) { this.pollIntervalMs = pollIntervalMs; }
    
    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
    
    public boolean isUseTriggers() { return useTriggers; }
    public void setUseTriggers(boolean useTriggers) { this.useTriggers = useTriggers; }
    
    public boolean isUseOutbox() { return useOutbox; }
    public void setUseOutbox(boolean useOutbox) { this.useOutbox = useOutbox; }
    
    public boolean isUseDebezium() { return useDebezium; }
    public void setUseDebezium(boolean useDebezium) { this.useDebezium = useDebezium; }
    
    public String getDebeziumTopic() { return debeziumTopic; }
    public void setDebeziumTopic(String debeziumTopic) { this.debeziumTopic = debeziumTopic; }
}

