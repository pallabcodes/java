package com.netflix.springframework.demo.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.netflix.springframework.demo.config.StripeConfig;
import com.netflix.springframework.demo.dto.PaymentRequest;
import com.netflix.springframework.demo.entity.PaymentEntity;
import com.netflix.springframework.demo.entity.PaymentEntity.PaymentStatus;
import com.netflix.springframework.demo.service.StripePaymentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Stripe Contract Tests
 * 
 * This test class demonstrates Netflix production-grade contract testing with WireMock:
 * 1. Stripe API contract validation
 * 2. Mock Stripe API responses
 * 3. Contract compliance testing
 * 4. API integration testing
 * 5. Error scenario testing
 * 
 * For C/C++ engineers:
 * - Contract tests are like API contract validation in C++
 * - WireMock is like mock HTTP servers in C++
 * - @SpringBootTest is like testing the entire application in C++
 * - Contract testing ensures API compatibility
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Stripe Contract Tests")
class StripeContractTest {
    
    @Autowired
    private StripePaymentService paymentService;
    
    @MockBean
    private StripeConfig stripeConfig;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private WireMockServer wireMockServer;
    private PaymentRequest paymentRequest;
    private PaymentEntity paymentEntity;
    
    @BeforeEach
    void setUp() {
        // Setup WireMock server
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);
        
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
        
        // Setup Stripe config
        when(stripeConfig.getDefaultCurrency()).thenReturn("USD");
        when(stripeConfig.getDefaultDescription()).thenReturn("Test payment");
    }
    
    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }
    
    @Test
    @DisplayName("Should create payment intent with Stripe API contract")
    void shouldCreatePaymentIntentWithStripeApiContract() {
        // Given
        String paymentIntentResponse = """
            {
                "id": "pi_test_123",
                "object": "payment_intent",
                "amount": 10000,
                "currency": "usd",
                "status": "requires_payment_method",
                "client_secret": "pi_test_123_secret_test123"
            }
            """;
        
        wireMockServer.stubFor(post(urlEqualTo("/v1/payment_intents"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(paymentIntentResponse)));
        
        when(paymentService.createPaymentIntent(any(PaymentRequest.class), anyLong()))
            .thenReturn(paymentEntity);
        
        // When
        PaymentEntity result = paymentService.createPaymentIntent(paymentRequest, 1L);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStripePaymentIntentId()).isEqualTo("pi_test_123");
        assertThat(result.getAmount()).isEqualTo(new BigDecimal("100.00"));
        assertThat(result.getCurrency()).isEqualTo("USD");
        
        // Verify WireMock was called
        wireMockServer.verify(postRequestedFor(urlEqualTo("/v1/payment_intents"))
                .withHeader("Authorization", equalTo("Bearer sk_test_123"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded")));
    }
    
    @Test
    @DisplayName("Should confirm payment intent with Stripe API contract")
    void shouldConfirmPaymentIntentWithStripeApiContract() {
        // Given
        String paymentIntentResponse = """
            {
                "id": "pi_test_123",
                "object": "payment_intent",
                "amount": 10000,
                "currency": "usd",
                "status": "succeeded",
                "client_secret": "pi_test_123_secret_test123"
            }
            """;
        
        wireMockServer.stubFor(post(urlEqualTo("/v1/payment_intents/pi_test_123/confirm"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(paymentIntentResponse)));
        
        when(paymentService.confirmPaymentIntent(anyLong()))
            .thenReturn(paymentEntity);
        
        // When
        PaymentEntity result = paymentService.confirmPaymentIntent(1L);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStripePaymentIntentId()).isEqualTo("pi_test_123");
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
        
        // Verify WireMock was called
        wireMockServer.verify(postRequestedFor(urlEqualTo("/v1/payment_intents/pi_test_123/confirm"))
                .withHeader("Authorization", equalTo("Bearer sk_test_123"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded")));
    }
    
    @Test
    @DisplayName("Should cancel payment intent with Stripe API contract")
    void shouldCancelPaymentIntentWithStripeApiContract() {
        // Given
        String paymentIntentResponse = """
            {
                "id": "pi_test_123",
                "object": "payment_intent",
                "amount": 10000,
                "currency": "usd",
                "status": "canceled",
                "client_secret": "pi_test_123_secret_test123"
            }
            """;
        
        wireMockServer.stubFor(post(urlEqualTo("/v1/payment_intents/pi_test_123/cancel"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(paymentIntentResponse)));
        
        paymentEntity.setStatus(PaymentStatus.CANCELLED);
        when(paymentService.cancelPaymentIntent(anyLong()))
            .thenReturn(paymentEntity);
        
        // When
        PaymentEntity result = paymentService.cancelPaymentIntent(1L);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStripePaymentIntentId()).isEqualTo("pi_test_123");
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        
        // Verify WireMock was called
        wireMockServer.verify(postRequestedFor(urlEqualTo("/v1/payment_intents/pi_test_123/cancel"))
                .withHeader("Authorization", equalTo("Bearer sk_test_123"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded")));
    }
    
    @Test
    @DisplayName("Should refund payment with Stripe API contract")
    void shouldRefundPaymentWithStripeApiContract() {
        // Given
        String refundResponse = """
            {
                "id": "re_test_123",
                "object": "refund",
                "amount": 5000,
                "currency": "usd",
                "status": "succeeded",
                "payment_intent": "pi_test_123"
            }
            """;
        
        wireMockServer.stubFor(post(urlEqualTo("/v1/refunds"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(refundResponse)));
        
        paymentEntity.setStatus(PaymentStatus.REFUNDED);
        when(paymentService.refundPayment(anyLong(), any(BigDecimal.class), anyString()))
            .thenReturn(paymentEntity);
        
        // When
        PaymentEntity result = paymentService.refundPayment(1L, new BigDecimal("50.00"), "Customer request");
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStripePaymentIntentId()).isEqualTo("pi_test_123");
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        
        // Verify WireMock was called
        wireMockServer.verify(postRequestedFor(urlEqualTo("/v1/refunds"))
                .withHeader("Authorization", equalTo("Bearer sk_test_123"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded")));
    }
    
    @Test
    @DisplayName("Should handle Stripe API error response")
    void shouldHandleStripeApiErrorResponse() {
        // Given
        String errorResponse = """
            {
                "error": {
                    "type": "card_error",
                    "code": "card_declined",
                    "message": "Your card was declined."
                }
            }
            """;
        
        wireMockServer.stubFor(post(urlEqualTo("/v1/payment_intents"))
                .willReturn(aResponse()
                        .withStatus(402)
                        .withHeader("Content-Type", "application/json")
                        .withBody(errorResponse)));
        
        when(paymentService.createPaymentIntent(any(PaymentRequest.class), anyLong()))
            .thenThrow(new RuntimeException("Stripe API error: Your card was declined."));
        
        // When & Then
        assertThatThrownBy(() -> paymentService.createPaymentIntent(paymentRequest, 1L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Stripe API error: Your card was declined.");
        
        // Verify WireMock was called
        wireMockServer.verify(postRequestedFor(urlEqualTo("/v1/payment_intents"))
                .withHeader("Authorization", equalTo("Bearer sk_test_123"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded")));
    }
    
    @Test
    @DisplayName("Should handle Stripe API timeout")
    void shouldHandleStripeApiTimeout() {
        // Given
        wireMockServer.stubFor(post(urlEqualTo("/v1/payment_intents"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withFixedDelay(10000) // 10 second delay
                        .withBody("{\"id\": \"pi_test_123\"}")));
        
        when(paymentService.createPaymentIntent(any(PaymentRequest.class), anyLong()))
            .thenThrow(new RuntimeException("Stripe API timeout"));
        
        // When & Then
        assertThatThrownBy(() -> paymentService.createPaymentIntent(paymentRequest, 1L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Stripe API timeout");
    }
    
    @Test
    @DisplayName("Should handle Stripe API rate limiting")
    void shouldHandleStripeApiRateLimiting() {
        // Given
        String rateLimitResponse = """
            {
                "error": {
                    "type": "rate_limit_error",
                    "message": "Too many requests hit the API too quickly."
                }
            }
            """;
        
        wireMockServer.stubFor(post(urlEqualTo("/v1/payment_intents"))
                .willReturn(aResponse()
                        .withStatus(429)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Retry-After", "60")
                        .withBody(rateLimitResponse)));
        
        when(paymentService.createPaymentIntent(any(PaymentRequest.class), anyLong()))
            .thenThrow(new RuntimeException("Stripe API rate limit exceeded"));
        
        // When & Then
        assertThatThrownBy(() -> paymentService.createPaymentIntent(paymentRequest, 1L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Stripe API rate limit exceeded");
        
        // Verify WireMock was called
        wireMockServer.verify(postRequestedFor(urlEqualTo("/v1/payment_intents"))
                .withHeader("Authorization", equalTo("Bearer sk_test_123"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded")));
    }
    
    @Test
    @DisplayName("Should handle Stripe API authentication error")
    void shouldHandleStripeApiAuthenticationError() {
        // Given
        String authErrorResponse = """
            {
                "error": {
                    "type": "authentication_error",
                    "message": "No API key provided."
                }
            }
            """;
        
        wireMockServer.stubFor(post(urlEqualTo("/v1/payment_intents"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody(authErrorResponse)));
        
        when(paymentService.createPaymentIntent(any(PaymentRequest.class), anyLong()))
            .thenThrow(new RuntimeException("Stripe API authentication error"));
        
        // When & Then
        assertThatThrownBy(() -> paymentService.createPaymentIntent(paymentRequest, 1L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Stripe API authentication error");
        
        // Verify WireMock was called
        wireMockServer.verify(postRequestedFor(urlEqualTo("/v1/payment_intents"))
                .withHeader("Authorization", equalTo("Bearer sk_test_123"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded")));
    }
    
    @Test
    @DisplayName("Should handle Stripe API invalid request error")
    void shouldHandleStripeApiInvalidRequestError() {
        // Given
        String invalidRequestResponse = """
            {
                "error": {
                    "type": "invalid_request_error",
                    "message": "Invalid request parameters."
                }
            }
            """;
        
        wireMockServer.stubFor(post(urlEqualTo("/v1/payment_intents"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(invalidRequestResponse)));
        
        when(paymentService.createPaymentIntent(any(PaymentRequest.class), anyLong()))
            .thenThrow(new RuntimeException("Stripe API invalid request error"));
        
        // When & Then
        assertThatThrownBy(() -> paymentService.createPaymentIntent(paymentRequest, 1L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Stripe API invalid request error");
        
        // Verify WireMock was called
        wireMockServer.verify(postRequestedFor(urlEqualTo("/v1/payment_intents"))
                .withHeader("Authorization", equalTo("Bearer sk_test_123"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded")));
    }
    
    @Test
    @DisplayName("Should handle Stripe API server error")
    void shouldHandleStripeApiServerError() {
        // Given
        String serverErrorResponse = """
            {
                "error": {
                    "type": "api_error",
                    "message": "Something went wrong on Stripe's end."
                }
            }
            """;
        
        wireMockServer.stubFor(post(urlEqualTo("/v1/payment_intents"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody(serverErrorResponse)));
        
        when(paymentService.createPaymentIntent(any(PaymentRequest.class), anyLong()))
            .thenThrow(new RuntimeException("Stripe API server error"));
        
        // When & Then
        assertThatThrownBy(() -> paymentService.createPaymentIntent(paymentRequest, 1L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Stripe API server error");
        
        // Verify WireMock was called
        wireMockServer.verify(postRequestedFor(urlEqualTo("/v1/payment_intents"))
                .withHeader("Authorization", equalTo("Bearer sk_test_123"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded")));
    }
    
    @Test
    @DisplayName("Should handle Stripe API network error")
    void shouldHandleStripeApiNetworkError() {
        // Given
        wireMockServer.stubFor(post(urlEqualTo("/v1/payment_intents"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withFault(com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER)));
        
        when(paymentService.createPaymentIntent(any(PaymentRequest.class), anyLong()))
            .thenThrow(new RuntimeException("Stripe API network error"));
        
        // When & Then
        assertThatThrownBy(() -> paymentService.createPaymentIntent(paymentRequest, 1L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Stripe API network error");
    }
    
    @Test
    @DisplayName("Should handle Stripe API malformed response")
    void shouldHandleStripeApiMalformedResponse() {
        // Given
        wireMockServer.stubFor(post(urlEqualTo("/v1/payment_intents"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("Invalid JSON response")));
        
        when(paymentService.createPaymentIntent(any(PaymentRequest.class), anyLong()))
            .thenThrow(new RuntimeException("Stripe API malformed response"));
        
        // When & Then
        assertThatThrownBy(() -> paymentService.createPaymentIntent(paymentRequest, 1L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Stripe API malformed response");
    }
    
    @Test
    @DisplayName("Should handle Stripe API empty response")
    void shouldHandleStripeApiEmptyResponse() {
        // Given
        wireMockServer.stubFor(post(urlEqualTo("/v1/payment_intents"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("")));
        
        when(paymentService.createPaymentIntent(any(PaymentRequest.class), anyLong()))
            .thenThrow(new RuntimeException("Stripe API empty response"));
        
        // When & Then
        assertThatThrownBy(() -> paymentService.createPaymentIntent(paymentRequest, 1L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Stripe API empty response");
    }
}
