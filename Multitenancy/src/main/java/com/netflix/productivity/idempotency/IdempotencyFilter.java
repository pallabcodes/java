package com.netflix.productivity.idempotency;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
public class IdempotencyFilter implements Filter {

    private final IdempotencyService idempotencyService;

    public IdempotencyFilter(IdempotencyService idempotencyService) {
        this.idempotencyService = idempotencyService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String method = req.getMethod();
        if (HttpMethod.POST.matches(method) || HttpMethod.PUT.matches(method) || HttpMethod.PATCH.matches(method)) {
            String key = req.getHeader("Idempotency-Key");
            String tenantId = req.getHeader("X-Tenant-ID");
            if (key == null || key.isBlank() || tenantId == null || tenantId.isBlank()) {
                res.setStatus(400);
                res.setContentType("application/json");
                res.getWriter().write("{\"error\":\"missing Idempotency-Key or X-Tenant-ID\"}");
                return;
            }
            boolean claimed = idempotencyService.claim(tenantId, key, Duration.ofHours(24));
            if (!claimed) {
                res.setStatus(409);
                res.setContentType("application/json");
                res.getWriter().write("{\"error\":\"duplicate idempotency key\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}


