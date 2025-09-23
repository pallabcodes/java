package com.netflix.productivity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

@Configuration
public class WebConfig {

    @Bean
    public PageableHandlerMethodArgumentResolverCustomizer pageableCustomizer() {
        return p -> {
            p.setMaxPageSize(100);
            p.setOneIndexedParameters(false);
            p.setFallbackPageable(org.springframework.data.domain.PageRequest.of(0, 20));
        };
    }
}


