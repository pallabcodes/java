package com.netflix.streaming.infrastructure.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResilienceServiceTest {

    @Mock
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Mock
    private RetryRegistry retryRegistry;

    @Mock
    private TimeLimiterRegistry timeLimiterRegistry;

    @Mock
    private CircuitBreaker circuitBreaker;

    @Mock
    private Retry retry;

    @Mock
    private TimeLimiter timeLimiter;

    private ResilienceService resilienceService;

    @BeforeEach
    void setUp() {
        when(circuitBreakerRegistry.circuitBreaker(anyString())).thenReturn(circuitBreaker);
        when(retryRegistry.retry(anyString())).thenReturn(retry);
        when(timeLimiterRegistry.timeLimiter(anyString())).thenReturn(timeLimiter);

        resilienceService = new ResilienceService(
            circuitBreakerRegistry,
            retryRegistry,
            timeLimiterRegistry
        );
    }

    @Test
    void shouldExecuteWithCircuitBreaker() {
        // Given
        Supplier<String> supplier = () -> "success";
        when(circuitBreaker.executeSupplier(any(Supplier.class))).thenReturn("success");

        // When
        String result = resilienceService.executeWithCircuitBreaker("test-circuit", supplier);

        // Then
        assertEquals("success", result);
        verify(circuitBreakerRegistry).circuitBreaker("test-circuit");
        verify(circuitBreaker).executeSupplier(any(Supplier.class));
    }

    @Test
    void shouldExecuteWithRetry() {
        // Given
        Supplier<String> supplier = () -> "success";
        when(retry.executeSupplier(any(Supplier.class))).thenReturn("success");

        // When
        String result = resilienceService.executeWithRetry("test-retry", supplier);

        // Then
        assertEquals("success", result);
        verify(retryRegistry).retry("test-retry");
        verify(retry).executeSupplier(any(Supplier.class));
    }

    @Test
    void shouldExecuteWithTimeLimiter() {
        // Given
        Supplier<String> supplier = () -> "success";
        when(timeLimiter.executeSupplier(any(Supplier.class))).thenReturn("success");

        // When
        String result = resilienceService.executeWithTimeLimiter("test-limiter", supplier);

        // Then
        assertEquals("success", result);
        verify(timeLimiterRegistry).timeLimiter("test-limiter");
        verify(timeLimiter).executeSupplier(any(Supplier.class));
    }

    @Test
    void shouldExecuteWithAllResiliencePatterns() {
        // Given
        Supplier<String> supplier = () -> "success";
        when(circuitBreaker.executeSupplier(any(Supplier.class))).thenReturn("circuit-result");
        when(retry.executeSupplier(any(Supplier.class))).thenReturn("retry-result");
        when(timeLimiter.executeSupplier(any(Supplier.class))).thenReturn("limiter-result");

        // When
        String result = resilienceService.executeWithAllResilience("test", supplier);

        // Then
        assertEquals("circuit-result", result);
        verify(circuitBreakerRegistry).circuitBreaker("test-circuit");
        verify(retryRegistry).retry("test-retry");
        verify(timeLimiterRegistry).timeLimiter("test-limiter");
    }

    @Test
    void shouldExecuteCallableWithCircuitBreaker() {
        // Given
        Callable<String> callable = () -> "success";
        when(circuitBreaker.executeCallable(any(Callable.class))).thenReturn("success");

        // When
        String result = resilienceService.executeCallableWithCircuitBreaker("test-circuit", callable);

        // Then
        assertEquals("success", result);
        verify(circuitBreaker).executeCallable(any(Callable.class));
    }

    @Test
    void shouldExecuteCallableWithRetry() {
        // Given
        Callable<String> callable = () -> "success";
        when(retry.executeCallable(any(Callable.class))).thenReturn("success");

        // When
        String result = resilienceService.executeCallableWithRetry("test-retry", callable);

        // Then
        assertEquals("success", result);
        verify(retry).executeCallable(any(Callable.class));
    }

    @Test
    void shouldGetCircuitBreakerState() {
        // Given
        when(circuitBreaker.getState()).thenReturn(CircuitBreaker.State.CLOSED);

        // When
        CircuitBreaker.State state = resilienceService.getCircuitBreakerState("test-circuit");

        // Then
        assertEquals(CircuitBreaker.State.CLOSED, state);
        verify(circuitBreakerRegistry).circuitBreaker("test-circuit");
    }

    @Test
    void shouldGetRetryMetrics() {
        // Given
        when(retry.getMetrics()).thenReturn(mock(io.github.resilience4j.retry.Retry.Metrics.class));

        // When
        var metrics = resilienceService.getRetryMetrics("test-retry");

        // Then
        assertNotNull(metrics);
        verify(retryRegistry).retry("test-retry");
    }

    @Test
    void shouldHandleCircuitBreakerOpenException() {
        // Given
        Supplier<String> supplier = () -> "success";
        when(circuitBreaker.executeSupplier(any(Supplier.class)))
            .thenThrow(new io.github.resilience4j.circuitbreaker.CallNotPermittedException("Circuit breaker is OPEN"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            resilienceService.executeWithCircuitBreaker("test-circuit", supplier);
        });
    }

    @Test
    void shouldHandleTimeoutException() {
        // Given
        Supplier<String> supplier = () -> "success";
        when(timeLimiter.executeSupplier(any(Supplier.class)))
            .thenThrow(new java.util.concurrent.TimeoutException("Operation timed out"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            resilienceService.executeWithTimeLimiter("test-limiter", supplier);
        });
    }
}
