package com.example.outbound;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Component
public class PaymentClient {
    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;

    public PaymentClient(WebClient.Builder builder, CircuitBreakerRegistry registry) {
        this.webClient = builder.baseUrl("http://localhost:8082").build();
        this.circuitBreaker = registry.circuitBreaker("payments");
    }

    public Mono<String> ping() {
        return Mono.defer(() -> webClient.get().uri("/ping").retrieve().bodyToMono(String.class).timeout(Duration.ofMillis(300)))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorResume(TimeoutException.class, e -> Mono.just("timeout"))
                .onErrorResume(CallNotPermittedException.class, e -> Mono.just("open"));
    }
}


