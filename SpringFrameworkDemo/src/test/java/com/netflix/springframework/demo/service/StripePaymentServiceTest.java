package com.netflix.springframework.demo.service;

import com.netflix.springframework.demo.config.StripeConfig;
import com.netflix.springframework.demo.dto.PaymentRequest;
import com.netflix.springframework.demo.entity.PaymentEntity;
import com.netflix.springframework.demo.entity.PaymentEntity.PaymentStatus;
import com.netflix.springframework.demo.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * StripePaymentService Unit Tests
 * 
 * This test class demonstrates Netflix production-grade unit testing with Mockito:
 * 1. Comprehensive test coverage for all service methods
 * 2. Mock-based testing for external dependencies
 * 3. Exception handling and error scenarios
 * 4. Edge cases and boundary conditions
 * 5. Performance and behavior validation
 * 
 * For C/C++ engineers:
 * - Unit tests are like function-level testing in C++
 * - Mockito is like mock objects in C++ testing frameworks
 * - @Mock is like creating mock objects in C++
 * - @InjectMocks is like dependency injection in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StripePaymentService Unit Tests")
class StripePaymentServiceTest {
    
    @Mock
    private StripeConfig stripeConfig;
    
    @Mock
    private PaymentRepository paymentRepository;
    
    @InjectMocks
    private StripePaymentService paymentService;
    
    private PaymentRequest paymentRequest;
    private PaymentEntity paymentEntity;
    private PaymentIntent paymentIntent;
    private Refund refund;
    
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
        paymentEntity.setStatus(PaymentStatus.PENDING);
        paymentEntity.setCreatedAt(LocalDateTime.now());
        
        paymentIntent = new PaymentIntent();
        paymentIntent.setId("pi_test_123");
        paymentIntent.setStatus("succeeded");
        paymentIntent.setAmount(10000L); // $100.00 in cents
        
        refund = new Refund();
        refund.setId("re_test_123");
        refund.setAmount(10000L); // $100.00 in cents
        refund.setStatus("succeeded");
    }
    
    @Test
    @DisplayName("Should create payment intent successfully")
    void shouldCreatePaymentIntentSuccessfully() throws StripeException {
        // Given
        Long userId = 1L;
        when(stripeConfig.getDefaultCurrency()).thenReturn("USD");
        when(stripeConfig.getDefaultDescription()).thenReturn("Test payment");
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(paymentEntity);
        
        try (MockedStatic<Stripe> stripeMock = mockStatic(Stripe.class)) {
            stripeMock.when(() -> Stripe.apiKey).thenReturn("sk_test_123");
            
            try (MockedStatic<PaymentIntent> paymentIntentMock = mockStatic(PaymentIntent.class)) {
                paymentIntentMock.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                    .thenReturn(paymentIntent);
                
                // When
                PaymentEntity result = paymentService.createPaymentIntent(paymentRequest, userId);
                
                // Then
                assertThat(result).isNotNull();
                assertThat(result.getUserId()).isEqualTo(userId);
                assertThat(result.getStripePaymentIntentId()).isEqualTo("pi_test_123");
                assertThat(result.getAmount()).isEqualTo(new BigDecimal("100.00"));
                assertThat(result.getCurrency()).isEqualTo("USD");
                
                verify(paymentRepository).save(any(PaymentEntity.class));
            }
        }
    }
    
    @Test
    @DisplayName("Should throw exception when Stripe API fails")
    void shouldThrowExceptionWhenStripeApiFails() throws StripeException {
        // Given
        Long userId = 1L;
        when(stripeConfig.getDefaultCurrency()).thenReturn("USD");
        when(stripeConfig.getDefaultDescription()).thenReturn("Test payment");
        
        try (MockedStatic<Stripe> stripeMock = mockStatic(Stripe.class)) {
            stripeMock.when(() -> Stripe.apiKey).thenReturn("sk_test_123");
            
            try (MockedStatic<PaymentIntent> paymentIntentMock = mockStatic(PaymentIntent.class)) {
                paymentIntentMock.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                    .thenThrow(new StripeException("Stripe API error"));
                
                // When & Then
                assertThatThrownBy(() -> paymentService.createPaymentIntent(paymentRequest, userId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to create payment intent");
            }
        }
    }
    
    @Test
    @DisplayName("Should confirm payment intent successfully")
    void shouldConfirmPaymentIntentSuccessfully() throws StripeException {
        // Given
        Long paymentId = 1L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(paymentEntity);
        
        try (MockedStatic<PaymentIntent> paymentIntentMock = mockStatic(PaymentIntent.class)) {
            paymentIntentMock.when(() -> PaymentIntent.retrieve("pi_test_123"))
                .thenReturn(paymentIntent);
            paymentIntentMock.when(() -> paymentIntent.confirm(any()))
                .thenReturn(paymentIntent);
            
            // When
            PaymentEntity result = paymentService.confirmPaymentIntent(paymentId);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(paymentId);
            verify(paymentRepository).save(any(PaymentEntity.class));
        }
    }
    
    @Test
    @DisplayName("Should throw exception when payment not found")
    void shouldThrowExceptionWhenPaymentNotFound() {
        // Given
        Long paymentId = 1L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> paymentService.confirmPaymentIntent(paymentId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Payment not found with ID: " + paymentId);
    }
    
    @Test
    @DisplayName("Should cancel payment intent successfully")
    void shouldCancelPaymentIntentSuccessfully() throws StripeException {
        // Given
        Long paymentId = 1L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(paymentEntity);
        
        try (MockedStatic<PaymentIntent> paymentIntentMock = mockStatic(PaymentIntent.class)) {
            paymentIntentMock.when(() -> PaymentIntent.retrieve("pi_test_123"))
                .thenReturn(paymentIntent);
            paymentIntentMock.when(() -> paymentIntent.cancel())
                .thenReturn(paymentIntent);
            
            // When
            PaymentEntity result = paymentService.cancelPaymentIntent(paymentId);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(paymentId);
            verify(paymentRepository).save(any(PaymentEntity.class));
        }
    }
    
    @Test
    @DisplayName("Should refund payment successfully")
    void shouldRefundPaymentSuccessfully() throws StripeException {
        // Given
        Long paymentId = 1L;
        BigDecimal refundAmount = new BigDecimal("50.00");
        String refundReason = "Customer request";
        
        paymentEntity.setStatus(PaymentStatus.SUCCEEDED);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(paymentEntity);
        
        try (MockedStatic<Refund> refundMock = mockStatic(Refund.class)) {
            refundMock.when(() -> Refund.create(any(RefundCreateParams.class)))
                .thenReturn(refund);
            
            // When
            PaymentEntity result = paymentService.refundPayment(paymentId, refundAmount, refundReason);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(paymentId);
            verify(paymentRepository).save(any(PaymentEntity.class));
        }
    }
    
    @Test
    @DisplayName("Should refund full amount when refund amount is null")
    void shouldRefundFullAmountWhenRefundAmountIsNull() throws StripeException {
        // Given
        Long paymentId = 1L;
        String refundReason = "Customer request";
        
        paymentEntity.setStatus(PaymentStatus.SUCCEEDED);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(paymentEntity);
        
        try (MockedStatic<Refund> refundMock = mockStatic(Refund.class)) {
            refundMock.when(() -> Refund.create(any(RefundCreateParams.class)))
                .thenReturn(refund);
            
            // When
            PaymentEntity result = paymentService.refundPayment(paymentId, null, refundReason);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(paymentId);
            verify(paymentRepository).save(any(PaymentEntity.class));
        }
    }
    
    @Test
    @DisplayName("Should throw exception when refund amount exceeds remaining amount")
    void shouldThrowExceptionWhenRefundAmountExceedsRemainingAmount() {
        // Given
        Long paymentId = 1L;
        BigDecimal refundAmount = new BigDecimal("200.00"); // More than payment amount
        String refundReason = "Customer request";
        
        paymentEntity.setStatus(PaymentStatus.SUCCEEDED);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));
        
        // When & Then
        assertThatThrownBy(() -> paymentService.refundPayment(paymentId, refundAmount, refundReason))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Refund amount exceeds remaining refundable amount");
    }
    
    @Test
    @DisplayName("Should get payment by ID successfully")
    void shouldGetPaymentByIdSuccessfully() {
        // Given
        Long paymentId = 1L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));
        
        // When
        Optional<PaymentEntity> result = paymentService.getPaymentById(paymentId);
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(paymentId);
    }
    
    @Test
    @DisplayName("Should return empty when payment not found")
    void shouldReturnEmptyWhenPaymentNotFound() {
        // Given
        Long paymentId = 1L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());
        
        // When
        Optional<PaymentEntity> result = paymentService.getPaymentById(paymentId);
        
        // Then
        assertThat(result).isEmpty();
    }
    
    @Test
    @DisplayName("Should get payment by Stripe ID successfully")
    void shouldGetPaymentByStripeIdSuccessfully() {
        // Given
        String stripePaymentIntentId = "pi_test_123";
        when(paymentRepository.findByStripePaymentIntentId(stripePaymentIntentId))
            .thenReturn(Optional.of(paymentEntity));
        
        // When
        Optional<PaymentEntity> result = paymentService.getPaymentByStripeId(stripePaymentIntentId);
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getStripePaymentIntentId()).isEqualTo(stripePaymentIntentId);
    }
    
    @Test
    @DisplayName("Should validate payment request successfully")
    void shouldValidatePaymentRequestSuccessfully() {
        // Given
        PaymentRequest validRequest = new PaymentRequest();
        validRequest.setAmount(new BigDecimal("100.00"));
        validRequest.setCurrency("USD");
        validRequest.setCustomerEmail("test@netflix.com");
        validRequest.setPaymentMethodId("pm_test_123");
        
        // When & Then
        assertThatCode(() -> {
            // This should not throw an exception
            paymentService.createPaymentIntent(validRequest, 1L);
        }).doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should throw exception for invalid payment request")
    void shouldThrowExceptionForInvalidPaymentRequest() {
        // Given
        PaymentRequest invalidRequest = new PaymentRequest();
        invalidRequest.setAmount(null); // Invalid amount
        invalidRequest.setCurrency("USD");
        invalidRequest.setCustomerEmail("test@netflix.com");
        invalidRequest.setPaymentMethodId("pm_test_123");
        
        // When & Then
        assertThatThrownBy(() -> paymentService.createPaymentIntent(invalidRequest, 1L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to create payment intent");
    }
    
    @Test
    @DisplayName("Should handle Stripe exception with retry")
    void shouldHandleStripeExceptionWithRetry() throws StripeException {
        // Given
        Long userId = 1L;
        when(stripeConfig.getDefaultCurrency()).thenReturn("USD");
        when(stripeConfig.getDefaultDescription()).thenReturn("Test payment");
        
        try (MockedStatic<Stripe> stripeMock = mockStatic(Stripe.class)) {
            stripeMock.when(() -> Stripe.apiKey).thenReturn("sk_test_123");
            
            try (MockedStatic<PaymentIntent> paymentIntentMock = mockStatic(PaymentIntent.class)) {
                paymentIntentMock.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                    .thenThrow(new StripeException("Temporary error"))
                    .thenThrow(new StripeException("Temporary error"))
                    .thenReturn(paymentIntent);
                
                when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(paymentEntity);
                
                // When
                PaymentEntity result = paymentService.createPaymentIntent(paymentRequest, userId);
                
                // Then
                assertThat(result).isNotNull();
                verify(paymentRepository).save(any(PaymentEntity.class));
            }
        }
    }
    
    @Test
    @DisplayName("Should handle payment intent with error")
    void shouldHandlePaymentIntentWithError() throws StripeException {
        // Given
        Long paymentId = 1L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(paymentEntity);
        
        // Create payment intent with error
        PaymentIntent errorPaymentIntent = new PaymentIntent();
        errorPaymentIntent.setId("pi_test_123");
        errorPaymentIntent.setStatus("payment_failed");
        
        try (MockedStatic<PaymentIntent> paymentIntentMock = mockStatic(PaymentIntent.class)) {
            paymentIntentMock.when(() -> PaymentIntent.retrieve("pi_test_123"))
                .thenReturn(errorPaymentIntent);
            paymentIntentMock.when(() -> errorPaymentIntent.confirm(any()))
                .thenReturn(errorPaymentIntent);
            
            // When
            PaymentEntity result = paymentService.confirmPaymentIntent(paymentId);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
            verify(paymentRepository).save(any(PaymentEntity.class));
        }
    }
    
    @Test
    @DisplayName("Should handle refund with Stripe exception")
    void shouldHandleRefundWithStripeException() throws StripeException {
        // Given
        Long paymentId = 1L;
        BigDecimal refundAmount = new BigDecimal("50.00");
        String refundReason = "Customer request";
        
        paymentEntity.setStatus(PaymentStatus.SUCCEEDED);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));
        
        try (MockedStatic<Refund> refundMock = mockStatic(Refund.class)) {
            refundMock.when(() -> Refund.create(any(RefundCreateParams.class)))
                .thenThrow(new StripeException("Refund failed"));
            
            // When & Then
            assertThatThrownBy(() -> paymentService.refundPayment(paymentId, refundAmount, refundReason))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to refund payment");
        }
    }
    
    @Test
    @DisplayName("Should handle partial refund successfully")
    void shouldHandlePartialRefundSuccessfully() throws StripeException {
        // Given
        Long paymentId = 1L;
        BigDecimal refundAmount = new BigDecimal("50.00");
        String refundReason = "Customer request";
        
        paymentEntity.setStatus(PaymentStatus.SUCCEEDED);
        paymentEntity.setRefundedAmount(new BigDecimal("25.00")); // Already partially refunded
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(paymentEntity);
        
        try (MockedStatic<Refund> refundMock = mockStatic(Refund.class)) {
            refundMock.when(() -> Refund.create(any(RefundCreateParams.class)))
                .thenReturn(refund);
            
            // When
            PaymentEntity result = paymentService.refundPayment(paymentId, refundAmount, refundReason);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRefundedAmount()).isEqualTo(new BigDecimal("75.00")); // 25 + 50
            verify(paymentRepository).save(any(PaymentEntity.class));
        }
    }
    
    @Test
    @DisplayName("Should handle full refund successfully")
    void shouldHandleFullRefundSuccessfully() throws StripeException {
        // Given
        Long paymentId = 1L;
        BigDecimal refundAmount = new BigDecimal("100.00"); // Full refund
        String refundReason = "Customer request";
        
        paymentEntity.setStatus(PaymentStatus.SUCCEEDED);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(paymentEntity);
        
        try (MockedStatic<Refund> refundMock = mockStatic(Refund.class)) {
            refundMock.when(() -> Refund.create(any(RefundCreateParams.class)))
                .thenReturn(refund);
            
            // When
            PaymentEntity result = paymentService.refundPayment(paymentId, refundAmount, refundReason);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRefundedAmount()).isEqualTo(new BigDecimal("100.00"));
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
            verify(paymentRepository).save(any(PaymentEntity.class));
        }
    }
}
