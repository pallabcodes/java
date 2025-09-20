package com.netflix.springframework.demo.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.springframework.demo.dto.PaymentRequest;
import com.netflix.springframework.demo.entity.PaymentEntity;
import com.netflix.springframework.demo.entity.PaymentEntity.PaymentStatus;
import com.netflix.springframework.demo.repository.PaymentRepository;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Payment Integration Tests
 * 
 * This test class demonstrates Netflix production-grade integration testing:
 * 1. End-to-end payment flow testing
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
@Testcontainers
@Transactional
@DisplayName("Payment Integration Tests")
class PaymentIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("netflix_payment_test_db")
            .withUsername("payment_test_user")
            .withPassword("payment_test_password")
            .withInitScript("init-payment-test-data.sql")
            .withReuse(true);
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
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
    @DisplayName("Should create payment intent end-to-end")
    void shouldCreatePaymentIntentEndToEnd() throws Exception {
        // Given
        when(paymentService.createPaymentIntent(any(PaymentRequest.class), anyLong()))
            .thenReturn(paymentEntity);
        
        // When
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
        
        // Then
        verify(paymentService).createPaymentIntent(any(PaymentRequest.class), eq(1L));
    }
    
    @Test
    @DisplayName("Should confirm payment intent end-to-end")
    void shouldConfirmPaymentIntentEndToEnd() throws Exception {
        // Given
        Long paymentId = 1L;
        when(paymentService.confirmPaymentIntent(paymentId))
            .thenReturn(paymentEntity);
        
        // When
        mockMvc.perform(post("/api/v1/payments/{paymentId}/confirm", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.message").value("Payment intent confirmed successfully"));
        
        // Then
        verify(paymentService).confirmPaymentIntent(paymentId);
    }
    
    @Test
    @DisplayName("Should cancel payment intent end-to-end")
    void shouldCancelPaymentIntentEndToEnd() throws Exception {
        // Given
        Long paymentId = 1L;
        paymentEntity.setStatus(PaymentStatus.CANCELLED);
        when(paymentService.cancelPaymentIntent(paymentId))
            .thenReturn(paymentEntity);
        
        // When
        mockMvc.perform(post("/api/v1/payments/{paymentId}/cancel", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.message").value("Payment intent cancelled successfully"));
        
        // Then
        verify(paymentService).cancelPaymentIntent(paymentId);
    }
    
    @Test
    @DisplayName("Should refund payment end-to-end")
    void shouldRefundPaymentEndToEnd() throws Exception {
        // Given
        Long paymentId = 1L;
        PaymentController.RefundRequest refundRequest = new PaymentController.RefundRequest();
        refundRequest.setAmount(new BigDecimal("50.00"));
        refundRequest.setReason("Customer request");
        
        paymentEntity.setStatus(PaymentStatus.REFUNDED);
        when(paymentService.refundPayment(eq(paymentId), any(BigDecimal.class), anyString()))
            .thenReturn(paymentEntity);
        
        // When
        mockMvc.perform(post("/api/v1/payments/{paymentId}/refund", paymentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refundRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.message").value("Payment refunded successfully"));
        
        // Then
        verify(paymentService).refundPayment(eq(paymentId), any(BigDecimal.class), anyString());
    }
    
    @Test
    @DisplayName("Should get payment by ID end-to-end")
    void shouldGetPaymentByIdEndToEnd() throws Exception {
        // Given
        Long paymentId = 1L;
        when(paymentService.getPaymentById(paymentId))
            .thenReturn(Optional.of(paymentEntity));
        
        // When
        mockMvc.perform(get("/api/v1/payments/{paymentId}", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.amount").value(100.00))
                .andExpect(jsonPath("$.data.currency").value("USD"))
                .andExpect(jsonPath("$.message").value("Payment retrieved successfully"));
        
        // Then
        verify(paymentService).getPaymentById(paymentId);
    }
    
    @Test
    @DisplayName("Should get payment by Stripe ID end-to-end")
    void shouldGetPaymentByStripeIdEndToEnd() throws Exception {
        // Given
        String stripePaymentIntentId = "pi_test_123";
        when(paymentService.getPaymentByStripeId(stripePaymentIntentId))
            .thenReturn(Optional.of(paymentEntity));
        
        // When
        mockMvc.perform(get("/api/v1/payments/stripe/{stripePaymentIntentId}", stripePaymentIntentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.stripePaymentIntentId").value("pi_test_123"))
                .andExpect(jsonPath("$.message").value("Payment retrieved successfully"));
        
        // Then
        verify(paymentService).getPaymentByStripeId(stripePaymentIntentId);
    }
    
    @Test
    @DisplayName("Should handle payment flow with database persistence")
    void shouldHandlePaymentFlowWithDatabasePersistence() throws Exception {
        // Given
        PaymentEntity savedPayment = new PaymentEntity();
        savedPayment.setId(1L);
        savedPayment.setUserId(1L);
        savedPayment.setStripePaymentIntentId("pi_test_123");
        savedPayment.setAmount(new BigDecimal("100.00"));
        savedPayment.setCurrency("USD");
        savedPayment.setStatus(PaymentStatus.PENDING);
        savedPayment.setCreatedAt(LocalDateTime.now());
        
        when(paymentService.createPaymentIntent(any(PaymentRequest.class), anyLong()))
            .thenReturn(savedPayment);
        
        // When
        mockMvc.perform(post("/api/v1/payments")
                .header("X-User-ID", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated());
        
        // Then
        verify(paymentService).createPaymentIntent(any(PaymentRequest.class), eq(1L));
    }
    
    @Test
    @DisplayName("Should handle payment confirmation with database persistence")
    void shouldHandlePaymentConfirmationWithDatabasePersistence() throws Exception {
        // Given
        Long paymentId = 1L;
        PaymentEntity confirmedPayment = new PaymentEntity();
        confirmedPayment.setId(1L);
        confirmedPayment.setUserId(1L);
        confirmedPayment.setStripePaymentIntentId("pi_test_123");
        confirmedPayment.setAmount(new BigDecimal("100.00"));
        confirmedPayment.setCurrency("USD");
        confirmedPayment.setStatus(PaymentStatus.SUCCEEDED);
        confirmedPayment.setCreatedAt(LocalDateTime.now());
        
        when(paymentService.confirmPaymentIntent(paymentId))
            .thenReturn(confirmedPayment);
        
        // When
        mockMvc.perform(post("/api/v1/payments/{paymentId}/confirm", paymentId))
                .andExpect(status().isOk());
        
        // Then
        verify(paymentService).confirmPaymentIntent(paymentId);
    }
    
    @Test
    @DisplayName("Should handle payment cancellation with database persistence")
    void shouldHandlePaymentCancellationWithDatabasePersistence() throws Exception {
        // Given
        Long paymentId = 1L;
        PaymentEntity cancelledPayment = new PaymentEntity();
        cancelledPayment.setId(1L);
        cancelledPayment.setUserId(1L);
        cancelledPayment.setStripePaymentIntentId("pi_test_123");
        cancelledPayment.setAmount(new BigDecimal("100.00"));
        cancelledPayment.setCurrency("USD");
        cancelledPayment.setStatus(PaymentStatus.CANCELLED);
        cancelledPayment.setCreatedAt(LocalDateTime.now());
        
        when(paymentService.cancelPaymentIntent(paymentId))
            .thenReturn(cancelledPayment);
        
        // When
        mockMvc.perform(post("/api/v1/payments/{paymentId}/cancel", paymentId))
                .andExpect(status().isOk());
        
        // Then
        verify(paymentService).cancelPaymentIntent(paymentId);
    }
    
    @Test
    @DisplayName("Should handle payment refund with database persistence")
    void shouldHandlePaymentRefundWithDatabasePersistence() throws Exception {
        // Given
        Long paymentId = 1L;
        PaymentController.RefundRequest refundRequest = new PaymentController.RefundRequest();
        refundRequest.setAmount(new BigDecimal("50.00"));
        refundRequest.setReason("Customer request");
        
        PaymentEntity refundedPayment = new PaymentEntity();
        refundedPayment.setId(1L);
        refundedPayment.setUserId(1L);
        refundedPayment.setStripePaymentIntentId("pi_test_123");
        refundedPayment.setAmount(new BigDecimal("100.00"));
        refundedPayment.setCurrency("USD");
        refundedPayment.setStatus(PaymentStatus.REFUNDED);
        refundedPayment.setRefundedAmount(new BigDecimal("50.00"));
        refundedPayment.setCreatedAt(LocalDateTime.now());
        
        when(paymentService.refundPayment(eq(paymentId), any(BigDecimal.class), anyString()))
            .thenReturn(refundedPayment);
        
        // When
        mockMvc.perform(post("/api/v1/payments/{paymentId}/refund", paymentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refundRequest)))
                .andExpect(status().isOk());
        
        // Then
        verify(paymentService).refundPayment(eq(paymentId), any(BigDecimal.class), anyString());
    }
    
    @Test
    @DisplayName("Should handle payment retrieval with database persistence")
    void shouldHandlePaymentRetrievalWithDatabasePersistence() throws Exception {
        // Given
        Long paymentId = 1L;
        PaymentEntity retrievedPayment = new PaymentEntity();
        retrievedPayment.setId(1L);
        retrievedPayment.setUserId(1L);
        retrievedPayment.setStripePaymentIntentId("pi_test_123");
        retrievedPayment.setAmount(new BigDecimal("100.00"));
        retrievedPayment.setCurrency("USD");
        retrievedPayment.setStatus(PaymentStatus.SUCCEEDED);
        retrievedPayment.setCreatedAt(LocalDateTime.now());
        
        when(paymentService.getPaymentById(paymentId))
            .thenReturn(Optional.of(retrievedPayment));
        
        // When
        mockMvc.perform(get("/api/v1/payments/{paymentId}", paymentId))
                .andExpect(status().isOk());
        
        // Then
        verify(paymentService).getPaymentById(paymentId);
    }
    
    @Test
    @DisplayName("Should handle payment retrieval by Stripe ID with database persistence")
    void shouldHandlePaymentRetrievalByStripeIdWithDatabasePersistence() throws Exception {
        // Given
        String stripePaymentIntentId = "pi_test_123";
        PaymentEntity retrievedPayment = new PaymentEntity();
        retrievedPayment.setId(1L);
        retrievedPayment.setUserId(1L);
        retrievedPayment.setStripePaymentIntentId("pi_test_123");
        retrievedPayment.setAmount(new BigDecimal("100.00"));
        retrievedPayment.setCurrency("USD");
        retrievedPayment.setStatus(PaymentStatus.SUCCEEDED);
        retrievedPayment.setCreatedAt(LocalDateTime.now());
        
        when(paymentService.getPaymentByStripeId(stripePaymentIntentId))
            .thenReturn(Optional.of(retrievedPayment));
        
        // When
        mockMvc.perform(get("/api/v1/payments/stripe/{stripePaymentIntentId}", stripePaymentIntentId))
                .andExpect(status().isOk());
        
        // Then
        verify(paymentService).getPaymentByStripeId(stripePaymentIntentId);
    }
    
    @Test
    @DisplayName("Should handle health check end-to-end")
    void shouldHandleHealthCheckEndToEnd() throws Exception {
        // When
        mockMvc.perform(get("/api/v1/payments/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Payment service is healthy"))
                .andExpect(jsonPath("$.message").value("Service is running"));
    }
    
    @Test
    @DisplayName("Should handle error scenarios end-to-end")
    void shouldHandleErrorScenariosEndToEnd() throws Exception {
        // Given
        when(paymentService.createPaymentIntent(any(PaymentRequest.class), anyLong()))
            .thenThrow(new RuntimeException("Service error"));
        
        // When
        mockMvc.perform(post("/api/v1/payments")
                .header("X-User-ID", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to create payment intent"));
    }
    
    @Test
    @DisplayName("Should handle validation errors end-to-end")
    void shouldHandleValidationErrorsEndToEnd() throws Exception {
        // Given
        PaymentRequest invalidRequest = new PaymentRequest();
        invalidRequest.setAmount(null); // Invalid amount
        invalidRequest.setCurrency("USD");
        invalidRequest.setCustomerEmail("test@netflix.com");
        invalidRequest.setPaymentMethodId("pm_test_123");
        
        when(paymentService.createPaymentIntent(any(PaymentRequest.class), anyLong()))
            .thenThrow(new IllegalArgumentException("Invalid payment request"));
        
        // When
        mockMvc.perform(post("/api/v1/payments")
                .header("X-User-ID", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid payment request: Invalid payment request"));
    }
    
    @Test
    @DisplayName("Should handle missing user ID header end-to-end")
    void shouldHandleMissingUserIdHeaderEndToEnd() throws Exception {
        // When
        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle invalid user ID header end-to-end")
    void shouldHandleInvalidUserIdHeaderEndToEnd() throws Exception {
        // When
        mockMvc.perform(post("/api/v1/payments")
                .header("X-User-ID", "invalid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle negative user ID header end-to-end")
    void shouldHandleNegativeUserIdHeaderEndToEnd() throws Exception {
        // When
        mockMvc.perform(post("/api/v1/payments")
                .header("X-User-ID", "-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle invalid payment ID path variable end-to-end")
    void shouldHandleInvalidPaymentIdPathVariableEndToEnd() throws Exception {
        // When
        mockMvc.perform(post("/api/v1/payments/{paymentId}/confirm", "invalid"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle negative payment ID path variable end-to-end")
    void shouldHandleNegativePaymentIdPathVariableEndToEnd() throws Exception {
        // When
        mockMvc.perform(post("/api/v1/payments/{paymentId}/confirm", -1))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle invalid Stripe ID path variable end-to-end")
    void shouldHandleInvalidStripeIdPathVariableEndToEnd() throws Exception {
        // When
        mockMvc.perform(get("/api/v1/payments/stripe/{stripePaymentIntentId}", ""))
                .andExpect(status().isBadRequest());
    }
}
