package com.netflix.productivity.logging;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter implements Filter {
    public static final String CORRELATION_ID = "X-Correlation-Id";
    public static final String TENANT_ID = "X-Tenant-ID";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest http = (HttpServletRequest) request;
        String corr = http.getHeader(CORRELATION_ID);
        if (corr == null || corr.isBlank()) {
            corr = UUID.randomUUID().toString();
        }
        String tenant = http.getHeader(TENANT_ID);
        try {
            MDC.put("correlationId", corr);
            if (tenant != null) MDC.put("tenantId", tenant);
            chain.doFilter(request, response);
        } finally {
            MDC.remove("correlationId");
            MDC.remove("tenantId");
        }
    }
}

