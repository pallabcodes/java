package com.netflix.reporting.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
public class RequestMetricsFilter implements Filter {

    private final MeterRegistry meterRegistry;

    public RequestMetricsFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();
        String method = req.getMethod();
        String tenant = req.getHeader("X-Tenant-ID");
        long start = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } finally {
            int status = res.getStatus();
            String statusStr = Integer.toString(status);

            // Rate
            Counter.builder("http.server.requests.count")
                    .tag("path", path)
                    .tag("method", method)
                    .tag("status", statusStr)
                    .tag("tenant", tenant == null ? "unknown" : tenant)
                    .register(meterRegistry)
                    .increment();

            // Errors (5xx)
            if (status >= 500) {
                Counter.builder("http.server.errors.count")
                        .tag("path", path)
                        .tag("method", method)
                        .tag("status", statusStr)
                        .tag("tenant", tenant == null ? "unknown" : tenant)
                        .register(meterRegistry)
                        .increment();
            }

            // Duration
            long end = System.nanoTime();
            Timer.builder("http.server.request.duration")
                    .description("server request duration")
                    .tag("path", path)
                    .tag("method", method)
                    .tag("status", statusStr)
                    .tag("tenant", tenant == null ? "unknown" : tenant)
                    .register(meterRegistry)
                    .record(Duration.ofNanos(end - start));
        }
    }
}


