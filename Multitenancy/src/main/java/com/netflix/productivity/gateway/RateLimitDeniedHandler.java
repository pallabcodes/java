package com.netflix.productivity.gateway;

import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

@Component
public class RateLimitDeniedHandler {
    public Mono<Void> write429(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String correlationId = exchange.getRequest().getHeaders().getFirst(CorrelationFilter.CORRELATION_ID);
        String body = "{" +
                "\"error\":\"rate_limited\"," +
                "\"message\":\"Too many requests\"," +
                "\"correlationId\":\"" + (correlationId == null ? "" : correlationId) + "\"," +
                "\"timestamp\":\"" + OffsetDateTime.now().toString() + "\"}";
        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
        return exchange.getResponse().writeWith(Mono.just(bufferFactory.wrap(body.getBytes(StandardCharsets.UTF_8))));
    }
}

