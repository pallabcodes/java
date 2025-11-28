package com.netflix.streaming.analytics.query;

import com.netflix.streaming.analytics.processor.RealTimeAnalyticsProcessor;
import org.springframework.stereotype.Service;

/**
 * Service for querying real-time metrics.
 * Acts as a facade for real-time analytics data.
 */
@Service
public class RealTimeMetricsService {

    private final RealTimeAnalyticsProcessor realTimeProcessor;

    public RealTimeMetricsService(RealTimeAnalyticsProcessor realTimeProcessor) {
        this.realTimeProcessor = realTimeProcessor;
    }

    public RealTimeAnalyticsProcessor.RealTimeMetrics getRealTimeMetrics() {
        return realTimeProcessor.getRealTimeMetrics();
    }
}