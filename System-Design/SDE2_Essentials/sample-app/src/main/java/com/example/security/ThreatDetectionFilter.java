package com.example.security;

import com.example.monitoring.ThreatDetectionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Component
public class ThreatDetectionFilter extends OncePerRequestFilter {

    private final ThreatDetectionService threatDetectionService;

    public ThreatDetectionFilter(ThreatDetectionService threatDetectionService) {
        this.threatDetectionService = threatDetectionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String requestPath = request.getRequestURI();
        Map<String, String> headers = extractHeaders(request);

        // Analyze request for threats
        threatDetectionService.analyzeRequest(ipAddress, userAgent, requestPath, headers);

        filterChain.doFilter(request, response);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.put(headerName, headerValue);
        }

        return headers;
    }
}
