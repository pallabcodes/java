package com.netflix.springframework.demo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.springframework.demo.dto.PaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security Test Suite
 * 
 * This test class demonstrates Netflix production-grade security testing:
 * 1. JWT token validation and security
 * 2. User authorization and access control
 * 3. Input validation and sanitization
 * 4. API security and protection
 * 5. SQL injection and XSS prevention
 * 
 * For C/C++ engineers:
 * - Security tests are like vulnerability testing in C++
 * - JWT validation is like authentication in C++ web frameworks
 * - Input validation is like buffer overflow prevention in C++
 * - Authorization is like access control in C++ systems
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Security Test Suite")
class SecurityTestSuite {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private PaymentRequest validPaymentRequest;
    private String validJwtToken;
    private String expiredJwtToken;
    private String invalidJwtToken;
    
    @BeforeEach
    void setUp() {
        // Setup valid payment request
        validPaymentRequest = new PaymentRequest();
        validPaymentRequest.setAmount(new BigDecimal("100.00"));
        validPaymentRequest.setCurrency("USD");
        validPaymentRequest.setCustomerEmail("test@netflix.com");
        validPaymentRequest.setPaymentMethodId("pm_test_123");
        validPaymentRequest.setDescription("Test payment");
        
        // Setup JWT tokens
        validJwtToken = generateValidJwtToken();
        expiredJwtToken = generateExpiredJwtToken();
        invalidJwtToken = "invalid.jwt.token";
    }
    
    @Test
    @DisplayName("Should reject requests without JWT token")
    void shouldRejectRequestsWithoutJwtToken() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPaymentRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Missing or invalid JWT token"));
    }
    
    @Test
    @DisplayName("Should reject requests with invalid JWT token")
    void shouldRejectRequestsWithInvalidJwtToken() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/payments")
                .header("Authorization", "Bearer " + invalidJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPaymentRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid JWT token"));
    }
    
    @Test
    @DisplayName("Should reject requests with expired JWT token")
    void shouldRejectRequestsWithExpiredJwtToken() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/payments")
                .header("Authorization", "Bearer " + expiredJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPaymentRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("JWT token has expired"));
    }
    
    @Test
    @DisplayName("Should accept requests with valid JWT token")
    void shouldAcceptRequestsWithValidJwtToken() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/payments")
                .header("Authorization", "Bearer " + validJwtToken)
                .header("X-User-ID", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPaymentRequest)))
                .andExpect(status().isCreated());
    }
    
    @Test
    @DisplayName("Should validate user authorization for payment access")
    void shouldValidateUserAuthorizationForPaymentAccess() throws Exception {
        // Given
        String otherUserJwtToken = generateJwtTokenForUser("other_user");
        
        // When & Then
        mockMvc.perform(get("/api/v1/payments/1")
                .header("Authorization", "Bearer " + otherUserJwtToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied: User not authorized to access this payment"));
    }
    
    @Test
    @DisplayName("Should prevent SQL injection in payment queries")
    void shouldPreventSqlInjectionInPaymentQueries() throws Exception {
        // Given
        String sqlInjectionPayload = "'; DROP TABLE payments; --";
        
        // When & Then
        mockMvc.perform(get("/api/v1/payments/stripe/" + sqlInjectionPayload)
                .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid input: SQL injection attempt detected"));
    }
    
    @Test
    @DisplayName("Should prevent XSS attacks in payment data")
    void shouldPreventXssAttacksInPaymentData() throws Exception {
        // Given
        PaymentRequest xssPaymentRequest = new PaymentRequest();
        xssPaymentRequest.setAmount(new BigDecimal("100.00"));
        xssPaymentRequest.setCurrency("USD");
        xssPaymentRequest.setCustomerEmail("<script>alert('xss')</script>@netflix.com");
        xssPaymentRequest.setPaymentMethodId("pm_test_123");
        xssPaymentRequest.setDescription("<script>alert('xss')</script>");
        
        // When & Then
        mockMvc.perform(post("/api/v1/payments")
                .header("Authorization", "Bearer " + validJwtToken)
                .header("X-User-ID", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(xssPaymentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid input: XSS attempt detected"));
    }
    
    @Test
    @DisplayName("Should validate payment amount boundaries")
    void shouldValidatePaymentAmountBoundaries() throws Exception {
        // Test minimum payment amount
        PaymentRequest minAmountRequest = createPaymentRequestWithAmount(new BigDecimal("0.01"));
        mockMvc.perform(post("/api/v1/payments")
                .header("Authorization", "Bearer " + validJwtToken)
                .header("X-User-ID", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(minAmountRequest)))
                .andExpect(status().isCreated());
        
        // Test maximum payment amount
        PaymentRequest maxAmountRequest = createPaymentRequestWithAmount(new BigDecimal("999999.99"));
        mockMvc.perform(post("/api/v1/payments")
                .header("Authorization", "Bearer " + validJwtToken)
                .header("X-User-ID", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maxAmountRequest)))
                .andExpect(status().isCreated());
        
        // Test amount below minimum
        PaymentRequest belowMinRequest = createPaymentRequestWithAmount(new BigDecimal("0.00"));
        mockMvc.perform(post("/api/v1/payments")
                .header("Authorization", "Bearer " + validJwtToken)
                .header("X-User-ID", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(belowMinRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid payment amount: Amount must be greater than 0"));
        
        // Test amount above maximum
        PaymentRequest aboveMaxRequest = createPaymentRequestWithAmount(new BigDecimal("1000000.00"));
        mockMvc.perform(post("/api/v1/payments")
                .header("Authorization", "Bearer " + validJwtToken)
                .header("X-User-ID", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aboveMaxRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid payment amount: Amount exceeds maximum limit"));
    }
    
    @Test
    @DisplayName("Should validate email format and domain")
    void shouldValidateEmailFormatAndDomain() throws Exception {
        // Test valid email formats
        String[] validEmails = {
            "test@netflix.com",
            "user.name@netflix.com",
            "user+tag@netflix.com",
            "user123@netflix.com"
        };
        
        for (String email : validEmails) {
            PaymentRequest validEmailRequest = createPaymentRequestWithEmail(email);
            mockMvc.perform(post("/api/v1/payments")
                    .header("Authorization", "Bearer " + validJwtToken)
                    .header("X-User-ID", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validEmailRequest)))
                    .andExpect(status().isCreated());
        }
        
        // Test invalid email formats
        String[] invalidEmails = {
            "invalid-email",
            "@netflix.com",
            "test@",
            "test@invalid-domain.com",
            "test..test@netflix.com"
        };
        
        for (String email : invalidEmails) {
            PaymentRequest invalidEmailRequest = createPaymentRequestWithEmail(email);
            mockMvc.perform(post("/api/v1/payments")
                    .header("Authorization", "Bearer " + validJwtToken)
                    .header("X-User-ID", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidEmailRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Invalid email format"));
        }
    }
    
    @Test
    @DisplayName("Should validate payment method ID format")
    void shouldValidatePaymentMethodIdFormat() throws Exception {
        // Test valid payment method ID
        PaymentRequest validPmRequest = createPaymentRequestWithPaymentMethodId("pm_test_1234567890");
        mockMvc.perform(post("/api/v1/payments")
                .header("Authorization", "Bearer " + validJwtToken)
                .header("X-User-ID", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPmRequest)))
                .andExpect(status().isCreated());
        
        // Test invalid payment method ID
        PaymentRequest invalidPmRequest = createPaymentRequestWithPaymentMethodId("invalid_pm_id");
        mockMvc.perform(post("/api/v1/payments")
                .header("Authorization", "Bearer " + validJwtToken)
                .header("X-User-ID", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPmRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid payment method ID format"));
    }
    
    @Test
    @DisplayName("Should validate currency code")
    void shouldValidateCurrencyCode() throws Exception {
        // Test valid currency codes
        String[] validCurrencies = {"USD", "EUR", "GBP", "CAD", "AUD"};
        
        for (String currency : validCurrencies) {
            PaymentRequest validCurrencyRequest = createPaymentRequestWithCurrency(currency);
            mockMvc.perform(post("/api/v1/payments")
                    .header("Authorization", "Bearer " + validJwtToken)
                    .header("X-User-ID", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCurrencyRequest)))
                    .andExpect(status().isCreated());
        }
        
        // Test invalid currency code
        PaymentRequest invalidCurrencyRequest = createPaymentRequestWithCurrency("INVALID");
        mockMvc.perform(post("/api/v1/payments")
                .header("Authorization", "Bearer " + validJwtToken)
                .header("X-User-ID", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCurrencyRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid currency code"));
    }
    
    @Test
    @DisplayName("Should validate request rate limiting")
    void shouldValidateRequestRateLimiting() throws Exception {
        // Send requests exceeding rate limit
        for (int i = 0; i < 101; i++) { // Rate limit is 100 requests per minute
            mockMvc.perform(post("/api/v1/payments")
                    .header("Authorization", "Bearer " + validJwtToken)
                    .header("X-User-ID", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validPaymentRequest)))
                    .andExpect(i < 100 ? status().isCreated() : status().isTooManyRequests());
        }
    }
    
    @Test
    @DisplayName("Should validate request size limits")
    void shouldValidateRequestSizeLimits() throws Exception {
        // Create large request payload
        PaymentRequest largeRequest = new PaymentRequest();
        largeRequest.setAmount(new BigDecimal("100.00"));
        largeRequest.setCurrency("USD");
        largeRequest.setCustomerEmail("test@netflix.com");
        largeRequest.setPaymentMethodId("pm_test_123");
        largeRequest.setDescription("x".repeat(10000)); // Large description
        
        // When & Then
        mockMvc.perform(post("/api/v1/payments")
                .header("Authorization", "Bearer " + validJwtToken)
                .header("X-User-ID", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(largeRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Request payload too large"));
    }
    
    @Test
    @DisplayName("Should validate content type")
    void shouldValidateContentType() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/payments")
                .header("Authorization", "Bearer " + validJwtToken)
                .header("X-User-ID", "1")
                .contentType(MediaType.TEXT_PLAIN)
                .content("invalid content"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.error").value("Unsupported media type"));
    }
    
    @Test
    @DisplayName("Should validate CORS headers")
    void shouldValidateCorsHeaders() throws Exception {
        // When & Then
        mockMvc.perform(options("/api/v1/payments")
                .header("Origin", "https://malicious-site.com")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
    
    // Helper methods
    
    private String generateValidJwtToken() {
        // In a real implementation, this would generate a valid JWT token
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    }
    
    private String generateExpiredJwtToken() {
        // In a real implementation, this would generate an expired JWT token
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjE1MTYyMzkwMjJ9.invalid";
    }
    
    private String generateJwtTokenForUser(String userId) {
        // In a real implementation, this would generate a JWT token for a specific user
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI" + userId + "IiwibmFtZSI6IlVzZXIiLCJpYXQiOjE1MTYyMzkwMjJ9.invalid";
    }
    
    private PaymentRequest createPaymentRequestWithAmount(BigDecimal amount) {
        PaymentRequest request = new PaymentRequest();
        request.setAmount(amount);
        request.setCurrency("USD");
        request.setCustomerEmail("test@netflix.com");
        request.setPaymentMethodId("pm_test_123");
        request.setDescription("Test payment");
        return request;
    }
    
    private PaymentRequest createPaymentRequestWithEmail(String email) {
        PaymentRequest request = new PaymentRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("USD");
        request.setCustomerEmail(email);
        request.setPaymentMethodId("pm_test_123");
        request.setDescription("Test payment");
        return request;
    }
    
    private PaymentRequest createPaymentRequestWithPaymentMethodId(String paymentMethodId) {
        PaymentRequest request = new PaymentRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("USD");
        request.setCustomerEmail("test@netflix.com");
        request.setPaymentMethodId(paymentMethodId);
        request.setDescription("Test payment");
        return request;
    }
    
    private PaymentRequest createPaymentRequestWithCurrency(String currency) {
        PaymentRequest request = new PaymentRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency(currency);
        request.setCustomerEmail("test@netflix.com");
        request.setPaymentMethodId("pm_test_123");
        request.setDescription("Test payment");
        return request;
    }
}
