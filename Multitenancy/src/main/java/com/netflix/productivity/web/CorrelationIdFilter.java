package com.netflix.productivity.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.Span;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";
    public static final String MDC_TENANT_KEY = "tenantId";
    public static final String MDC_USER_KEY = "userId";

    private final Tracer tracer;

    public CorrelationIdFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put(MDC_KEY, correlationId);
        Span span = tracer.currentSpan();
        if (span != null) {
            span.tag("correlationId", correlationId);
        }
        String tenantId = request.getHeader("X-Tenant-ID");
        if (tenantId != null && !tenantId.isBlank()) {
            MDC.put(MDC_TENANT_KEY, tenantId);
            if (span != null) { span.tag("tenantId", tenantId); }
        }
        String userId = request.getHeader("X-User-Id");
        if (userId != null && !userId.isBlank()) {
            MDC.put(MDC_USER_KEY, userId);
            if (span != null) { span.tag("userId", userId); }
        }
        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
            MDC.remove(MDC_TENANT_KEY);
            MDC.remove(MDC_USER_KEY);
        }
    }
}


