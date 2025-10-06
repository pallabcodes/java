package com.netflix.reporting.chaos;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ChaosFilter implements Filter {

    private final ChaosService chaosService;

    public ChaosFilter(ChaosService chaosService) {
        this.chaosService = chaosService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!chaosService.isEnabled()) {
            chain.doFilter(request, response);
            return;
        }
        long latency = chaosService.latencyMs();
        if (latency > 0) {
            try { Thread.sleep(latency); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        if (chaosService.shouldFail()) {
            HttpServletResponse res = (HttpServletResponse) response;
            res.setStatus(500);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"chaos injected failure\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}


