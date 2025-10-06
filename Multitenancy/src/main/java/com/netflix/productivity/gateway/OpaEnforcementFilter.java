package com.netflix.productivity.gateway;

import com.netflix.productivity.security.OpaPolicyClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpaEnforcementFilter implements GlobalFilter, Ordered {

    private final OpaPolicyClient opaPolicyClient;

    @Value("${app.opa.gateway-enabled:false}")
    private boolean gatewayEnabled;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!gatewayEnabled) return chain.filter(exchange);
        try {
            String path = exchange.getRequest().getPath().value();
            String method = exchange.getRequest().getMethodValue();
            String tenantId = exchange.getRequest().getHeaders().getFirst("X-Tenant-ID");
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");

            Map<String, Object> input = new HashMap<>();
            input.put("path", path);
            input.put("method", method);
            input.put("tenantId", tenantId);
            input.put("userId", userId);
            input.put("attributes", Map.of());

            boolean allow = opaPolicyClient.allow(input);
            if (!allow) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
        } catch (Exception e) {
            log.error("OPA enforcement error", e);
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -50; // before routing
    }
}
