## Circuit Breaker

### Goals
* Fail fast to protect resources
* Prevent cascades and enable graceful degradation

### States
* Closed
* Open
* Half open

### Key settings
* Error rate threshold and sliding window
* Open state duration and half open probes
* Per endpoint and per dependency configuration

### Operability
* Metrics and alerts for open ratio and latency
* Fallbacks with clear semantics and idempotency

### Java reference with Resilience4j

```java
@Configuration
public class ResilienceConfig {
    @Bean
    public CircuitBreakerRegistry cbRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .slidingWindowType(SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(100)
            .waitDurationInOpenState(Duration.ofSeconds(10))
            .permittedNumberOfCallsInHalfOpenState(5)
            .recordException(e -> !(e instanceof ClientErrorException))
            .build();
        return CircuitBreakerRegistry.of(config);
    }
}
```

```java
@Component
public class PaymentClient {
    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;

    public PaymentClient(WebClient.Builder builder, CircuitBreakerRegistry registry) {
        this.webClient = builder.baseUrl("https://payments.example.com").build();
        this.circuitBreaker = registry.circuitBreaker("payments");
    }

    public Mono<Receipt> charge(PaymentRequest req) {
        Supplier<Mono<Receipt>> supplier = () -> webClient.post().uri("/v1/charge")
                .bodyValue(req)
                .retrieve()
                .bodyToMono(Receipt.class)
                .timeout(Duration.ofMillis(500));

        return Mono.defer(supplier).transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorResume(TimeoutException.class, e -> Mono.error(new DependencyTimeoutException()))
                .onErrorResume(CallNotPermittedException.class, e -> Mono.error(new DependencyUnavailableException()));
    }
}
```

### Review checklist
* Error classification defined and tested
* Timeouts and retries composed correctly with breaker
* Fallback behavior documented and monitored


