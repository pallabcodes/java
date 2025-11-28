package com.netflix.streaming.infrastructure.observability;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.AbstractRequestLoggingFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Comprehensive Logging Configuration.
 *
 * Provides Netflix-grade structured logging with:
 * - Correlation ID tracking across service boundaries
 * - Request/response logging
 * - MDC context propagation
 * - JSON structured logs
 */
@Configuration
public class LoggingConfig {

    /**
     * Request Correlation ID Filter
     * Generates and propagates correlation IDs for request tracing
     */
    @Bean
    public CorrelationIdFilter correlationIdFilter() {
        return new CorrelationIdFilter();
    }

    /**
     * Request/Response Logging Filter
     * Logs HTTP requests and responses with correlation IDs
     */
    @Bean
    public RequestResponseLoggingFilter requestResponseLoggingFilter(ObjectMapper objectMapper) {
        RequestResponseLoggingFilter filter = new RequestResponseLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(false); // Don't log sensitive data
        filter.setIncludeHeaders(false);
        filter.setBeforeMessagePrefix("REQUEST: ");
        filter.setAfterMessagePrefix("RESPONSE: ");
        return filter;
    }

    /**
     * Structured Logging Service
     * Provides utilities for consistent structured logging across services
     */
    @Bean
    public StructuredLoggingService structuredLoggingService(ObjectMapper objectMapper) {
        return new StructuredLoggingService(objectMapper);
    }

    /**
     * Correlation ID Filter Implementation
     */
    public static class CorrelationIdFilter extends OncePerRequestFilter {

        private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
        private static final String TENANT_ID_HEADER = "X-Tenant-ID";
        private static final String USER_ID_HEADER = "X-User-ID";

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {

            // Extract or generate correlation ID
            String correlationId = request.getHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.trim().isEmpty()) {
                correlationId = UUID.randomUUID().toString();
            }

            // Extract tenant and user context
            String tenantId = request.getHeader(TENANT_ID_HEADER);
            String userId = request.getHeader(USER_ID_HEADER);

            // Set MDC context for structured logging
            MDC.put("correlationId", correlationId);
            MDC.put("tenantId", tenantId != null ? tenantId : "default");
            MDC.put("userId", userId != null ? userId : "anonymous");
            MDC.put("requestMethod", request.getMethod());
            MDC.put("requestUri", request.getRequestURI());
            MDC.put("remoteAddr", request.getRemoteAddr());

            // Add correlation ID to response header
            response.setHeader(CORRELATION_ID_HEADER, correlationId);

            try {
                filterChain.doFilter(request, response);
            } finally {
                // Clean up MDC context
                MDC.clear();
            }
        }
    }

    /**
     * Structured Logging Service Implementation
     */
    public static class StructuredLoggingService {

        private final ObjectMapper objectMapper;

        public StructuredLoggingService(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        /**
         * Log business event with structured data
         */
        public void logBusinessEvent(String eventType, Object data) {
            try {
                String jsonData = objectMapper.writeValueAsString(data);
                org.slf4j.LoggerFactory.getLogger("business-events")
                    .info("Business event: type={}, data={}", eventType, jsonData);
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(StructuredLoggingService.class)
                    .error("Failed to log business event", e);
            }
        }

        /**
         * Log security event with structured data
         */
        public void logSecurityEvent(String eventType, String severity, Object data) {
            try {
                String jsonData = objectMapper.writeValueAsString(data);
                org.slf4j.LoggerFactory.getLogger("security-events")
                    .warn("Security event: type={}, severity={}, data={}", eventType, severity, jsonData);
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(StructuredLoggingService.class)
                    .error("Failed to log security event", e);
            }
        }

        /**
         * Log performance metrics with structured data
         */
        public void logPerformanceMetric(String metricName, Number value, java.util.Map<String, String> tags) {
            try {
                String tagsJson = objectMapper.writeValueAsString(tags);
                org.slf4j.LoggerFactory.getLogger("performance-metrics")
                    .info("Performance metric: name={}, value={}, tags={}", metricName, value, tagsJson);
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(StructuredLoggingService.class)
                    .error("Failed to log performance metric", e);
            }
        }

        /**
         * Log error with structured context
         */
        public void logError(String operation, String errorType, Throwable throwable, Object context) {
            try {
                String contextJson = context != null ? objectMapper.writeValueAsString(context) : "{}";
                org.slf4j.LoggerFactory.getLogger("error-events")
                    .error("Error occurred: operation={}, errorType={}, context={}, message={}",
                           operation, errorType, contextJson, throwable.getMessage(), throwable);
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(StructuredLoggingService.class)
                    .error("Failed to log error event", e);
            }
        }

        /**
         * Log audit event for compliance
         */
        public void logAuditEvent(String action, String resource, String outcome, Object details) {
            try {
                String detailsJson = objectMapper.writeValueAsString(details);
                org.slf4j.LoggerFactory.getLogger("audit-events")
                    .info("Audit event: action={}, resource={}, outcome={}, details={}",
                          action, resource, outcome, detailsJson);
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(StructuredLoggingService.class)
                    .error("Failed to log audit event", e);
            }
        }
    }
}