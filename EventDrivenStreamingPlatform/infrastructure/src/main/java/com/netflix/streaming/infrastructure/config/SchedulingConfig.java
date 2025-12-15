package com.netflix.streaming.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Scheduling configuration for background tasks.
 * Enables @Scheduled annotations for tasks like consumer lag monitoring.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}

