package com.netflix.productivity.resilience;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResilienceHealth {
    private String circuitBreakerState;
    private int bulkheadAvailablePermits;
    private int threadPoolBulkheadAvailablePermits;
}
