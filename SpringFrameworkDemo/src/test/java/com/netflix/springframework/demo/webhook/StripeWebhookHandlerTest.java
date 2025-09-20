package com.netflix.springframework.demo.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.springframework.demo.config.StripeConfig;
import com.netflix.springframework.demo.service.StripePaymentService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * StripeWebhookHandler Unit Tests
 * 
 * This test class demonstrates Netflix production-grade unit testing with Mockito:
 * 1. Comprehensive test coverage for all webhook methods
 * 2. Mock-based testing for external dependencies
 * 3. Exception handling and error scenarios
 * 4. Edge cases and boundary conditions
 * 5. Security and validation testing
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
@DisplayName("StripeWebhookHandler Unit Tests")
class StripeWebhookHandlerTest {
    
    @Mock
    private StripeConfig stripeConfig;
    
    @Mock
    private StripePaymentService paymentService;
    
    @InjectMocks
    private StripeWebhookHandler webhookHandler;
    
    private MockHttpServletRequest request;
    private Event event;
    private PaymentIntent paymentIntent;
    
    @BeforeEach
    void setUp() {
        // Setup test data
        request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/v1/webhooks/stripe");
        request.setContentType("application/json");
        request.addHeader("Stripe-Signature", "t=1234567890,v1=test_signature");
        
        event = new Event();
        event.setId("evt_test_123");
        event.setType("payment_intent.succeeded");
        event.setCreated(System.currentTimeMillis() / 1000);
        
        paymentIntent = new PaymentIntent();
        paymentIntent.setId("pi_test_123");
        paymentIntent.setStatus("succeeded");
        paymentIntent.setAmount(10000L); // $100.00 in cents
    }
    
    @Test
    @DisplayName("Should handle webhook successfully")
    void shouldHandleWebhookSuccessfully() {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"payment_intent.succeeded\"}";
        request.setContent(payload.getBytes());
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            ResponseEntity<String> result = webhookHandler.handleStripeWebhook(request);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo("Webhook processed successfully");
        }
    }
    
    @Test
    @DisplayName("Should handle signature verification failure")
    void shouldHandleSignatureVerificationFailure() {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"payment_intent.succeeded\"}";
        request.setContent(payload.getBytes());
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenThrow(new SignatureVerificationException("Invalid signature", "test_signature"));
            
            // When
            ResponseEntity<String> result = webhookHandler.handleStripeWebhook(request);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(result.getBody()).isEqualTo("Invalid webhook signature");
        }
    }
    
    @Test
    @DisplayName("Should handle webhook processing exception")
    void shouldHandleWebhookProcessingException() {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"payment_intent.succeeded\"}";
        request.setContent(payload.getBytes());
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            ResponseEntity<String> result = webhookHandler.handleStripeWebhook(request);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(result.getBody()).isEqualTo("Failed to process webhook event");
        }
    }
    
    @Test
    @DisplayName("Should handle missing signature header")
    void shouldHandleMissingSignatureHeader() {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"payment_intent.succeeded\"}";
        request.setContent(payload.getBytes());
        request.removeHeader("Stripe-Signature");
        
        // When
        ResponseEntity<String> result = webhookHandler.handleStripeWebhook(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isEqualTo("Missing Stripe signature header");
    }
    
    @Test
    @DisplayName("Should handle empty payload")
    void shouldHandleEmptyPayload() {
        // Given
        request.setContent(new byte[0]);
        
        // When
        ResponseEntity<String> result = webhookHandler.handleStripeWebhook(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isEqualTo("Empty webhook payload");
    }
    
    @Test
    @DisplayName("Should handle invalid JSON payload")
    void shouldHandleInvalidJsonPayload() {
        // Given
        String invalidPayload = "invalid json";
        request.setContent(invalidPayload.getBytes());
        
        // When
        ResponseEntity<String> result = webhookHandler.handleStripeWebhook(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isEqualTo("Invalid JSON payload");
    }
    
    @Test
    @DisplayName("Should handle payment intent succeeded event")
    void shouldHandlePaymentIntentSucceededEvent() {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"payment_intent.succeeded\"}";
        request.setContent(payload.getBytes());
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            ResponseEntity<String> result = webhookHandler.handleStripeWebhook(request);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo("Webhook processed successfully");
        }
    }
    
    @Test
    @DisplayName("Should handle payment intent payment failed event")
    void shouldHandlePaymentIntentPaymentFailedEvent() {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"payment_intent.payment_failed\"}";
        request.setContent(payload.getBytes());
        
        event.setType("payment_intent.payment_failed");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            ResponseEntity<String> result = webhookHandler.handleStripeWebhook(request);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo("Webhook processed successfully");
        }
    }
    
    @Test
    @DisplayName("Should handle payment intent canceled event")
    void shouldHandlePaymentIntentCanceledEvent() {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"payment_intent.canceled\"}";
        request.setContent(payload.getBytes());
        
        event.setType("payment_intent.canceled");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            ResponseEntity<String> result = webhookHandler.handleStripeWebhook(request);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo("Webhook processed successfully");
        }
    }
    
    @Test
    @DisplayName("Should handle payment intent requires action event")
    void shouldHandlePaymentIntentRequiresActionEvent() {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"payment_intent.requires_action\"}";
        request.setContent(payload.getBytes());
        
        event.setType("payment_intent.requires_action");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            ResponseEntity<String> result = webhookHandler.handleStripeWebhook(request);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo("Webhook processed successfully");
        }
    }
    
    @Test
    @DisplayName("Should handle invoice payment succeeded event")
    void shouldHandleInvoicePaymentSucceededEvent() {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"invoice.payment_succeeded\"}";
        request.setContent(payload.getBytes());
        
        event.setType("invoice.payment_succeeded");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            ResponseEntity<String> result = webhookHandler.handleStripeWebhook(request);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo("Webhook processed successfully");
        }
    }
    
    @Test
    @DisplayName("Should handle invoice payment failed event")
    void shouldHandleInvoicePaymentFailedEvent() {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"invoice.payment_failed\"}";
        request.setContent(payload.getBytes());
        
        event.setType("invoice.payment_failed");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            ResponseEntity<String> result = webhookHandler.handleStripeWebhook(request);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo("Webhook processed successfully");
        }
    }
    
    @Test
    @DisplayName("Should handle customer subscription created event")
    void shouldHandleCustomerSubscriptionCreatedEvent() {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"customer.subscription.created\"}";
        request.setContent(payload.getBytes());
        
        event.setType("customer.subscription.created");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            ResponseEntity<String> result = webhookHandler.handleStripeWebhook(request);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo("Webhook processed successfully");
        }
    }
    
    @Test
    @DisplayName("Should handle customer subscription updated event")
    void shouldHandleCustomerSubscriptionUpdatedEvent() {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"customer.subscription.updated\"}";
        request.setContent(payload.getBytes());
        
        event.setType("customer.subscription.updated");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            ResponseEntity<String> result = webhookHandler.handleStripeWebhook(request);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo("Webhook processed successfully");
        }
    }
    
    @Test
    @DisplayName("Should handle customer subscription deleted event")
    void shouldHandleCustomerSubscriptionDeletedEvent() {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"customer.subscription.deleted\"}";
        request.setContent(payload.getBytes());
        
        event.setType("customer.subscription.deleted");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            ResponseEntity<String> result = webhookHandler.handleStripeWebhook(request);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo("Webhook processed successfully");
        }
    }
    
    @Test
    @DisplayName("Should handle customer subscription trial will end event")
    void shouldHandleCustomerSubscriptionTrialWillEndEvent() {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"customer.subscription.trial_will_end\"}";
        request.setContent(payload.getBytes());
        
        event.setType("customer.subscription.trial_will_end");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            ResponseEntity<String> result = webhookHandler.handleStripeWebhook(request);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo("Webhook processed successfully");
        }
    }
    
    @Test
    @DisplayName("Should handle invoice created event")
    void shouldHandleInvoiceCreatedEvent() {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"invoice.created\"}";
        request.setContent(payload.getBytes());
        
        event.setType("invoice.created");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            ResponseEntity<String> result = webhookHandler.handleStripeWebhook(request);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo("Webhook processed successfully");
        }
    }
    
    @Test
    @DisplayName("Should handle invoice finalized event")
    void shouldHandleInvoiceFinalizedEvent() {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"invoice.finalized\"}";
        request.setContent(payload.getBytes());
        
        event.setType("invoice.finalized");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            ResponseEntity<String> result = webhookHandler.handleStripeWebhook(request);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo("Webhook processed successfully");
        }
    }
    
    @Test
    @DisplayName("Should handle invoice payment action required event")
    void shouldHandleInvoicePaymentActionRequiredEvent() {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"invoice.payment_action_required\"}";
        request.setContent(payload.getBytes());
        
        event.setType("invoice.payment_action_required");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            ResponseEntity<String> result = webhookHandler.handleStripeWebhook(request);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo("Webhook processed successfully");
        }
    }
    
    @Test
    @DisplayName("Should handle payment method attached event")
    void shouldHandlePaymentMethodAttachedEvent() {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"payment_method.attached\"}";
        request.setContent(payload.getBytes());
        
        event.setType("payment_method.attached");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            ResponseEntity<String> result = webhookHandler.handleStripeWebhook(request);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo("Webhook processed successfully");
        }
    }
    
    @Test
    @DisplayName("Should handle payment method detached event")
    void shouldHandlePaymentMethodDetachedEvent() {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"payment_method.detached\"}";
        request.setContent(payload.getBytes());
        
        event.setType("payment_method.detached");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            ResponseEntity<String> result = webhookHandler.handleStripeWebhook(request);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo("Webhook processed successfully");
        }
    }
    
    @Test
    @DisplayName("Should handle setup intent succeeded event")
    void shouldHandleSetupIntentSucceededEvent() {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"setup_intent.succeeded\"}";
        request.setContent(payload.getBytes());
        
        event.setType("setup_intent.succeeded");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            ResponseEntity<String> result = webhookHandler.handleStripeWebhook(request);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo("Webhook processed successfully");
        }
    }
    
    @Test
    @DisplayName("Should handle setup intent setup failed event")
    void shouldHandleSetupIntentSetupFailedEvent() {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"setup_intent.setup_failed\"}";
        request.setContent(payload.getBytes());
        
        event.setType("setup_intent.setup_failed");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            ResponseEntity<String> result = webhookHandler.handleStripeWebhook(request);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo("Webhook processed successfully");
        }
    }
    
    @Test
    @DisplayName("Should handle unhandled webhook event type")
    void shouldHandleUnhandledWebhookEventType() {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"unhandled.event.type\"}";
        request.setContent(payload.getBytes());
        
        event.setType("unhandled.event.type");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            ResponseEntity<String> result = webhookHandler.handleStripeWebhook(request);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo("Webhook processed successfully");
        }
    }
    
    @Test
    @DisplayName("Should handle webhook with null event type")
    void shouldHandleWebhookWithNullEventType() {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":null}";
        request.setContent(payload.getBytes());
        
        event.setType(null);
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            ResponseEntity<String> result = webhookHandler.handleStripeWebhook(request);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(result.getBody()).isEqualTo("Failed to process webhook event");
        }
    }
    
    @Test
    @DisplayName("Should handle webhook with empty event type")
    void shouldHandleWebhookWithEmptyEventType() {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"\"}";
        request.setContent(payload.getBytes());
        
        event.setType("");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            ResponseEntity<String> result = webhookHandler.handleStripeWebhook(request);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(result.getBody()).isEqualTo("Failed to process webhook event");
        }
    }
}
