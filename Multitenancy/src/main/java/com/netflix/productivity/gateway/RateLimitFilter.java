package com.netflix.productivity.gateway;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RateLimitFilter implements GlobalFilter {
    private final RateLimitDeniedHandler deniedHandler;

    public RateLimitFilter(RateLimitDeniedHandler deniedHandler) {
        this.deniedHandler = deniedHandler;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Object allowedAttr = exchange.getAttribute("resolvedRateLimiterAllowed");
        if (allowedAttr instanceof Boolean && !((Boolean) allowedAttr)) {
            return deniedHandler.write429(exchange);
        }
        HttpStatus status = exchange.getResponse().getStatusCode();
        if (status == HttpStatus.TOO_MANY_REQUESTS) {
            return deniedHandler.write429(exchange);
        }
        return chain.filter(exchange);
    }
}

