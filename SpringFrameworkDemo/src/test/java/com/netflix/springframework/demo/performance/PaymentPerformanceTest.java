package com.netflix.springframework.demo.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.springframework.demo.dto.PaymentRequest;
import com.netflix.springframework.demo.entity.PaymentEntity;
import com.netflix.springframework.demo.entity.PaymentEntity.PaymentStatus;
import com.netflix.springframework.demo.service.StripePaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Payment Performance Tests
 * 
 * This test class demonstrates Netflix production-grade performance testing:
 * 1. Load testing with concurrent requests
 * 2. Stress testing with high volume
 * 3. Performance metrics and benchmarking
 * 4. Resource utilization monitoring
 * 5. Scalability testing and validation
 * 
 * For C/C++ engineers:
 * - Performance tests are like load testing in C++
 * - Concurrent testing is like multi-threaded testing in C++
 * - ExecutorService is like thread pools in C++
 * - CompletableFuture is like async operations in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Payment Performance Tests")
class PaymentPerformanceTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private StripePaymentService paymentService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private PaymentRequest paymentRequest;
    private PaymentEntity paymentEntity;
    
    @BeforeEach
    void setUp() {
        // Setup test data
        paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(new BigDecimal("100.00"));
        paymentRequest.setCurrency("USD");
        paymentRequest.setCustomerEmail("test@netflix.com");
        paymentRequest.setPaymentMethodId("pm_test_123");
        paymentRequest.setDescription("Test payment");
        
        paymentEntity = new PaymentEntity();
        paymentEntity.setId(1L);
        paymentEntity.setUserId(1L);
        paymentEntity.setStripePaymentIntentId("pi_test_123");
        paymentEntity.setAmount(new BigDecimal("100.00"));
        paymentEntity.setCurrency("USD");
        paymentEntity.setStatus(PaymentStatus.SUCCEEDED);
        paymentEntity.setCreatedAt(LocalDateTime.now());
    }
    
    @Test
    @DisplayName("Should handle concurrent payment creation requests")
    void shouldHandleConcurrentPaymentCreationRequests() throws Exception {
        // Given
        int concurrentRequests = 100;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentRequests);
        
        when(paymentService.createPaymentIntent(any(PaymentRequest.class), anyLong()))
            .thenReturn(paymentEntity);
        
        // When
        long startTime = System.currentTimeMillis();
        
        CompletableFuture<Void>[] futures = IntStream.range(0, concurrentRequests)
            .mapToObj(i -> CompletableFuture.runAsync(() -> {
                try {
                    mockMvc.perform(post("/api/v1/payments")
                            .header("X-User-ID", String.valueOf(i + 1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(paymentRequest)))
                            .andExpect(status().isCreated());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor))
            .toArray(CompletableFuture[]::new);
        
        CompletableFuture.allOf(futures).join();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Then
        assertThat(duration).isLessThan(5000); // Should complete within 5 seconds
        verify(paymentService, times(concurrentRequests)).createPaymentIntent(any(PaymentRequest.class), anyLong());
        
        executor.shutdown();
    }
    
    @Test
    @DisplayName("Should handle concurrent payment confirmation requests")
    void shouldHandleConcurrentPaymentConfirmationRequests() throws Exception {
        // Given
        int concurrentRequests = 100;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentRequests);
        
        when(paymentService.confirmPaymentIntent(anyLong()))
            .thenReturn(paymentEntity);
        
        // When
        long startTime = System.currentTimeMillis();
        
        CompletableFuture<Void>[] futures = IntStream.range(0, concurrentRequests)
            .mapToObj(i -> CompletableFuture.runAsync(() -> {
                try {
                    mockMvc.perform(post("/api/v1/payments/{paymentId}/confirm", i + 1))
                            .andExpect(status().isOk());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor))
            .toArray(CompletableFuture[]::new);
        
        CompletableFuture.allOf(futures).join();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Then
        assertThat(duration).isLessThan(5000); // Should complete within 5 seconds
        verify(paymentService, times(concurrentRequests)).confirmPaymentIntent(anyLong());
        
        executor.shutdown();
    }
    
    @Test
    @DisplayName("Should handle concurrent payment cancellation requests")
    void shouldHandleConcurrentPaymentCancellationRequests() throws Exception {
        // Given
        int concurrentRequests = 100;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentRequests);
        
        when(paymentService.cancelPaymentIntent(anyLong()))
            .thenReturn(paymentEntity);
        
        // When
        long startTime = System.currentTimeMillis();
        
        CompletableFuture<Void>[] futures = IntStream.range(0, concurrentRequests)
            .mapToObj(i -> CompletableFuture.runAsync(() -> {
                try {
                    mockMvc.perform(post("/api/v1/payments/{paymentId}/cancel", i + 1))
                            .andExpect(status().isOk());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor))
            .toArray(CompletableFuture[]::new);
        
        CompletableFuture.allOf(futures).join();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Then
        assertThat(duration).isLessThan(5000); // Should complete within 5 seconds
        verify(paymentService, times(concurrentRequests)).cancelPaymentIntent(anyLong());
        
        executor.shutdown();
    }
    
    @Test
    @DisplayName("Should handle concurrent payment refund requests")
    void shouldHandleConcurrentPaymentRefundRequests() throws Exception {
        // Given
        int concurrentRequests = 100;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentRequests);
        
        when(paymentService.refundPayment(anyLong(), any(BigDecimal.class), anyString()))
            .thenReturn(paymentEntity);
        
        // When
        long startTime = System.currentTimeMillis();
        
        CompletableFuture<Void>[] futures = IntStream.range(0, concurrentRequests)
            .mapToObj(i -> CompletableFuture.runAsync(() -> {
                try {
                    mockMvc.perform(post("/api/v1/payments/{paymentId}/refund", i + 1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\":50.00,\"reason\":\"Customer request\"}"))
                            .andExpect(status().isOk());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor))
            .toArray(CompletableFuture[]::new);
        
        CompletableFuture.allOf(futures).join();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Then
        assertThat(duration).isLessThan(5000); // Should complete within 5 seconds
        verify(paymentService, times(concurrentRequests)).refundPayment(anyLong(), any(BigDecimal.class), anyString());
        
        executor.shutdown();
    }
    
    @Test
    @DisplayName("Should handle concurrent payment retrieval requests")
    void shouldHandleConcurrentPaymentRetrievalRequests() throws Exception {
        // Given
        int concurrentRequests = 100;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentRequests);
        
        when(paymentService.getPaymentById(anyLong()))
            .thenReturn(java.util.Optional.of(paymentEntity));
        
        // When
        long startTime = System.currentTimeMillis();
        
        CompletableFuture<Void>[] futures = IntStream.range(0, concurrentRequests)
            .mapToObj(i -> CompletableFuture.runAsync(() -> {
                try {
                    mockMvc.perform(get("/api/v1/payments/{paymentId}", i + 1))
                            .andExpect(status().isOk());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor))
            .toArray(CompletableFuture[]::new);
        
        CompletableFuture.allOf(futures).join();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Then
        assertThat(duration).isLessThan(5000); // Should complete within 5 seconds
        verify(paymentService, times(concurrentRequests)).getPaymentById(anyLong());
        
        executor.shutdown();
    }
    
    @Test
    @DisplayName("Should handle concurrent payment retrieval by Stripe ID requests")
    void shouldHandleConcurrentPaymentRetrievalByStripeIdRequests() throws Exception {
        // Given
        int concurrentRequests = 100;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentRequests);
        
        when(paymentService.getPaymentByStripeId(anyString()))
            .thenReturn(java.util.Optional.of(paymentEntity));
        
        // When
        long startTime = System.currentTimeMillis();
        
        CompletableFuture<Void>[] futures = IntStream.range(0, concurrentRequests)
            .mapToObj(i -> CompletableFuture.runAsync(() -> {
                try {
                    mockMvc.perform(get("/api/v1/payments/stripe/{stripePaymentIntentId}", "pi_test_" + i))
                            .andExpect(status().isOk());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor))
            .toArray(CompletableFuture[]::new);
        
        CompletableFuture.allOf(futures).join();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Then
        assertThat(duration).isLessThan(5000); // Should complete within 5 seconds
        verify(paymentService, times(concurrentRequests)).getPaymentByStripeId(anyString());
        
        executor.shutdown();
    }
    
    @Test
    @DisplayName("Should handle high volume payment creation requests")
    void shouldHandleHighVolumePaymentCreationRequests() throws Exception {
        // Given
        int highVolumeRequests = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(50); // Limit thread pool size
        
        when(paymentService.createPaymentIntent(any(PaymentRequest.class), anyLong()))
            .thenReturn(paymentEntity);
        
        // When
        long startTime = System.currentTimeMillis();
        
        CompletableFuture<Void>[] futures = IntStream.range(0, highVolumeRequests)
            .mapToObj(i -> CompletableFuture.runAsync(() -> {
                try {
                    mockMvc.perform(post("/api/v1/payments")
                            .header("X-User-ID", String.valueOf(i + 1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(paymentRequest)))
                            .andExpect(status().isCreated());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor))
            .toArray(CompletableFuture[]::new);
        
        CompletableFuture.allOf(futures).join();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Then
        assertThat(duration).isLessThan(30000); // Should complete within 30 seconds
        verify(paymentService, times(highVolumeRequests)).createPaymentIntent(any(PaymentRequest.class), anyLong());
        
        executor.shutdown();
    }
    
    @Test
    @DisplayName("Should handle stress testing with mixed operations")
    void shouldHandleStressTestingWithMixedOperations() throws Exception {
        // Given
        int stressTestRequests = 500;
        ExecutorService executor = Executors.newFixedThreadPool(100);
        
        when(paymentService.createPaymentIntent(any(PaymentRequest.class), anyLong()))
            .thenReturn(paymentEntity);
        when(paymentService.confirmPaymentIntent(anyLong()))
            .thenReturn(paymentEntity);
        when(paymentService.cancelPaymentIntent(anyLong()))
            .thenReturn(paymentEntity);
        when(paymentService.refundPayment(anyLong(), any(BigDecimal.class), anyString()))
            .thenReturn(paymentEntity);
        when(paymentService.getPaymentById(anyLong()))
            .thenReturn(java.util.Optional.of(paymentEntity));
        
        // When
        long startTime = System.currentTimeMillis();
        
        CompletableFuture<Void>[] futures = IntStream.range(0, stressTestRequests)
            .mapToObj(i -> CompletableFuture.runAsync(() -> {
                try {
                    int operation = i % 5;
                    switch (operation) {
                        case 0:
                            // Create payment
                            mockMvc.perform(post("/api/v1/payments")
                                    .header("X-User-ID", String.valueOf(i + 1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(paymentRequest)))
                                    .andExpect(status().isCreated());
                            break;
                        case 1:
                            // Confirm payment
                            mockMvc.perform(post("/api/v1/payments/{paymentId}/confirm", i + 1))
                                    .andExpect(status().isOk());
                            break;
                        case 2:
                            // Cancel payment
                            mockMvc.perform(post("/api/v1/payments/{paymentId}/cancel", i + 1))
                                    .andExpect(status().isOk());
                            break;
                        case 3:
                            // Refund payment
                            mockMvc.perform(post("/api/v1/payments/{paymentId}/refund", i + 1)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"amount\":50.00,\"reason\":\"Customer request\"}"))
                                    .andExpect(status().isOk());
                            break;
                        case 4:
                            // Get payment
                            mockMvc.perform(get("/api/v1/payments/{paymentId}", i + 1))
                                    .andExpect(status().isOk());
                            break;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor))
            .toArray(CompletableFuture[]::new);
        
        CompletableFuture.allOf(futures).join();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Then
        assertThat(duration).isLessThan(30000); // Should complete within 30 seconds
        
        executor.shutdown();
    }
    
    @Test
    @DisplayName("Should handle performance benchmarking")
    void shouldHandlePerformanceBenchmarking() throws Exception {
        // Given
        int benchmarkRequests = 100;
        ExecutorService executor = Executors.newFixedThreadPool(benchmarkRequests);
        
        when(paymentService.createPaymentIntent(any(PaymentRequest.class), anyLong()))
            .thenReturn(paymentEntity);
        
        // When
        long startTime = System.currentTimeMillis();
        
        CompletableFuture<Long>[] futures = IntStream.range(0, benchmarkRequests)
            .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                try {
                    long requestStart = System.currentTimeMillis();
                    mockMvc.perform(post("/api/v1/payments")
                            .header("X-User-ID", String.valueOf(i + 1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(paymentRequest)))
                            .andExpect(status().isCreated());
                    long requestEnd = System.currentTimeMillis();
                    return requestEnd - requestStart;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor))
            .toArray(CompletableFuture[]::new);
        
        CompletableFuture.allOf(futures).join();
        
        long endTime = System.currentTimeMillis();
        long totalDuration = endTime - startTime;
        
        // Calculate performance metrics
        long[] responseTimes = new long[benchmarkRequests];
        for (int i = 0; i < benchmarkRequests; i++) {
            responseTimes[i] = futures[i].join();
        }
        
        long averageResponseTime = java.util.Arrays.stream(responseTimes).sum() / benchmarkRequests;
        long maxResponseTime = java.util.Arrays.stream(responseTimes).max().orElse(0);
        long minResponseTime = java.util.Arrays.stream(responseTimes).min().orElse(0);
        double throughput = (double) benchmarkRequests / (totalDuration / 1000.0); // requests per second
        
        // Then
        assertThat(averageResponseTime).isLessThan(1000); // Average response time should be less than 1 second
        assertThat(maxResponseTime).isLessThan(5000); // Max response time should be less than 5 seconds
        assertThat(throughput).isGreaterThan(10); // Should handle at least 10 requests per second
        
        executor.shutdown();
    }
    
    @Test
    @DisplayName("Should handle memory usage under load")
    void shouldHandleMemoryUsageUnderLoad() throws Exception {
        // Given
        int memoryTestRequests = 200;
        ExecutorService executor = Executors.newFixedThreadPool(50);
        
        when(paymentService.createPaymentIntent(any(PaymentRequest.class), anyLong()))
            .thenReturn(paymentEntity);
        
        // Get initial memory usage
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // When
        CompletableFuture<Void>[] futures = IntStream.range(0, memoryTestRequests)
            .mapToObj(i -> CompletableFuture.runAsync(() -> {
                try {
                    mockMvc.perform(post("/api/v1/payments")
                            .header("X-User-ID", String.valueOf(i + 1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(paymentRequest)))
                            .andExpect(status().isCreated());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor))
            .toArray(CompletableFuture[]::new);
        
        CompletableFuture.allOf(futures).join();
        
        // Get final memory usage
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = finalMemory - initialMemory;
        
        // Then
        assertThat(memoryUsed).isLessThan(100 * 1024 * 1024); // Should use less than 100MB additional memory
        
        executor.shutdown();
    }
    
    @Test
    @DisplayName("Should handle timeout scenarios")
    void shouldHandleTimeoutScenarios() throws Exception {
        // Given
        int timeoutTestRequests = 50;
        ExecutorService executor = Executors.newFixedThreadPool(timeoutTestRequests);
        
        when(paymentService.createPaymentIntent(any(PaymentRequest.class), anyLong()))
            .thenAnswer(invocation -> {
                Thread.sleep(100); // Simulate slow response
                return paymentEntity;
            });
        
        // When
        long startTime = System.currentTimeMillis();
        
        CompletableFuture<Void>[] futures = IntStream.range(0, timeoutTestRequests)
            .mapToObj(i -> CompletableFuture.runAsync(() -> {
                try {
                    mockMvc.perform(post("/api/v1/payments")
                            .header("X-User-ID", String.valueOf(i + 1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(paymentRequest)))
                            .andExpect(status().isCreated());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor))
            .toArray(CompletableFuture[]::new);
        
        CompletableFuture.allOf(futures).join();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Then
        assertThat(duration).isLessThan(10000); // Should complete within 10 seconds even with delays
        
        executor.shutdown();
    }
}
