package com.netflix.productivity.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(prefix = "features.idempotency", name = "enabled", havingValue = "true")
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private final ConcurrentHashMap<String, Boolean> seenKeys = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (isMutating(request)) {
            String key = request.getHeader(IDEMPOTENCY_KEY_HEADER);
            if (key == null || key.isBlank()) {
                filterChain.doFilter(request, response);
                return;
            }
            boolean firstTime = seenKeys.putIfAbsent(composeKey(request, key), Boolean.TRUE) == null;
            if (!firstTime) {
                response.setStatus(HttpStatus.CONFLICT.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Duplicate idempotency key\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isMutating(HttpServletRequest request) {
        String method = request.getMethod();
        return Objects.equals(method, "POST") || Objects.equals(method, "PUT") || Objects.equals(method, "PATCH") || Objects.equals(method, "DELETE");
    }

    private String composeKey(HttpServletRequest request, String key) {
        return request.getRequestURI() + "|" + key;
    }
}
