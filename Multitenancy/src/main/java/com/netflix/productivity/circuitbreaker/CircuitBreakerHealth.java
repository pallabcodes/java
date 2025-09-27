package com.netflix.productivity.circuitbreaker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CircuitBreakerHealth {
    private CircuitBreakerStatus webhookCircuitBreaker;
    private CircuitBreakerStatus externalApiCircuitBreaker;
    private CircuitBreakerStatus databaseCircuitBreaker;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CircuitBreakerStatus {
        private String groupKey;
        private boolean isOpen;
        private long requestCount;
        private long errorCount;
        private double errorPercentage;
        private String errorMessage;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CircuitBreakerConfig {
        private int requestVolumeThreshold;
        private int errorThresholdPercentage;
        private int sleepWindowInMilliseconds;
        private int executionTimeoutInMilliseconds;
        private int coreSize;
        private int maxQueueSize;
    }
}
