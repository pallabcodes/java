package com.netflix.streaming.infrastructure.api.versioning;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * API versioning configuration.
 * 
 * Supports:
 * - URL path versioning (/api/v1/, /api/v2/)
 * - Header-based versioning (Accept: application/vnd.api.v2+json)
 * - Deprecation warnings
 */
@Configuration
public class ApiVersioningConfig implements WebMvcConfigurer {

    /**
     * API version constants.
     */
    public static class ApiVersions {
        public static final String V1 = "v1";
        public static final String V2 = "v2";
        public static final String CURRENT = V1;
        public static final String DEPRECATED = null; // Set to version when deprecating
    }

    /**
     * API versioning interceptor for adding version headers.
     */
    @org.springframework.context.annotation.Bean
    public ApiVersionInterceptor apiVersionInterceptor() {
        return new ApiVersionInterceptor();
    }

    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        registry.addInterceptor(apiVersionInterceptor());
    }
}

