package com.netflix.reporting.quotas;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TenantQuotaFilter implements Filter {

    private final TenantQuotaService tenantQuotaService;
    private final MeterRegistry meterRegistry;

    public TenantQuotaFilter(TenantQuotaService tenantQuotaService, MeterRegistry meterRegistry) {
        this.tenantQuotaService = tenantQuotaService;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String tenantId = req.getHeader("X-Tenant-ID");
        String method = req.getMethod();
        if (tenantId == null || tenantId.isBlank()) {
            chain.doFilter(request, response);
            return;
        }

        double cost = HttpMethod.GET.matches(method) ? 1.0 : 2.0;
        boolean allowed = tenantQuotaService.tryConsume(tenantId, cost);
        Tags tags = Tags.of(Tag.of("tenant", tenantId));
        meterRegistry.counter("tenant.requests", tags).increment();
        meterRegistry.counter("tenant.cost.units", tags).increment(cost);
        if (!allowed) {
            res.setStatus(429);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"quota exceeded\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}


