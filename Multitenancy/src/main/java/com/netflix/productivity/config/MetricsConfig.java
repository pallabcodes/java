package com.netflix.productivity.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcMetricsFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;

@Configuration
@EnableConfigurationProperties
public class MetricsConfig {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> commonMetricsCustomizer() {
        return registry -> {
            registry.config().commonTags("app", "netflix-productivity");
        };
    }
}


