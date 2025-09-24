package com.netflix.productivity.web;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class HttpMetricsInterceptor implements HandlerInterceptor {

    private final MeterRegistry meterRegistry;

    public HttpMetricsInterceptor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    private static final String START_TIME = "httpStart";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME, System.nanoTime());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Object start = request.getAttribute(START_TIME);
        if (start instanceof Long) {
            long durationNs = System.nanoTime() - (Long) start;
            String route = request.getRequestURI();
            String method = request.getMethod();
            String status = Integer.toString(response.getStatus());
            String tenantId = MDC.get("tenantId");
            String errorCode = (String) request.getAttribute("errorCode");
            Timer.builder("http.server.requests.custom")
                    .description("HTTP server requests with tenant and codes")
                    .tags("method", method,
                            "uri", route,
                            "status", status,
                            "tenant", tenantId == null ? "unknown" : tenantId,
                            "errorCode", errorCode == null ? "none" : errorCode)
                    .register(meterRegistry)
                    .record(durationNs, java.util.concurrent.TimeUnit.NANOSECONDS);
        }
    }
}


