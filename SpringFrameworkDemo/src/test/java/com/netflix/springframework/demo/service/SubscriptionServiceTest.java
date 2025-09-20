package com.netflix.springframework.demo.service;

import com.netflix.springframework.demo.config.StripeConfig;
import com.netflix.springframework.demo.entity.SubscriptionEntity;
import com.netflix.springframework.demo.entity.SubscriptionEntity.SubscriptionStatus;
import com.netflix.springframework.demo.repository.SubscriptionRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.SubscriptionUpdateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SubscriptionService Unit Tests
 * 
 * This test class demonstrates Netflix production-grade unit testing with Mockito:
 * 1. Comprehensive test coverage for all subscription methods
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
@DisplayName("SubscriptionService Unit Tests")
class SubscriptionServiceTest {
    
    @Mock
    private StripeConfig stripeConfig;
    
    @Mock
    private SubscriptionRepository subscriptionRepository;
    
    @Mock
    private StripePaymentService paymentService;
    
    @InjectMocks
    private SubscriptionService subscriptionService;
    
    private SubscriptionEntity subscriptionEntity;
    private Subscription stripeSubscription;
    
    @BeforeEach
    void setUp() {
        // Setup test data
        subscriptionEntity = new SubscriptionEntity();
        subscriptionEntity.setId(1L);
        subscriptionEntity.setUserId(1L);
        subscriptionEntity.setStripeSubscriptionId("sub_test_123");
        subscriptionEntity.setPriceId("price_test_123");
        subscriptionEntity.setStatus(SubscriptionStatus.ACTIVE);
        subscriptionEntity.setCurrentPeriodStart(LocalDateTime.now());
        subscriptionEntity.setCurrentPeriodEnd(LocalDateTime.now().plusMonths(1));
        subscriptionEntity.setCreatedAt(LocalDateTime.now());
        
        stripeSubscription = new Subscription();
        stripeSubscription.setId("sub_test_123");
        stripeSubscription.setStatus("active");
        stripeSubscription.setCurrentPeriodStart(System.currentTimeMillis() / 1000);
        stripeSubscription.setCurrentPeriodEnd(System.currentTimeMillis() / 1000 + 2592000); // 30 days
    }
    
    @Test
    @DisplayName("Should create subscription successfully")
    void shouldCreateSubscriptionSuccessfully() throws StripeException {
        // Given
        Long userId = 1L;
        String priceId = "price_test_123";
        String paymentMethodId = "pm_test_123";
        
        when(stripeConfig.getDefaultCurrency()).thenReturn("USD");
        when(subscriptionRepository.save(any(SubscriptionEntity.class))).thenReturn(subscriptionEntity);
        
        try (MockedStatic<Stripe> stripeMock = mockStatic(Stripe.class)) {
            stripeMock.when(() -> Stripe.apiKey).thenReturn("sk_test_123");
            
            try (MockedStatic<Subscription> subscriptionMock = mockStatic(Subscription.class)) {
                subscriptionMock.when(() -> Subscription.create(any(SubscriptionCreateParams.class)))
                    .thenReturn(stripeSubscription);
                
                // When
                SubscriptionEntity result = subscriptionService.createSubscription(userId, priceId, paymentMethodId);
                
                // Then
                assertThat(result).isNotNull();
                assertThat(result.getUserId()).isEqualTo(userId);
                assertThat(result.getStripeSubscriptionId()).isEqualTo("sub_test_123");
                assertThat(result.getPriceId()).isEqualTo(priceId);
                assertThat(result.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
                
                verify(subscriptionRepository).save(any(SubscriptionEntity.class));
            }
        }
    }
    
    @Test
    @DisplayName("Should throw exception when Stripe API fails")
    void shouldThrowExceptionWhenStripeApiFails() throws StripeException {
        // Given
        Long userId = 1L;
        String priceId = "price_test_123";
        String paymentMethodId = "pm_test_123";
        
        when(stripeConfig.getDefaultCurrency()).thenReturn("USD");
        
        try (MockedStatic<Stripe> stripeMock = mockStatic(Stripe.class)) {
            stripeMock.when(() -> Stripe.apiKey).thenReturn("sk_test_123");
            
            try (MockedStatic<Subscription> subscriptionMock = mockStatic(Subscription.class)) {
                subscriptionMock.when(() -> Subscription.create(any(SubscriptionCreateParams.class)))
                    .thenThrow(new StripeException("Stripe API error"));
                
                // When & Then
                assertThatThrownBy(() -> subscriptionService.createSubscription(userId, priceId, paymentMethodId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to create subscription: Stripe API error");
            }
        }
    }
    
    @Test
    @DisplayName("Should update subscription successfully")
    void shouldUpdateSubscriptionSuccessfully() throws StripeException {
        // Given
        Long subscriptionId = 1L;
        String newPriceId = "price_test_456";
        
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscriptionEntity));
        when(subscriptionRepository.save(any(SubscriptionEntity.class))).thenReturn(subscriptionEntity);
        
        try (MockedStatic<Subscription> subscriptionMock = mockStatic(Subscription.class)) {
            subscriptionMock.when(() -> Subscription.retrieve("sub_test_123"))
                .thenReturn(stripeSubscription);
            subscriptionMock.when(() -> stripeSubscription.update(any(SubscriptionUpdateParams.class)))
                .thenReturn(stripeSubscription);
            
            // When
            SubscriptionEntity result = subscriptionService.updateSubscription(subscriptionId, newPriceId);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(subscriptionId);
            verify(subscriptionRepository).save(any(SubscriptionEntity.class));
        }
    }
    
    @Test
    @DisplayName("Should throw exception when subscription not found for update")
    void shouldThrowExceptionWhenSubscriptionNotFoundForUpdate() {
        // Given
        Long subscriptionId = 1L;
        String newPriceId = "price_test_456";
        
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> subscriptionService.updateSubscription(subscriptionId, newPriceId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Subscription not found with ID: " + subscriptionId);
    }
    
    @Test
    @DisplayName("Should cancel subscription successfully")
    void shouldCancelSubscriptionSuccessfully() throws StripeException {
        // Given
        Long subscriptionId = 1L;
        boolean cancelAtPeriodEnd = true;
        
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscriptionEntity));
        when(subscriptionRepository.save(any(SubscriptionEntity.class))).thenReturn(subscriptionEntity);
        
        try (MockedStatic<Subscription> subscriptionMock = mockStatic(Subscription.class)) {
            subscriptionMock.when(() -> Subscription.retrieve("sub_test_123"))
                .thenReturn(stripeSubscription);
            subscriptionMock.when(() -> stripeSubscription.cancel())
                .thenReturn(stripeSubscription);
            
            // When
            SubscriptionEntity result = subscriptionService.cancelSubscription(subscriptionId, cancelAtPeriodEnd);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(subscriptionId);
            verify(subscriptionRepository).save(any(SubscriptionEntity.class));
        }
    }
    
    @Test
    @DisplayName("Should throw exception when subscription not found for cancellation")
    void shouldThrowExceptionWhenSubscriptionNotFoundForCancellation() {
        // Given
        Long subscriptionId = 1L;
        boolean cancelAtPeriodEnd = true;
        
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> subscriptionService.cancelSubscription(subscriptionId, cancelAtPeriodEnd))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Subscription not found with ID: " + subscriptionId);
    }
    
    @Test
    @DisplayName("Should reactivate subscription successfully")
    void shouldReactivateSubscriptionSuccessfully() throws StripeException {
        // Given
        Long subscriptionId = 1L;
        
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscriptionEntity));
        when(subscriptionRepository.save(any(SubscriptionEntity.class))).thenReturn(subscriptionEntity);
        
        try (MockedStatic<Subscription> subscriptionMock = mockStatic(Subscription.class)) {
            subscriptionMock.when(() -> Subscription.retrieve("sub_test_123"))
                .thenReturn(stripeSubscription);
            
            // When
            SubscriptionEntity result = subscriptionService.reactivateSubscription(subscriptionId);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(subscriptionId);
            verify(subscriptionRepository).save(any(SubscriptionEntity.class));
        }
    }
    
    @Test
    @DisplayName("Should throw exception when subscription not found for reactivation")
    void shouldThrowExceptionWhenSubscriptionNotFoundForReactivation() {
        // Given
        Long subscriptionId = 1L;
        
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> subscriptionService.reactivateSubscription(subscriptionId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Subscription not found with ID: " + subscriptionId);
    }
    
    @Test
    @DisplayName("Should get subscription by ID successfully")
    void shouldGetSubscriptionByIdSuccessfully() {
        // Given
        Long subscriptionId = 1L;
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscriptionEntity));
        
        // When
        Optional<SubscriptionEntity> result = subscriptionService.getSubscriptionById(subscriptionId);
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(subscriptionId);
    }
    
    @Test
    @DisplayName("Should return empty when subscription not found by ID")
    void shouldReturnEmptyWhenSubscriptionNotFoundById() {
        // Given
        Long subscriptionId = 1L;
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.empty());
        
        // When
        Optional<SubscriptionEntity> result = subscriptionService.getSubscriptionById(subscriptionId);
        
        // Then
        assertThat(result).isEmpty();
    }
    
    @Test
    @DisplayName("Should get subscription by Stripe ID successfully")
    void shouldGetSubscriptionByStripeIdSuccessfully() {
        // Given
        String stripeSubscriptionId = "sub_test_123";
        when(subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId))
            .thenReturn(Optional.of(subscriptionEntity));
        
        // When
        Optional<SubscriptionEntity> result = subscriptionService.getSubscriptionByStripeId(stripeSubscriptionId);
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getStripeSubscriptionId()).isEqualTo(stripeSubscriptionId);
    }
    
    @Test
    @DisplayName("Should return empty when subscription not found by Stripe ID")
    void shouldReturnEmptyWhenSubscriptionNotFoundByStripeId() {
        // Given
        String stripeSubscriptionId = "sub_test_123";
        when(subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId))
            .thenReturn(Optional.empty());
        
        // When
        Optional<SubscriptionEntity> result = subscriptionService.getSubscriptionByStripeId(stripeSubscriptionId);
        
        // Then
        assertThat(result).isEmpty();
    }
    
    @Test
    @DisplayName("Should get subscriptions by user ID successfully")
    void shouldGetSubscriptionsByUserIdSuccessfully() {
        // Given
        Long userId = 1L;
        List<SubscriptionEntity> subscriptions = List.of(subscriptionEntity);
        when(subscriptionRepository.findByUserId(userId)).thenReturn(subscriptions);
        
        // When
        List<SubscriptionEntity> result = subscriptionService.getSubscriptionsByUserId(userId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(userId);
    }
    
    @Test
    @DisplayName("Should get active subscriptions by user ID successfully")
    void shouldGetActiveSubscriptionsByUserIdSuccessfully() {
        // Given
        Long userId = 1L;
        List<SubscriptionEntity> subscriptions = List.of(subscriptionEntity);
        when(subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE))
            .thenReturn(subscriptions);
        
        // When
        List<SubscriptionEntity> result = subscriptionService.getActiveSubscriptionsByUserId(userId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(userId);
        assertThat(result.get(0).getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }
    
    @Test
    @DisplayName("Should handle Stripe exception with retry")
    void shouldHandleStripeExceptionWithRetry() throws StripeException {
        // Given
        Long userId = 1L;
        String priceId = "price_test_123";
        String paymentMethodId = "pm_test_123";
        
        when(stripeConfig.getDefaultCurrency()).thenReturn("USD");
        
        try (MockedStatic<Stripe> stripeMock = mockStatic(Stripe.class)) {
            stripeMock.when(() -> Stripe.apiKey).thenReturn("sk_test_123");
            
            try (MockedStatic<Subscription> subscriptionMock = mockStatic(Subscription.class)) {
                subscriptionMock.when(() -> Subscription.create(any(SubscriptionCreateParams.class)))
                    .thenThrow(new StripeException("Temporary error"))
                    .thenThrow(new StripeException("Temporary error"))
                    .thenReturn(stripeSubscription);
                
                when(subscriptionRepository.save(any(SubscriptionEntity.class))).thenReturn(subscriptionEntity);
                
                // When
                SubscriptionEntity result = subscriptionService.createSubscription(userId, priceId, paymentMethodId);
                
                // Then
                assertThat(result).isNotNull();
                verify(subscriptionRepository).save(any(SubscriptionEntity.class));
            }
        }
    }
    
    @Test
    @DisplayName("Should handle subscription update with error")
    void shouldHandleSubscriptionUpdateWithError() throws StripeException {
        // Given
        Long subscriptionId = 1L;
        String newPriceId = "price_test_456";
        
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscriptionEntity));
        
        try (MockedStatic<Subscription> subscriptionMock = mockStatic(Subscription.class)) {
            subscriptionMock.when(() -> Subscription.retrieve("sub_test_123"))
                .thenReturn(stripeSubscription);
            subscriptionMock.when(() -> stripeSubscription.update(any(SubscriptionUpdateParams.class)))
                .thenThrow(new StripeException("Update failed"));
            
            // When & Then
            assertThatThrownBy(() -> subscriptionService.updateSubscription(subscriptionId, newPriceId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to update subscription: Update failed");
        }
    }
    
    @Test
    @DisplayName("Should handle subscription cancellation with error")
    void shouldHandleSubscriptionCancellationWithError() throws StripeException {
        // Given
        Long subscriptionId = 1L;
        boolean cancelAtPeriodEnd = true;
        
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscriptionEntity));
        
        try (MockedStatic<Subscription> subscriptionMock = mockStatic(Subscription.class)) {
            subscriptionMock.when(() -> Subscription.retrieve("sub_test_123"))
                .thenReturn(stripeSubscription);
            subscriptionMock.when(() -> stripeSubscription.cancel())
                .thenThrow(new StripeException("Cancellation failed"));
            
            // When & Then
            assertThatThrownBy(() -> subscriptionService.cancelSubscription(subscriptionId, cancelAtPeriodEnd))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to cancel subscription: Cancellation failed");
        }
    }
    
    @Test
    @DisplayName("Should handle subscription reactivation with error")
    void shouldHandleSubscriptionReactivationWithError() throws StripeException {
        // Given
        Long subscriptionId = 1L;
        
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscriptionEntity));
        
        try (MockedStatic<Subscription> subscriptionMock = mockStatic(Subscription.class)) {
            subscriptionMock.when(() -> Subscription.retrieve("sub_test_123"))
                .thenThrow(new StripeException("Reactivation failed"));
            
            // When & Then
            assertThatThrownBy(() -> subscriptionService.reactivateSubscription(subscriptionId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to reactivate subscription: Reactivation failed");
        }
    }
    
    @Test
    @DisplayName("Should handle service exception during subscription creation")
    void shouldHandleServiceExceptionDuringSubscriptionCreation() throws StripeException {
        // Given
        Long userId = 1L;
        String priceId = "price_test_123";
        String paymentMethodId = "pm_test_123";
        
        when(stripeConfig.getDefaultCurrency()).thenReturn("USD");
        
        try (MockedStatic<Stripe> stripeMock = mockStatic(Stripe.class)) {
            stripeMock.when(() -> Stripe.apiKey).thenReturn("sk_test_123");
            
            try (MockedStatic<Subscription> subscriptionMock = mockStatic(Subscription.class)) {
                subscriptionMock.when(() -> Subscription.create(any(SubscriptionCreateParams.class)))
                    .thenReturn(stripeSubscription);
                
                when(subscriptionRepository.save(any(SubscriptionEntity.class)))
                    .thenThrow(new RuntimeException("Database error"));
                
                // When & Then
                assertThatThrownBy(() -> subscriptionService.createSubscription(userId, priceId, paymentMethodId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to create subscription");
            }
        }
    }
    
    @Test
    @DisplayName("Should handle service exception during subscription update")
    void shouldHandleServiceExceptionDuringSubscriptionUpdate() throws StripeException {
        // Given
        Long subscriptionId = 1L;
        String newPriceId = "price_test_456";
        
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscriptionEntity));
        when(subscriptionRepository.save(any(SubscriptionEntity.class)))
            .thenThrow(new RuntimeException("Database error"));
        
        try (MockedStatic<Subscription> subscriptionMock = mockStatic(Subscription.class)) {
            subscriptionMock.when(() -> Subscription.retrieve("sub_test_123"))
                .thenReturn(stripeSubscription);
            subscriptionMock.when(() -> stripeSubscription.update(any(SubscriptionUpdateParams.class)))
                .thenReturn(stripeSubscription);
            
            // When & Then
            assertThatThrownBy(() -> subscriptionService.updateSubscription(subscriptionId, newPriceId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to update subscription");
        }
    }
    
    @Test
    @DisplayName("Should handle service exception during subscription cancellation")
    void shouldHandleServiceExceptionDuringSubscriptionCancellation() throws StripeException {
        // Given
        Long subscriptionId = 1L;
        boolean cancelAtPeriodEnd = true;
        
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscriptionEntity));
        when(subscriptionRepository.save(any(SubscriptionEntity.class)))
            .thenThrow(new RuntimeException("Database error"));
        
        try (MockedStatic<Subscription> subscriptionMock = mockStatic(Subscription.class)) {
            subscriptionMock.when(() -> Subscription.retrieve("sub_test_123"))
                .thenReturn(stripeSubscription);
            subscriptionMock.when(() -> stripeSubscription.cancel())
                .thenReturn(stripeSubscription);
            
            // When & Then
            assertThatThrownBy(() -> subscriptionService.cancelSubscription(subscriptionId, cancelAtPeriodEnd))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to cancel subscription");
        }
    }
    
    @Test
    @DisplayName("Should handle service exception during subscription reactivation")
    void shouldHandleServiceExceptionDuringSubscriptionReactivation() throws StripeException {
        // Given
        Long subscriptionId = 1L;
        
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscriptionEntity));
        when(subscriptionRepository.save(any(SubscriptionEntity.class)))
            .thenThrow(new RuntimeException("Database error"));
        
        try (MockedStatic<Subscription> subscriptionMock = mockStatic(Subscription.class)) {
            subscriptionMock.when(() -> Subscription.retrieve("sub_test_123"))
                .thenReturn(stripeSubscription);
            
            // When & Then
            assertThatThrownBy(() -> subscriptionService.reactivateSubscription(subscriptionId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to reactivate subscription");
        }
    }
}
