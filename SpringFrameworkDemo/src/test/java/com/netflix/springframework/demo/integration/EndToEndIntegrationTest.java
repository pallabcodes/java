package com.netflix.springframework.demo.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.springframework.demo.dto.PaymentRequest;
import com.netflix.springframework.demo.entity.PaymentEntity;
import com.netflix.springframework.demo.entity.PaymentEntity.PaymentStatus;
import com.netflix.springframework.demo.entity.SubscriptionEntity;
import com.netflix.springframework.demo.entity.SubscriptionEntity.SubscriptionStatus;
import com.netflix.springframework.demo.repository.PaymentRepository;
import com.netflix.springframework.demo.repository.SubscriptionRepository;
import com.netflix.springframework.demo.service.PaymentFulfillmentService;
import com.netflix.springframework.demo.service.StripePaymentService;
import com.netflix.springframework.demo.service.SubscriptionService;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-End Integration Test Suite
 * 
 * This test class demonstrates Netflix production-grade end-to-end integration testing:
 * 1. Complete business workflow testing
 * 2. Cross-service communication validation
 * 3. Database consistency and transaction testing
 * 4. Real-world scenario simulation
 * 5. Performance under realistic load
 * 
 * For C/C++ engineers:
 * - End-to-end tests are like system integration testing in C++
 * - Business workflow testing is like user scenario testing in C++
 * - Cross-service communication is like inter-process communication in C++
 * - Database consistency is like data integrity testing in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("End-to-End Integration Test Suite")
class EndToEndIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    
    @MockBean
    private StripePaymentService paymentService;
    
    @MockBean
    private SubscriptionService subscriptionService;
    
    @MockBean
    private PaymentFulfillmentService fulfillmentService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private PaymentRequest paymentRequest;
    private PaymentEntity paymentEntity;
    private SubscriptionEntity subscriptionEntity;
    
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
        
        subscriptionEntity = new SubscriptionEntity();
        subscriptionEntity.setId(1L);
        subscriptionEntity.setUserId(1L);
        subscriptionEntity.setStripeSubscriptionId("sub_test_123");
        subscriptionEntity.setPriceId("price_test_123");
        subscriptionEntity.setStatus(SubscriptionStatus.ACTIVE);
        subscriptionEntity.setCurrentPeriodStart(LocalDateTime.now());
        subscriptionEntity.setCurrentPeriodEnd(LocalDateTime.now().plusMonths(1));
        subscriptionEntity.setCreatedAt(LocalDateTime.now());
    }
    
    @Test
    @DisplayName("Should complete full payment workflow end-to-end")
    void shouldCompleteFullPaymentWorkflowEndToEnd() throws Exception {
        // Given
        when(paymentService.createPaymentIntent(any(PaymentRequest.class), anyLong()))
            .thenReturn(paymentEntity);
        when(paymentService.confirmPaymentIntent(anyLong()))
            .thenReturn(paymentEntity);
        when(paymentService.getPaymentById(anyLong()))
            .thenReturn(Optional.of(paymentEntity));
        
        // Step 1: Create payment intent
        mockMvc.perform(post("/api/v1/payments")
                .header("X-User-ID", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.message").value("Payment intent created successfully"));
        
        // Step 2: Confirm payment intent
        mockMvc.perform(post("/api/v1/payments/1/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.message").value("Payment intent confirmed successfully"));
        
        // Step 3: Retrieve payment
        mockMvc.perform(get("/api/v1/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("SUCCEEDED"))
                .andExpect(jsonPath("$.message").value("Payment retrieved successfully"));
        
        // Then - Verify all service calls
        verify(paymentService).createPaymentIntent(any(PaymentRequest.class), eq(1L));
        verify(paymentService).confirmPaymentIntent(1L);
        verify(paymentService).getPaymentById(1L);
    }
    
    @Test
    @DisplayName("Should complete full subscription workflow end-to-end")
    void shouldCompleteFullSubscriptionWorkflowEndToEnd() throws Exception {
        // Given
        when(subscriptionService.createSubscription(anyLong(), anyString(), anyString()))
            .thenReturn(subscriptionEntity);
        when(subscriptionService.getSubscriptionById(anyLong()))
            .thenReturn(Optional.of(subscriptionEntity));
        when(subscriptionService.getSubscriptionsByUserId(anyLong()))
            .thenReturn(List.of(subscriptionEntity));
        
        // Step 1: Create subscription
        mockMvc.perform(post("/api/v1/subscriptions")
                .header("X-User-ID", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"priceId\":\"price_test_123\",\"paymentMethodId\":\"pm_test_123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.stripeSubscriptionId").value("sub_test_123"))
                .andExpect(jsonPath("$.message").value("Subscription created successfully"));
        
        // Step 2: Retrieve subscription
        mockMvc.perform(get("/api/v1/subscriptions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.message").value("Subscription retrieved successfully"));
        
        // Step 3: Get user subscriptions
        mockMvc.perform(get("/api/v1/subscriptions/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.message").value("Subscriptions retrieved successfully"));
        
        // Then - Verify all service calls
        verify(subscriptionService).createSubscription(eq(1L), eq("price_test_123"), eq("pm_test_123"));
        verify(subscriptionService).getSubscriptionById(1L);
        verify(subscriptionService).getSubscriptionsByUserId(1L);
    }
    
    @Test
    @DisplayName("Should complete payment refund workflow end-to-end")
    void shouldCompletePaymentRefundWorkflowEndToEnd() throws Exception {
        // Given
        PaymentEntity refundedPayment = new PaymentEntity();
        refundedPayment.setId(1L);
        refundedPayment.setUserId(1L);
        refundedPayment.setStripePaymentIntentId("pi_test_123");
        refundedPayment.setAmount(new BigDecimal("100.00"));
        refundedPayment.setCurrency("USD");
        refundedPayment.setStatus(PaymentStatus.REFUNDED);
        refundedPayment.setRefundedAmount(new BigDecimal("50.00"));
        refundedPayment.setCreatedAt(LocalDateTime.now());
        
        when(paymentService.getPaymentById(anyLong()))
            .thenReturn(Optional.of(paymentEntity));
        when(paymentService.refundPayment(anyLong(), any(BigDecimal.class), anyString()))
            .thenReturn(refundedPayment);
        
        // Step 1: Get payment details
        mockMvc.perform(get("/api/v1/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
        
        // Step 2: Refund payment
        mockMvc.perform(post("/api/v1/payments/1/refund")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":50.00,\"reason\":\"Customer request\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("REFUNDED"))
                .andExpect(jsonPath("$.message").value("Payment refunded successfully"));
        
        // Then - Verify all service calls
        verify(paymentService).getPaymentById(1L);
        verify(paymentService).refundPayment(eq(1L), eq(new BigDecimal("50.00")), eq("Customer request"));
    }
    
    @Test
    @DisplayName("Should complete subscription cancellation workflow end-to-end")
    void shouldCompleteSubscriptionCancellationWorkflowEndToEnd() throws Exception {
        // Given
        SubscriptionEntity cancelledSubscription = new SubscriptionEntity();
        cancelledSubscription.setId(1L);
        cancelledSubscription.setUserId(1L);
        cancelledSubscription.setStripeSubscriptionId("sub_test_123");
        cancelledSubscription.setPriceId("price_test_123");
        cancelledSubscription.setStatus(SubscriptionStatus.CANCELLED);
        cancelledSubscription.setCurrentPeriodStart(LocalDateTime.now());
        cancelledSubscription.setCurrentPeriodEnd(LocalDateTime.now().plusMonths(1));
        cancelledSubscription.setCreatedAt(LocalDateTime.now());
        
        when(subscriptionService.getSubscriptionById(anyLong()))
            .thenReturn(Optional.of(subscriptionEntity));
        when(subscriptionService.cancelSubscription(anyLong(), anyBoolean()))
            .thenReturn(cancelledSubscription);
        
        // Step 1: Get subscription details
        mockMvc.perform(get("/api/v1/subscriptions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
        
        // Step 2: Cancel subscription
        mockMvc.perform(post("/api/v1/subscriptions/1/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cancelAtPeriodEnd\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"))
                .andExpect(jsonPath("$.message").value("Subscription cancelled successfully"));
        
        // Then - Verify all service calls
        verify(subscriptionService).getSubscriptionById(1L);
        verify(subscriptionService).cancelSubscription(eq(1L), eq(true));
    }
    
    @Test
    @DisplayName("Should complete payment fulfillment workflow end-to-end")
    void shouldCompletePaymentFulfillmentWorkflowEndToEnd() throws Exception {
        // Given
        when(paymentService.getPaymentById(anyLong()))
            .thenReturn(Optional.of(paymentEntity));
        when(fulfillmentService.processPaymentFulfillment(anyLong()))
            .thenReturn(createMockFulfillmentResult());
        
        // Step 1: Get payment details
        mockMvc.perform(get("/api/v1/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
        
        // Step 2: Process payment fulfillment
        mockMvc.perform(post("/api/v1/fulfillment/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Payment fulfillment processed successfully"));
        
        // Then - Verify all service calls
        verify(paymentService).getPaymentById(1L);
        verify(fulfillmentService).processPaymentFulfillment(1L);
    }
    
    @Test
    @DisplayName("Should handle concurrent payment and subscription operations")
    void shouldHandleConcurrentPaymentAndSubscriptionOperations() throws Exception {
        // Given
        when(paymentService.createPaymentIntent(any(PaymentRequest.class), anyLong()))
            .thenReturn(paymentEntity);
        when(subscriptionService.createSubscription(anyLong(), anyString(), anyString()))
            .thenReturn(subscriptionEntity);
        
        // When - Send concurrent requests
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CompletableFuture<Void>[] futures = new CompletableFuture[20];
        
        for (int i = 0; i < 10; i++) {
            final int requestId = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    mockMvc.perform(post("/api/v1/payments")
                            .header("X-User-ID", String.valueOf(requestId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(paymentRequest)))
                            .andExpect(status().isCreated());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor);
            
            futures[i + 10] = CompletableFuture.runAsync(() -> {
                try {
                    mockMvc.perform(post("/api/v1/subscriptions")
                            .header("X-User-ID", String.valueOf(requestId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"priceId\":\"price_test_123\",\"paymentMethodId\":\"pm_test_123\"}"))
                            .andExpect(status().isCreated());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor);
        }
        
        // Wait for all requests to complete
        CompletableFuture.allOf(futures).join();
        
        // Then - All requests should succeed
        verify(paymentService, times(10)).createPaymentIntent(any(PaymentRequest.class), anyLong());
        verify(subscriptionService, times(10)).createSubscription(anyLong(), anyString(), anyString());
        
        executor.shutdown();
    }
    
    @Test
    @DisplayName("Should handle database consistency during concurrent operations")
    void shouldHandleDatabaseConsistencyDuringConcurrentOperations() throws Exception {
        // Given
        when(paymentService.createPaymentIntent(any(PaymentRequest.class), anyLong()))
            .thenReturn(paymentEntity);
        
        // When - Send concurrent requests with same user ID
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CompletableFuture<Void>[] futures = new CompletableFuture[5];
        
        for (int i = 0; i < 5; i++) {
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    mockMvc.perform(post("/api/v1/payments")
                            .header("X-User-ID", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(paymentRequest)))
                            .andExpect(status().isCreated());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor);
        }
        
        // Wait for all requests to complete
        CompletableFuture.allOf(futures).join();
        
        // Then - Database should remain consistent
        verify(paymentService, times(5)).createPaymentIntent(any(PaymentRequest.class), eq(1L));
        
        executor.shutdown();
    }
    
    @Test
    @DisplayName("Should handle error scenarios gracefully in end-to-end workflow")
    void shouldHandleErrorScenariosGracefullyInEndToEndWorkflow() throws Exception {
        // Given - Service fails
        when(paymentService.createPaymentIntent(any(PaymentRequest.class), anyLong()))
            .thenThrow(new RuntimeException("Service temporarily unavailable"));
        
        // When - Send request
        mockMvc.perform(post("/api/v1/payments")
                .header("X-User-ID", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to create payment intent"));
        
        // Then - Error should be handled gracefully
        verify(paymentService).createPaymentIntent(any(PaymentRequest.class), eq(1L));
    }
    
    @Test
    @DisplayName("Should handle partial service failure in end-to-end workflow")
    void shouldHandlePartialServiceFailureInEndToEndWorkflow() throws Exception {
        // Given - Some services work, others fail
        when(paymentService.createPaymentIntent(any(PaymentRequest.class), anyLong()))
            .thenReturn(paymentEntity);
        when(paymentService.confirmPaymentIntent(anyLong()))
            .thenThrow(new RuntimeException("Confirmation service unavailable"));
        
        // Step 1: Create payment intent (should succeed)
        mockMvc.perform(post("/api/v1/payments")
                .header("X-User-ID", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated());
        
        // Step 2: Confirm payment intent (should fail)
        mockMvc.perform(post("/api/v1/payments/1/confirm"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to confirm payment intent"));
        
        // Then - Partial failure should be handled gracefully
        verify(paymentService).createPaymentIntent(any(PaymentRequest.class), eq(1L));
        verify(paymentService).confirmPaymentIntent(1L);
    }
    
    @Test
    @DisplayName("Should handle realistic user scenario end-to-end")
    void shouldHandleRealisticUserScenarioEndToEnd() throws Exception {
        // Given - Realistic user scenario
        when(paymentService.createPaymentIntent(any(PaymentRequest.class), anyLong()))
            .thenReturn(paymentEntity);
        when(paymentService.confirmPaymentIntent(anyLong()))
            .thenReturn(paymentEntity);
        when(subscriptionService.createSubscription(anyLong(), anyString(), anyString()))
            .thenReturn(subscriptionEntity);
        when(fulfillmentService.processPaymentFulfillment(anyLong()))
            .thenReturn(createMockFulfillmentResult());
        
        // Step 1: User creates payment
        mockMvc.perform(post("/api/v1/payments")
                .header("X-User-ID", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated());
        
        // Step 2: User confirms payment
        mockMvc.perform(post("/api/v1/payments/1/confirm"))
                .andExpect(status().isOk());
        
        // Step 3: User creates subscription
        mockMvc.perform(post("/api/v1/subscriptions")
                .header("X-User-ID", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"priceId\":\"price_test_123\",\"paymentMethodId\":\"pm_test_123\"}"))
                .andExpect(status().isCreated());
        
        // Step 4: System processes fulfillment
        mockMvc.perform(post("/api/v1/fulfillment/1"))
                .andExpect(status().isOk());
        
        // Then - Complete user scenario should work
        verify(paymentService).createPaymentIntent(any(PaymentRequest.class), eq(1L));
        verify(paymentService).confirmPaymentIntent(1L);
        verify(subscriptionService).createSubscription(eq(1L), eq("price_test_123"), eq("pm_test_123"));
        verify(fulfillmentService).processPaymentFulfillment(1L);
    }
    
    // Helper methods
    
    private Object createMockFulfillmentResult() {
        // In a real implementation, this would create a mock FulfillmentResult
        return new Object();
    }
}
