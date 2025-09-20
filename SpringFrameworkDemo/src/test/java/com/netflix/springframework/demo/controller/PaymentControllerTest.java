package com.netflix.springframework.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.springframework.demo.dto.PaymentRequest;
import com.netflix.springframework.demo.entity.PaymentEntity;
import com.netflix.springframework.demo.entity.PaymentEntity.PaymentStatus;
import com.netflix.springframework.demo.service.StripePaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PaymentController Unit Tests
 * 
 * This test class demonstrates Netflix production-grade unit testing with MockMvc:
 * 1. Comprehensive test coverage for all controller endpoints
 * 2. Mock-based testing for service dependencies
 * 3. HTTP request/response validation
 * 4. Exception handling and error scenarios
 * 5. Input validation and security testing
 * 
 * For C/C++ engineers:
 * - MockMvc is like HTTP request testing in C++ web frameworks
 * - @WebMvcTest is like testing web controllers in C++
 * - @MockBean is like mocking service dependencies in C++
 * - MockMvc.perform() is like making HTTP requests in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@WebMvcTest(PaymentController.class)
@DisplayName("PaymentController Unit Tests")
class PaymentControllerTest {
    
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
    @DisplayName("Should create payment intent successfully")
    void shouldCreatePaymentIntentSuccessfully() throws Exception {
        // Given
        when(paymentService.createPaymentIntent(any(PaymentRequest.class), anyLong()))
            .thenReturn(paymentEntity);
        
        // When & Then
        mockMvc.perform(post("/api/v1/payments")
                .header("X-User-ID", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.amount").value(100.00))
                .andExpect(jsonPath("$.data.currency").value("USD"))
                .andExpect(jsonPath("$.message").value("Payment intent created successfully"));
        
        verify(paymentService).createPaymentIntent(any(PaymentRequest.class), eq(1L));
    }
    
    @Test
    @DisplayName("Should return bad request for invalid payment request")
    void shouldReturnBadRequestForInvalidPaymentRequest() throws Exception {
        // Given
        PaymentRequest invalidRequest = new PaymentRequest();
        invalidRequest.setAmount(null); // Invalid amount
        invalidRequest.setCurrency("USD");
        invalidRequest.setCustomerEmail("test@netflix.com");
        invalidRequest.setPaymentMethodId("pm_test_123");
        
        when(paymentService.createPaymentIntent(any(PaymentRequest.class), anyLong()))
            .thenThrow(new IllegalArgumentException("Invalid payment request"));
        
        // When & Then
        mockMvc.perform(post("/api/v1/payments")
                .header("X-User-ID", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid payment request: Invalid payment request"));
    }
    
    @Test
    @DisplayName("Should return internal server error for service exception")
    void shouldReturnInternalServerErrorForServiceException() throws Exception {
        // Given
        when(paymentService.createPaymentIntent(any(PaymentRequest.class), anyLong()))
            .thenThrow(new RuntimeException("Service error"));
        
        // When & Then
        mockMvc.perform(post("/api/v1/payments")
                .header("X-User-ID", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to create payment intent"));
    }
    
    @Test
    @DisplayName("Should confirm payment intent successfully")
    void shouldConfirmPaymentIntentSuccessfully() throws Exception {
        // Given
        Long paymentId = 1L;
        when(paymentService.confirmPaymentIntent(paymentId))
            .thenReturn(paymentEntity);
        
        // When & Then
        mockMvc.perform(post("/api/v1/payments/{paymentId}/confirm", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.message").value("Payment intent confirmed successfully"));
        
        verify(paymentService).confirmPaymentIntent(paymentId);
    }
    
    @Test
    @DisplayName("Should return bad request for invalid payment ID")
    void shouldReturnBadRequestForInvalidPaymentId() throws Exception {
        // Given
        Long paymentId = -1L;
        when(paymentService.confirmPaymentIntent(paymentId))
            .thenThrow(new IllegalArgumentException("Invalid payment ID"));
        
        // When & Then
        mockMvc.perform(post("/api/v1/payments/{paymentId}/confirm", paymentId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid payment ID: Invalid payment ID"));
    }
    
    @Test
    @DisplayName("Should cancel payment intent successfully")
    void shouldCancelPaymentIntentSuccessfully() throws Exception {
        // Given
        Long paymentId = 1L;
        paymentEntity.setStatus(PaymentStatus.CANCELLED);
        when(paymentService.cancelPaymentIntent(paymentId))
            .thenReturn(paymentEntity);
        
        // When & Then
        mockMvc.perform(post("/api/v1/payments/{paymentId}/cancel", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.message").value("Payment intent cancelled successfully"));
        
        verify(paymentService).cancelPaymentIntent(paymentId);
    }
    
    @Test
    @DisplayName("Should refund payment successfully")
    void shouldRefundPaymentSuccessfully() throws Exception {
        // Given
        Long paymentId = 1L;
        PaymentController.RefundRequest refundRequest = new PaymentController.RefundRequest();
        refundRequest.setAmount(new BigDecimal("50.00"));
        refundRequest.setReason("Customer request");
        
        paymentEntity.setStatus(PaymentStatus.REFUNDED);
        when(paymentService.refundPayment(eq(paymentId), any(BigDecimal.class), anyString()))
            .thenReturn(paymentEntity);
        
        // When & Then
        mockMvc.perform(post("/api/v1/payments/{paymentId}/refund", paymentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refundRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.message").value("Payment refunded successfully"));
        
        verify(paymentService).refundPayment(eq(paymentId), any(BigDecimal.class), anyString());
    }
    
    @Test
    @DisplayName("Should return bad request for invalid refund request")
    void shouldReturnBadRequestForInvalidRefundRequest() throws Exception {
        // Given
        Long paymentId = 1L;
        PaymentController.RefundRequest refundRequest = new PaymentController.RefundRequest();
        refundRequest.setAmount(new BigDecimal("200.00")); // More than payment amount
        refundRequest.setReason("Customer request");
        
        when(paymentService.refundPayment(eq(paymentId), any(BigDecimal.class), anyString()))
            .thenThrow(new IllegalArgumentException("Invalid refund request"));
        
        // When & Then
        mockMvc.perform(post("/api/v1/payments/{paymentId}/refund", paymentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refundRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid refund request: Invalid refund request"));
    }
    
    @Test
    @DisplayName("Should get payment by ID successfully")
    void shouldGetPaymentByIdSuccessfully() throws Exception {
        // Given
        Long paymentId = 1L;
        when(paymentService.getPaymentById(paymentId))
            .thenReturn(Optional.of(paymentEntity));
        
        // When & Then
        mockMvc.perform(get("/api/v1/payments/{paymentId}", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.amount").value(100.00))
                .andExpect(jsonPath("$.data.currency").value("USD"))
                .andExpect(jsonPath("$.message").value("Payment retrieved successfully"));
        
        verify(paymentService).getPaymentById(paymentId);
    }
    
    @Test
    @DisplayName("Should return not found when payment not found")
    void shouldReturnNotFoundWhenPaymentNotFound() throws Exception {
        // Given
        Long paymentId = 1L;
        when(paymentService.getPaymentById(paymentId))
            .thenReturn(Optional.empty());
        
        // When & Then
        mockMvc.perform(get("/api/v1/payments/{paymentId}", paymentId))
                .andExpect(status().isNotFound());
        
        verify(paymentService).getPaymentById(paymentId);
    }
    
    @Test
    @DisplayName("Should get payment by Stripe ID successfully")
    void shouldGetPaymentByStripeIdSuccessfully() throws Exception {
        // Given
        String stripePaymentIntentId = "pi_test_123";
        when(paymentService.getPaymentByStripeId(stripePaymentIntentId))
            .thenReturn(Optional.of(paymentEntity));
        
        // When & Then
        mockMvc.perform(get("/api/v1/payments/stripe/{stripePaymentIntentId}", stripePaymentIntentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.stripePaymentIntentId").value("pi_test_123"))
                .andExpect(jsonPath("$.message").value("Payment retrieved successfully"));
        
        verify(paymentService).getPaymentByStripeId(stripePaymentIntentId);
    }
    
    @Test
    @DisplayName("Should return not found when payment by Stripe ID not found")
    void shouldReturnNotFoundWhenPaymentByStripeIdNotFound() throws Exception {
        // Given
        String stripePaymentIntentId = "pi_test_123";
        when(paymentService.getPaymentByStripeId(stripePaymentIntentId))
            .thenReturn(Optional.empty());
        
        // When & Then
        mockMvc.perform(get("/api/v1/payments/stripe/{stripePaymentIntentId}", stripePaymentIntentId))
                .andExpect(status().isNotFound());
        
        verify(paymentService).getPaymentByStripeId(stripePaymentIntentId);
    }
    
    @Test
    @DisplayName("Should return health check successfully")
    void shouldReturnHealthCheckSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/payments/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Payment service is healthy"))
                .andExpect(jsonPath("$.message").value("Service is running"));
    }
    
    @Test
    @DisplayName("Should return bad request for missing user ID header")
    void shouldReturnBadRequestForMissingUserIdHeader() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should return bad request for invalid user ID header")
    void shouldReturnBadRequestForInvalidUserIdHeader() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/payments")
                .header("X-User-ID", "invalid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should return bad request for negative user ID header")
    void shouldReturnBadRequestForNegativeUserIdHeader() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/payments")
                .header("X-User-ID", "-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should return bad request for invalid payment ID path variable")
    void shouldReturnBadRequestForInvalidPaymentIdPathVariable() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/payments/{paymentId}/confirm", "invalid"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should return bad request for negative payment ID path variable")
    void shouldReturnBadRequestForNegativePaymentIdPathVariable() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/payments/{paymentId}/confirm", -1))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should return bad request for invalid Stripe ID path variable")
    void shouldReturnBadRequestForInvalidStripeIdPathVariable() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/payments/stripe/{stripePaymentIntentId}", ""))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle internal server error for service exception")
    void shouldHandleInternalServerErrorForServiceException() throws Exception {
        // Given
        Long paymentId = 1L;
        when(paymentService.getPaymentById(paymentId))
            .thenThrow(new RuntimeException("Service error"));
        
        // When & Then
        mockMvc.perform(get("/api/v1/payments/{paymentId}", paymentId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to retrieve payment"));
    }
    
    @Test
    @DisplayName("Should handle internal server error for confirm payment exception")
    void shouldHandleInternalServerErrorForConfirmPaymentException() throws Exception {
        // Given
        Long paymentId = 1L;
        when(paymentService.confirmPaymentIntent(paymentId))
            .thenThrow(new RuntimeException("Service error"));
        
        // When & Then
        mockMvc.perform(post("/api/v1/payments/{paymentId}/confirm", paymentId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to confirm payment intent"));
    }
    
    @Test
    @DisplayName("Should handle internal server error for cancel payment exception")
    void shouldHandleInternalServerErrorForCancelPaymentException() throws Exception {
        // Given
        Long paymentId = 1L;
        when(paymentService.cancelPaymentIntent(paymentId))
            .thenThrow(new RuntimeException("Service error"));
        
        // When & Then
        mockMvc.perform(post("/api/v1/payments/{paymentId}/cancel", paymentId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to cancel payment intent"));
    }
    
    @Test
    @DisplayName("Should handle internal server error for refund payment exception")
    void shouldHandleInternalServerErrorForRefundPaymentException() throws Exception {
        // Given
        Long paymentId = 1L;
        PaymentController.RefundRequest refundRequest = new PaymentController.RefundRequest();
        refundRequest.setAmount(new BigDecimal("50.00"));
        refundRequest.setReason("Customer request");
        
        when(paymentService.refundPayment(eq(paymentId), any(BigDecimal.class), anyString()))
            .thenThrow(new RuntimeException("Service error"));
        
        // When & Then
        mockMvc.perform(post("/api/v1/payments/{paymentId}/refund", paymentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refundRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to refund payment"));
    }
}
