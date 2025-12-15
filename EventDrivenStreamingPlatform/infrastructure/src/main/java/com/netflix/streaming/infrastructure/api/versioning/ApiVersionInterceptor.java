package com.netflix.streaming.infrastructure.api.versioning;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor for API versioning and deprecation warnings.
 */
@Component
public class ApiVersionInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ApiVersionInterceptor.class);
    private static final String API_VERSION_HEADER = "X-API-Version";
    private static final String API_DEPRECATION_HEADER = "Deprecation";
    private static final String API_SUNSET_HEADER = "Sunset";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestPath = request.getRequestURI();
        
        // Extract version from path (/api/v1/... or /api/v2/...)
        String apiVersion = extractVersionFromPath(requestPath);
        
        if (apiVersion != null) {
            // Add version header
            response.setHeader(API_VERSION_HEADER, apiVersion);
            
            // Check if version is deprecated
            if (ApiVersioningConfig.ApiVersions.DEPRECATED != null && 
                apiVersion.equals(ApiVersioningConfig.ApiVersions.DEPRECATED)) {
                response.setHeader(API_DEPRECATION_HEADER, "true");
                response.setHeader(API_SUNSET_HEADER, "2025-12-31"); // Example sunset date
                logger.warn("Deprecated API version accessed: {} from {}", apiVersion, request.getRemoteAddr());
            }
            
            // Warn if using old version
            if (!apiVersion.equals(ApiVersioningConfig.ApiVersions.CURRENT)) {
                response.setHeader("X-API-Version-Warning", 
                    "You are using an older API version. Consider upgrading to " + 
                    ApiVersioningConfig.ApiVersions.CURRENT);
            }
        }
        
        return true;
    }

    /**
     * Extract API version from request path.
     */
    private String extractVersionFromPath(String path) {
        if (path == null) {
            return null;
        }
        
        // Match /api/v1/ or /api/v2/ pattern
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("/api/(v\\d+)/");
        java.util.regex.Matcher matcher = pattern.matcher(path);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }
}

