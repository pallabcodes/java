package com.netflix.productivity.gateway;

import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationFilter implements GlobalFilter, Ordered {
    public static final String CORRELATION_ID = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        String finalCorrelationId = correlationId;
        return chain.filter(exchange.mutate()
                .request(exchange.getRequest().mutate().headers(httpHeaders -> httpHeaders.set(CORRELATION_ID, finalCorrelationId)).build())
                .response(exchange.getResponse())
                .build())
            .doFirst(() -> MDC.put(CORRELATION_ID, finalCorrelationId))
            .doOnSubscribe(s -> exchange.getResponse().getHeaders().set(CORRELATION_ID, finalCorrelationId))
            .doFinally(signalType -> MDC.remove(CORRELATION_ID));
    }

    @Override
    public int getOrder() {
        return -1000;
    }
}

