package com.netflix.springframework.demo.service;

import com.netflix.springframework.demo.entity.PaymentEntity;
import com.netflix.springframework.demo.entity.PaymentEntity.PaymentStatus;
import com.netflix.springframework.demo.repository.PaymentRepository;
import com.netflix.springframework.demo.service.PaymentFulfillmentService.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PaymentFulfillmentService Unit Tests
 * 
 * This test class demonstrates Netflix production-grade unit testing with Mockito:
 * 1. Comprehensive test coverage for all fulfillment methods
 * 2. Mock-based testing for external dependencies
 * 3. Exception handling and error scenarios
 * 4. Edge cases and boundary conditions
 * 5. Business logic validation
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
@DisplayName("PaymentFulfillmentService Unit Tests")
class PaymentFulfillmentServiceTest {
    
    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private UserService userService;
    
    @InjectMocks
    private PaymentFulfillmentService fulfillmentService;
    
    private PaymentEntity paymentEntity;
    
    @BeforeEach
    void setUp() {
        paymentEntity = new PaymentEntity();
        paymentEntity.setId(1L);
        paymentEntity.setUserId(1L);
        paymentEntity.setStripePaymentIntentId("pi_test_123");
        paymentEntity.setAmount(new BigDecimal("100.00"));
        paymentEntity.setCurrency("USD");
        paymentEntity.setStatus(PaymentStatus.SUCCEEDED);
        paymentEntity.setDescription("Test subscription payment");
        paymentEntity.setCreatedAt(LocalDateTime.now());
    }
    
    @Test
    @DisplayName("Should process payment fulfillment successfully")
    void shouldProcessPaymentFulfillmentSuccessfully() {
        // Given
        Long paymentId = 1L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(paymentEntity);
        
        // When
        FulfillmentResult result = fulfillmentService.processPaymentFulfillment(paymentId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(FulfillmentStatus.Status.COMPLETED);
        assertThat(result.getMessage()).isEqualTo("Payment fulfilled successfully");
        assertThat(result.getFulfillmentType()).isEqualTo("generic");
        
        verify(paymentRepository).save(any(PaymentEntity.class));
    }
    
    @Test
    @DisplayName("Should throw exception when payment not found")
    void shouldThrowExceptionWhenPaymentNotFound() {
        // Given
        Long paymentId = 1L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> fulfillmentService.processPaymentFulfillment(paymentId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Payment not found with ID: " + paymentId);
    }
    
    @Test
    @DisplayName("Should throw exception when payment is not successful")
    void shouldThrowExceptionWhenPaymentIsNotSuccessful() {
        // Given
        Long paymentId = 1L;
        paymentEntity.setStatus(PaymentStatus.FAILED);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));
        
        // When & Then
        assertThatThrownBy(() -> fulfillmentService.processPaymentFulfillment(paymentId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Payment must be successful to fulfill");
    }
    
    @Test
    @DisplayName("Should process subscription fulfillment successfully")
    void shouldProcessSubscriptionFulfillmentSuccessfully() {
        // Given
        Long paymentId = 1L;
        paymentEntity.setDescription("Test subscription payment");
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(paymentEntity);
        
        // When
        FulfillmentResult result = fulfillmentService.processPaymentFulfillment(paymentId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(FulfillmentStatus.Status.COMPLETED);
        assertThat(result.getMessage()).isEqualTo("Subscription activated successfully");
        assertThat(result.getFulfillmentType()).isEqualTo("subscription");
        
        verify(paymentRepository).save(any(PaymentEntity.class));
    }
    
    @Test
    @DisplayName("Should process digital product fulfillment successfully")
    void shouldProcessDigitalProductFulfillmentSuccessfully() {
        // Given
        Long paymentId = 1L;
        paymentEntity.setDescription("Test digital product payment");
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(paymentEntity);
        
        // When
        FulfillmentResult result = fulfillmentService.processPaymentFulfillment(paymentId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(FulfillmentStatus.Status.COMPLETED);
        assertThat(result.getMessage()).isEqualTo("Digital product delivered successfully");
        assertThat(result.getFulfillmentType()).isEqualTo("digital_product");
        
        verify(paymentRepository).save(any(PaymentEntity.class));
    }
    
    @Test
    @DisplayName("Should process service fulfillment successfully")
    void shouldProcessServiceFulfillmentSuccessfully() {
        // Given
        Long paymentId = 1L;
        paymentEntity.setDescription("Test service payment");
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(paymentEntity);
        
        // When
        FulfillmentResult result = fulfillmentService.processPaymentFulfillment(paymentId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(FulfillmentStatus.Status.COMPLETED);
        assertThat(result.getMessage()).isEqualTo("Service fulfilled successfully");
        assertThat(result.getFulfillmentType()).isEqualTo("service");
        
        verify(paymentRepository).save(any(PaymentEntity.class));
    }
    
    @Test
    @DisplayName("Should activate subscription successfully")
    void shouldActivateSubscriptionSuccessfully() {
        // Given
        Long paymentId = 1L;
        paymentEntity.setDescription("Test subscription payment");
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(paymentEntity);
        
        // When
        SubscriptionActivationResult result = fulfillmentService.activateSubscription(paymentId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSubscriptionId()).isEqualTo("sub_1");
        assertThat(result.getStatus()).isEqualTo("active");
        assertThat(result.getStartDate()).isNotNull();
        assertThat(result.getEndDate()).isNotNull();
        
        verify(paymentRepository).save(any(PaymentEntity.class));
    }
    
    @Test
    @DisplayName("Should throw exception when subscription payment is invalid")
    void shouldThrowExceptionWhenSubscriptionPaymentIsInvalid() {
        // Given
        Long paymentId = 1L;
        paymentEntity.setDescription("Test payment"); // Not a subscription
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));
        
        // When & Then
        assertThatThrownBy(() -> fulfillmentService.activateSubscription(paymentId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Payment must be for subscription");
    }
    
    @Test
    @DisplayName("Should deliver digital product successfully")
    void shouldDeliverDigitalProductSuccessfully() {
        // Given
        Long paymentId = 1L;
        paymentEntity.setDescription("Test digital product payment");
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(paymentEntity);
        
        // When
        DigitalProductDeliveryResult result = fulfillmentService.deliverDigitalProduct(paymentId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo("prod_1");
        assertThat(result.getProductType()).isEqualTo("digital_content");
        assertThat(result.getDeliveryUrl()).isEqualTo("https://download.example.com/product/1");
        assertThat(result.getDeliveryDate()).isNotNull();
        
        verify(paymentRepository).save(any(PaymentEntity.class));
    }
    
    @Test
    @DisplayName("Should throw exception when digital product payment is invalid")
    void shouldThrowExceptionWhenDigitalProductPaymentIsInvalid() {
        // Given
        Long paymentId = 1L;
        paymentEntity.setDescription("Test payment"); // Not a digital product
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));
        
        // When & Then
        assertThatThrownBy(() -> fulfillmentService.deliverDigitalProduct(paymentId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Payment must be for digital product");
    }
    
    @Test
    @DisplayName("Should process refund fulfillment successfully")
    void shouldProcessRefundFulfillmentSuccessfully() {
        // Given
        Long paymentId = 1L;
        paymentEntity.setStatus(PaymentStatus.REFUNDED);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(paymentEntity);
        
        // When
        RefundFulfillmentResult result = fulfillmentService.processRefundFulfillment(paymentId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(RefundFulfillmentStatus.Status.COMPLETED);
        assertThat(result.getMessage()).isEqualTo("Refund processed successfully");
        assertThat(result.getFulfillmentDate()).isNotNull();
        
        verify(paymentRepository).save(any(PaymentEntity.class));
    }
    
    @Test
    @DisplayName("Should throw exception when refund fulfillment payment is invalid")
    void shouldThrowExceptionWhenRefundFulfillmentPaymentIsInvalid() {
        // Given
        Long paymentId = 1L;
        paymentEntity.setStatus(PaymentStatus.SUCCEEDED); // Not refunded
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));
        
        // When & Then
        assertThatThrownBy(() -> fulfillmentService.processRefundFulfillment(paymentId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Payment must be refunded to process refund fulfillment");
    }
    
    @Test
    @DisplayName("Should get fulfillment status successfully")
    void shouldGetFulfillmentStatusSuccessfully() {
        // Given
        Long paymentId = 1L;
        paymentEntity.setMetadata("fulfillment_status=COMPLETED;fulfillment_date=2024-01-01T00:00:00;fulfillment_type=generic");
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));
        
        // When
        FulfillmentStatus result = fulfillmentService.getFulfillmentStatus(paymentId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(FulfillmentStatus.Status.COMPLETED);
        assertThat(result.getMessage()).isEqualTo("Fulfillment completed");
    }
    
    @Test
    @DisplayName("Should throw exception when getting fulfillment status for non-existent payment")
    void shouldThrowExceptionWhenGettingFulfillmentStatusForNonExistentPayment() {
        // Given
        Long paymentId = 1L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> fulfillmentService.getFulfillmentStatus(paymentId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Payment not found with ID: " + paymentId);
    }
    
    @Test
    @DisplayName("Should handle subscription refund fulfillment successfully")
    void shouldHandleSubscriptionRefundFulfillmentSuccessfully() {
        // Given
        Long paymentId = 1L;
        paymentEntity.setStatus(PaymentStatus.REFUNDED);
        paymentEntity.setDescription("Test subscription payment");
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(paymentEntity);
        
        // When
        RefundFulfillmentResult result = fulfillmentService.processRefundFulfillment(paymentId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(RefundFulfillmentStatus.Status.COMPLETED);
        assertThat(result.getMessage()).isEqualTo("Subscription refund processed successfully");
        
        verify(paymentRepository).save(any(PaymentEntity.class));
    }
    
    @Test
    @DisplayName("Should handle digital product refund fulfillment successfully")
    void shouldHandleDigitalProductRefundFulfillmentSuccessfully() {
        // Given
        Long paymentId = 1L;
        paymentEntity.setStatus(PaymentStatus.REFUNDED);
        paymentEntity.setDescription("Test digital product payment");
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(paymentEntity);
        
        // When
        RefundFulfillmentResult result = fulfillmentService.processRefundFulfillment(paymentId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(RefundFulfillmentStatus.Status.COMPLETED);
        assertThat(result.getMessage()).isEqualTo("Digital product refund processed successfully");
        
        verify(paymentRepository).save(any(PaymentEntity.class));
    }
    
    @Test
    @DisplayName("Should handle partial refund fulfillment successfully")
    void shouldHandlePartialRefundFulfillmentSuccessfully() {
        // Given
        Long paymentId = 1L;
        paymentEntity.setStatus(PaymentStatus.PARTIALLY_REFUNDED);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(paymentEntity);
        
        // When
        RefundFulfillmentResult result = fulfillmentService.processRefundFulfillment(paymentId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(RefundFulfillmentStatus.Status.COMPLETED);
        assertThat(result.getMessage()).isEqualTo("Refund processed successfully");
        
        verify(paymentRepository).save(any(PaymentEntity.class));
    }
    
    @Test
    @DisplayName("Should handle fulfillment service exception")
    void shouldHandleFulfillmentServiceException() {
        // Given
        Long paymentId = 1L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));
        when(paymentRepository.save(any(PaymentEntity.class))).thenThrow(new RuntimeException("Database error"));
        
        // When & Then
        assertThatThrownBy(() -> fulfillmentService.processPaymentFulfillment(paymentId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to process payment fulfillment");
    }
    
    @Test
    @DisplayName("Should handle subscription activation exception")
    void shouldHandleSubscriptionActivationException() {
        // Given
        Long paymentId = 1L;
        paymentEntity.setDescription("Test subscription payment");
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));
        when(paymentRepository.save(any(PaymentEntity.class))).thenThrow(new RuntimeException("Database error"));
        
        // When & Then
        assertThatThrownBy(() -> fulfillmentService.activateSubscription(paymentId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to activate subscription");
    }
    
    @Test
    @DisplayName("Should handle digital product delivery exception")
    void shouldHandleDigitalProductDeliveryException() {
        // Given
        Long paymentId = 1L;
        paymentEntity.setDescription("Test digital product payment");
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));
        when(paymentRepository.save(any(PaymentEntity.class))).thenThrow(new RuntimeException("Database error"));
        
        // When & Then
        assertThatThrownBy(() -> fulfillmentService.deliverDigitalProduct(paymentId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to deliver digital product");
    }
    
    @Test
    @DisplayName("Should handle refund fulfillment exception")
    void shouldHandleRefundFulfillmentException() {
        // Given
        Long paymentId = 1L;
        paymentEntity.setStatus(PaymentStatus.REFUNDED);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));
        when(paymentRepository.save(any(PaymentEntity.class))).thenThrow(new RuntimeException("Database error"));
        
        // When & Then
        assertThatThrownBy(() -> fulfillmentService.processRefundFulfillment(paymentId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to process refund fulfillment");
    }
}
