package com.netflix.springframework.demo.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.springframework.demo.entity.SubscriptionEntity;
import com.netflix.springframework.demo.entity.SubscriptionEntity.SubscriptionStatus;
import com.netflix.springframework.demo.repository.SubscriptionRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Subscription Integration Tests
 * 
 * This test class demonstrates Netflix production-grade integration testing:
 * 1. End-to-end subscription flow testing
 * 2. Database integration testing
 * 3. Service layer integration testing
 * 4. Controller layer integration testing
 * 5. Transaction management testing
 * 
 * For C/C++ engineers:
 * - Integration tests are like system-level testing in C++
 * - @SpringBootTest is like testing the entire application in C++
 * - @Transactional is like database transaction testing in C++
 * - MockMvc is like HTTP request testing in C++ web frameworks
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Subscription Integration Tests")
class SubscriptionIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    
    @MockBean
    private SubscriptionService subscriptionService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private SubscriptionEntity subscriptionEntity;
    
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
    }
    
    @Test
    @DisplayName("Should create subscription end-to-end")
    void shouldCreateSubscriptionEndToEnd() throws Exception {
        // Given
        when(subscriptionService.createSubscription(anyLong(), anyString(), anyString()))
            .thenReturn(subscriptionEntity);
        
        // When
        mockMvc.perform(post("/api/v1/subscriptions")
                .header("X-User-ID", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"priceId\":\"price_test_123\",\"paymentMethodId\":\"pm_test_123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.stripeSubscriptionId").value("sub_test_123"))
                .andExpect(jsonPath("$.data.priceId").value("price_test_123"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.message").value("Subscription created successfully"));
        
        // Then
        verify(subscriptionService).createSubscription(eq(1L), eq("price_test_123"), eq("pm_test_123"));
    }
    
    @Test
    @DisplayName("Should update subscription end-to-end")
    void shouldUpdateSubscriptionEndToEnd() throws Exception {
        // Given
        Long subscriptionId = 1L;
        String newPriceId = "price_test_456";
        
        when(subscriptionService.updateSubscription(subscriptionId, newPriceId))
            .thenReturn(subscriptionEntity);
        
        // When
        mockMvc.perform(put("/api/v1/subscriptions/{subscriptionId}", subscriptionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"priceId\":\"" + newPriceId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.priceId").value("price_test_456"))
                .andExpect(jsonPath("$.message").value("Subscription updated successfully"));
        
        // Then
        verify(subscriptionService).updateSubscription(subscriptionId, newPriceId);
    }
    
    @Test
    @DisplayName("Should cancel subscription end-to-end")
    void shouldCancelSubscriptionEndToEnd() throws Exception {
        // Given
        Long subscriptionId = 1L;
        subscriptionEntity.setStatus(SubscriptionStatus.CANCELLED);
        
        when(subscriptionService.cancelSubscription(subscriptionId, true))
            .thenReturn(subscriptionEntity);
        
        // When
        mockMvc.perform(post("/api/v1/subscriptions/{subscriptionId}/cancel", subscriptionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cancelAtPeriodEnd\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"))
                .andExpect(jsonPath("$.message").value("Subscription cancelled successfully"));
        
        // Then
        verify(subscriptionService).cancelSubscription(subscriptionId, true);
    }
    
    @Test
    @DisplayName("Should reactivate subscription end-to-end")
    void shouldReactivateSubscriptionEndToEnd() throws Exception {
        // Given
        Long subscriptionId = 1L;
        
        when(subscriptionService.reactivateSubscription(subscriptionId))
            .thenReturn(subscriptionEntity);
        
        // When
        mockMvc.perform(post("/api/v1/subscriptions/{subscriptionId}/reactivate", subscriptionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.message").value("Subscription reactivated successfully"));
        
        // Then
        verify(subscriptionService).reactivateSubscription(subscriptionId);
    }
    
    @Test
    @DisplayName("Should get subscription by ID end-to-end")
    void shouldGetSubscriptionByIdEndToEnd() throws Exception {
        // Given
        Long subscriptionId = 1L;
        when(subscriptionService.getSubscriptionById(subscriptionId))
            .thenReturn(Optional.of(subscriptionEntity));
        
        // When
        mockMvc.perform(get("/api/v1/subscriptions/{subscriptionId}", subscriptionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.stripeSubscriptionId").value("sub_test_123"))
                .andExpect(jsonPath("$.data.priceId").value("price_test_123"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.message").value("Subscription retrieved successfully"));
        
        // Then
        verify(subscriptionService).getSubscriptionById(subscriptionId);
    }
    
    @Test
    @DisplayName("Should return not found when subscription not found by ID")
    void shouldReturnNotFoundWhenSubscriptionNotFoundById() throws Exception {
        // Given
        Long subscriptionId = 1L;
        when(subscriptionService.getSubscriptionById(subscriptionId))
            .thenReturn(Optional.empty());
        
        // When
        mockMvc.perform(get("/api/v1/subscriptions/{subscriptionId}", subscriptionId))
                .andExpect(status().isNotFound());
        
        // Then
        verify(subscriptionService).getSubscriptionById(subscriptionId);
    }
    
    @Test
    @DisplayName("Should get subscription by Stripe ID end-to-end")
    void shouldGetSubscriptionByStripeIdEndToEnd() throws Exception {
        // Given
        String stripeSubscriptionId = "sub_test_123";
        when(subscriptionService.getSubscriptionByStripeId(stripeSubscriptionId))
            .thenReturn(Optional.of(subscriptionEntity));
        
        // When
        mockMvc.perform(get("/api/v1/subscriptions/stripe/{stripeSubscriptionId}", stripeSubscriptionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.stripeSubscriptionId").value("sub_test_123"))
                .andExpect(jsonPath("$.message").value("Subscription retrieved successfully"));
        
        // Then
        verify(subscriptionService).getSubscriptionByStripeId(stripeSubscriptionId);
    }
    
    @Test
    @DisplayName("Should return not found when subscription not found by Stripe ID")
    void shouldReturnNotFoundWhenSubscriptionNotFoundByStripeId() throws Exception {
        // Given
        String stripeSubscriptionId = "sub_test_123";
        when(subscriptionService.getSubscriptionByStripeId(stripeSubscriptionId))
            .thenReturn(Optional.empty());
        
        // When
        mockMvc.perform(get("/api/v1/subscriptions/stripe/{stripeSubscriptionId}", stripeSubscriptionId))
                .andExpect(status().isNotFound());
        
        // Then
        verify(subscriptionService).getSubscriptionByStripeId(stripeSubscriptionId);
    }
    
    @Test
    @DisplayName("Should get subscriptions by user ID end-to-end")
    void shouldGetSubscriptionsByUserIdEndToEnd() throws Exception {
        // Given
        Long userId = 1L;
        List<SubscriptionEntity> subscriptions = List.of(subscriptionEntity);
        when(subscriptionService.getSubscriptionsByUserId(userId))
            .thenReturn(subscriptions);
        
        // When
        mockMvc.perform(get("/api/v1/subscriptions/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].userId").value(1))
                .andExpect(jsonPath("$.data[0].stripeSubscriptionId").value("sub_test_123"))
                .andExpect(jsonPath("$.message").value("Subscriptions retrieved successfully"));
        
        // Then
        verify(subscriptionService).getSubscriptionsByUserId(userId);
    }
    
    @Test
    @DisplayName("Should get active subscriptions by user ID end-to-end")
    void shouldGetActiveSubscriptionsByUserIdEndToEnd() throws Exception {
        // Given
        Long userId = 1L;
        List<SubscriptionEntity> subscriptions = List.of(subscriptionEntity);
        when(subscriptionService.getActiveSubscriptionsByUserId(userId))
            .thenReturn(subscriptions);
        
        // When
        mockMvc.perform(get("/api/v1/subscriptions/user/{userId}/active", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].userId").value(1))
                .andExpect(jsonPath("$.data[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.message").value("Active subscriptions retrieved successfully"));
        
        // Then
        verify(subscriptionService).getActiveSubscriptionsByUserId(userId);
    }
    
    @Test
    @DisplayName("Should handle subscription flow with database persistence")
    void shouldHandleSubscriptionFlowWithDatabasePersistence() throws Exception {
        // Given
        when(subscriptionService.createSubscription(anyLong(), anyString(), anyString()))
            .thenReturn(subscriptionEntity);
        
        // When
        mockMvc.perform(post("/api/v1/subscriptions")
                .header("X-User-ID", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"priceId\":\"price_test_123\",\"paymentMethodId\":\"pm_test_123\"}"))
                .andExpect(status().isCreated());
        
        // Then
        verify(subscriptionService).createSubscription(eq(1L), eq("price_test_123"), eq("pm_test_123"));
    }
    
    @Test
    @DisplayName("Should handle subscription update with database persistence")
    void shouldHandleSubscriptionUpdateWithDatabasePersistence() throws Exception {
        // Given
        Long subscriptionId = 1L;
        String newPriceId = "price_test_456";
        
        when(subscriptionService.updateSubscription(subscriptionId, newPriceId))
            .thenReturn(subscriptionEntity);
        
        // When
        mockMvc.perform(put("/api/v1/subscriptions/{subscriptionId}", subscriptionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"priceId\":\"" + newPriceId + "\"}"))
                .andExpect(status().isOk());
        
        // Then
        verify(subscriptionService).updateSubscription(subscriptionId, newPriceId);
    }
    
    @Test
    @DisplayName("Should handle subscription cancellation with database persistence")
    void shouldHandleSubscriptionCancellationWithDatabasePersistence() throws Exception {
        // Given
        Long subscriptionId = 1L;
        subscriptionEntity.setStatus(SubscriptionStatus.CANCELLED);
        
        when(subscriptionService.cancelSubscription(subscriptionId, true))
            .thenReturn(subscriptionEntity);
        
        // When
        mockMvc.perform(post("/api/v1/subscriptions/{subscriptionId}/cancel", subscriptionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cancelAtPeriodEnd\":true}"))
                .andExpect(status().isOk());
        
        // Then
        verify(subscriptionService).cancelSubscription(subscriptionId, true);
    }
    
    @Test
    @DisplayName("Should handle subscription reactivation with database persistence")
    void shouldHandleSubscriptionReactivationWithDatabasePersistence() throws Exception {
        // Given
        Long subscriptionId = 1L;
        
        when(subscriptionService.reactivateSubscription(subscriptionId))
            .thenReturn(subscriptionEntity);
        
        // When
        mockMvc.perform(post("/api/v1/subscriptions/{subscriptionId}/reactivate", subscriptionId))
                .andExpect(status().isOk());
        
        // Then
        verify(subscriptionService).reactivateSubscription(subscriptionId);
    }
    
    @Test
    @DisplayName("Should handle subscription retrieval with database persistence")
    void shouldHandleSubscriptionRetrievalWithDatabasePersistence() throws Exception {
        // Given
        Long subscriptionId = 1L;
        when(subscriptionService.getSubscriptionById(subscriptionId))
            .thenReturn(Optional.of(subscriptionEntity));
        
        // When
        mockMvc.perform(get("/api/v1/subscriptions/{subscriptionId}", subscriptionId))
                .andExpect(status().isOk());
        
        // Then
        verify(subscriptionService).getSubscriptionById(subscriptionId);
    }
    
    @Test
    @DisplayName("Should handle subscription retrieval by Stripe ID with database persistence")
    void shouldHandleSubscriptionRetrievalByStripeIdWithDatabasePersistence() throws Exception {
        // Given
        String stripeSubscriptionId = "sub_test_123";
        when(subscriptionService.getSubscriptionByStripeId(stripeSubscriptionId))
            .thenReturn(Optional.of(subscriptionEntity));
        
        // When
        mockMvc.perform(get("/api/v1/subscriptions/stripe/{stripeSubscriptionId}", stripeSubscriptionId))
                .andExpect(status().isOk());
        
        // Then
        verify(subscriptionService).getSubscriptionByStripeId(stripeSubscriptionId);
    }
    
    @Test
    @DisplayName("Should handle subscription retrieval by user ID with database persistence")
    void shouldHandleSubscriptionRetrievalByUserIdWithDatabasePersistence() throws Exception {
        // Given
        Long userId = 1L;
        List<SubscriptionEntity> subscriptions = List.of(subscriptionEntity);
        when(subscriptionService.getSubscriptionsByUserId(userId))
            .thenReturn(subscriptions);
        
        // When
        mockMvc.perform(get("/api/v1/subscriptions/user/{userId}", userId))
                .andExpect(status().isOk());
        
        // Then
        verify(subscriptionService).getSubscriptionsByUserId(userId);
    }
    
    @Test
    @DisplayName("Should handle subscription retrieval by user ID and status with database persistence")
    void shouldHandleSubscriptionRetrievalByUserIdAndStatusWithDatabasePersistence() throws Exception {
        // Given
        Long userId = 1L;
        List<SubscriptionEntity> subscriptions = List.of(subscriptionEntity);
        when(subscriptionService.getActiveSubscriptionsByUserId(userId))
            .thenReturn(subscriptions);
        
        // When
        mockMvc.perform(get("/api/v1/subscriptions/user/{userId}/active", userId))
                .andExpect(status().isOk());
        
        // Then
        verify(subscriptionService).getActiveSubscriptionsByUserId(userId);
    }
    
    @Test
    @DisplayName("Should handle error scenarios end-to-end")
    void shouldHandleErrorScenariosEndToEnd() throws Exception {
        // Given
        when(subscriptionService.createSubscription(anyLong(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Service error"));
        
        // When
        mockMvc.perform(post("/api/v1/subscriptions")
                .header("X-User-ID", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"priceId\":\"price_test_123\",\"paymentMethodId\":\"pm_test_123\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to create subscription"));
    }
    
    @Test
    @DisplayName("Should handle validation errors end-to-end")
    void shouldHandleValidationErrorsEndToEnd() throws Exception {
        // Given
        when(subscriptionService.createSubscription(anyLong(), anyString(), anyString()))
            .thenThrow(new IllegalArgumentException("Invalid subscription request"));
        
        // When
        mockMvc.perform(post("/api/v1/subscriptions")
                .header("X-User-ID", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"priceId\":\"\",\"paymentMethodId\":\"pm_test_123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid subscription request: Invalid subscription request"));
    }
    
    @Test
    @DisplayName("Should handle missing user ID header end-to-end")
    void shouldHandleMissingUserIdHeaderEndToEnd() throws Exception {
        // When
        mockMvc.perform(post("/api/v1/subscriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"priceId\":\"price_test_123\",\"paymentMethodId\":\"pm_test_123\"}"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle invalid user ID header end-to-end")
    void shouldHandleInvalidUserIdHeaderEndToEnd() throws Exception {
        // When
        mockMvc.perform(post("/api/v1/subscriptions")
                .header("X-User-ID", "invalid")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"priceId\":\"price_test_123\",\"paymentMethodId\":\"pm_test_123\"}"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle negative user ID header end-to-end")
    void shouldHandleNegativeUserIdHeaderEndToEnd() throws Exception {
        // When
        mockMvc.perform(post("/api/v1/subscriptions")
                .header("X-User-ID", "-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"priceId\":\"price_test_123\",\"paymentMethodId\":\"pm_test_123\"}"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle invalid subscription ID path variable end-to-end")
    void shouldHandleInvalidSubscriptionIdPathVariableEndToEnd() throws Exception {
        // When
        mockMvc.perform(put("/api/v1/subscriptions/{subscriptionId}", "invalid")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"priceId\":\"price_test_456\"}"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle negative subscription ID path variable end-to-end")
    void shouldHandleNegativeSubscriptionIdPathVariableEndToEnd() throws Exception {
        // When
        mockMvc.perform(put("/api/v1/subscriptions/{subscriptionId}", -1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"priceId\":\"price_test_456\"}"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle invalid Stripe ID path variable end-to-end")
    void shouldHandleInvalidStripeIdPathVariableEndToEnd() throws Exception {
        // When
        mockMvc.perform(get("/api/v1/subscriptions/stripe/{stripeSubscriptionId}", ""))
                .andExpect(status().isBadRequest());
    }
}
