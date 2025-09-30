package com.netflix.productivity.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest req = exchange.getRequest();
        String method = req.getMethodValue();
        String path = req.getURI().getPath();
        String tenant = req.getHeaders().getFirst("X-Tenant-ID");
        String corr = req.getHeaders().getFirst(CorrelationFilter.CORRELATION_ID);
        log.info("gw request method={} path={} tenant={} corr={}", method, path, tenant, corr);
        long start = System.nanoTime();
        return chain.filter(exchange)
                .doFinally(s -> {
                    long ms = (System.nanoTime() - start) / 1_000_000;
                    int status = exchange.getResponse().getStatusCode() == null ? 0 : exchange.getResponse().getStatusCode().value();
                    log.info("gw response status={} path={} tookMs={} corr={}", status, path, ms, corr);
                });
    }

    @Override
    public int getOrder() {
        return 0;
    }
}

