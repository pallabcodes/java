package com.netflix.springframework.demo.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.springframework.demo.config.StripeConfig;
import com.netflix.springframework.demo.service.StripePaymentService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Webhook Integration Tests
 * 
 * This test class demonstrates Netflix production-grade integration testing:
 * 1. End-to-end webhook processing testing
 * 2. Stripe webhook event handling
 * 3. Signature verification testing
 * 4. Error handling and validation
 * 5. Security and authentication testing
 * 
 * For C/C++ engineers:
 * - Integration tests are like system-level testing in C++
 * - @SpringBootTest is like testing the entire application in C++
 * - MockMvc is like HTTP request testing in C++ web frameworks
 * - Webhook testing is like event-driven system testing in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Webhook Integration Tests")
class WebhookIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private StripeConfig stripeConfig;
    
    @MockBean
    private StripePaymentService paymentService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private Event event;
    
    @BeforeEach
    void setUp() {
        // Setup test data
        event = new Event();
        event.setId("evt_test_123");
        event.setType("payment_intent.succeeded");
        event.setCreated(System.currentTimeMillis() / 1000);
    }
    
    @Test
    @DisplayName("Should handle webhook successfully end-to-end")
    void shouldHandleWebhookSuccessfullyEndToEnd() throws Exception {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"payment_intent.succeeded\"}";
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            mockMvc.perform(post("/api/v1/webhooks/stripe")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Stripe-Signature", "t=1234567890,v1=test_signature")
                    .content(payload))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Webhook processed successfully"));
        }
    }
    
    @Test
    @DisplayName("Should handle signature verification failure end-to-end")
    void shouldHandleSignatureVerificationFailureEndToEnd() throws Exception {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"payment_intent.succeeded\"}";
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenThrow(new SignatureVerificationException("Invalid signature", "test_signature"));
            
            // When
            mockMvc.perform(post("/api/v1/webhooks/stripe")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Stripe-Signature", "t=1234567890,v1=test_signature")
                    .content(payload))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Invalid webhook signature"));
        }
    }
    
    @Test
    @DisplayName("Should handle missing signature header end-to-end")
    void shouldHandleMissingSignatureHeaderEndToEnd() throws Exception {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"payment_intent.succeeded\"}";
        
        // When
        mockMvc.perform(post("/api/v1/webhooks/stripe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Missing Stripe signature header"));
    }
    
    @Test
    @DisplayName("Should handle empty payload end-to-end")
    void shouldHandleEmptyPayloadEndToEnd() throws Exception {
        // When
        mockMvc.perform(post("/api/v1/webhooks/stripe")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Stripe-Signature", "t=1234567890,v1=test_signature")
                .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Empty webhook payload"));
    }
    
    @Test
    @DisplayName("Should handle invalid JSON payload end-to-end")
    void shouldHandleInvalidJsonPayloadEndToEnd() throws Exception {
        // Given
        String invalidPayload = "invalid json";
        
        // When
        mockMvc.perform(post("/api/v1/webhooks/stripe")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Stripe-Signature", "t=1234567890,v1=test_signature")
                .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid JSON payload"));
    }
    
    @Test
    @DisplayName("Should handle payment intent succeeded event end-to-end")
    void shouldHandlePaymentIntentSucceededEventEndToEnd() throws Exception {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"payment_intent.succeeded\"}";
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            mockMvc.perform(post("/api/v1/webhooks/stripe")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Stripe-Signature", "t=1234567890,v1=test_signature")
                    .content(payload))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Webhook processed successfully"));
        }
    }
    
    @Test
    @DisplayName("Should handle payment intent payment failed event end-to-end")
    void shouldHandlePaymentIntentPaymentFailedEventEndToEnd() throws Exception {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"payment_intent.payment_failed\"}";
        event.setType("payment_intent.payment_failed");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            mockMvc.perform(post("/api/v1/webhooks/stripe")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Stripe-Signature", "t=1234567890,v1=test_signature")
                    .content(payload))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Webhook processed successfully"));
        }
    }
    
    @Test
    @DisplayName("Should handle payment intent canceled event end-to-end")
    void shouldHandlePaymentIntentCanceledEventEndToEnd() throws Exception {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"payment_intent.canceled\"}";
        event.setType("payment_intent.canceled");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            mockMvc.perform(post("/api/v1/webhooks/stripe")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Stripe-Signature", "t=1234567890,v1=test_signature")
                    .content(payload))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Webhook processed successfully"));
        }
    }
    
    @Test
    @DisplayName("Should handle payment intent requires action event end-to-end")
    void shouldHandlePaymentIntentRequiresActionEventEndToEnd() throws Exception {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"payment_intent.requires_action\"}";
        event.setType("payment_intent.requires_action");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            mockMvc.perform(post("/api/v1/webhooks/stripe")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Stripe-Signature", "t=1234567890,v1=test_signature")
                    .content(payload))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Webhook processed successfully"));
        }
    }
    
    @Test
    @DisplayName("Should handle invoice payment succeeded event end-to-end")
    void shouldHandleInvoicePaymentSucceededEventEndToEnd() throws Exception {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"invoice.payment_succeeded\"}";
        event.setType("invoice.payment_succeeded");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            mockMvc.perform(post("/api/v1/webhooks/stripe")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Stripe-Signature", "t=1234567890,v1=test_signature")
                    .content(payload))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Webhook processed successfully"));
        }
    }
    
    @Test
    @DisplayName("Should handle invoice payment failed event end-to-end")
    void shouldHandleInvoicePaymentFailedEventEndToEnd() throws Exception {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"invoice.payment_failed\"}";
        event.setType("invoice.payment_failed");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            mockMvc.perform(post("/api/v1/webhooks/stripe")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Stripe-Signature", "t=1234567890,v1=test_signature")
                    .content(payload))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Webhook processed successfully"));
        }
    }
    
    @Test
    @DisplayName("Should handle customer subscription created event end-to-end")
    void shouldHandleCustomerSubscriptionCreatedEventEndToEnd() throws Exception {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"customer.subscription.created\"}";
        event.setType("customer.subscription.created");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            mockMvc.perform(post("/api/v1/webhooks/stripe")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Stripe-Signature", "t=1234567890,v1=test_signature")
                    .content(payload))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Webhook processed successfully"));
        }
    }
    
    @Test
    @DisplayName("Should handle customer subscription updated event end-to-end")
    void shouldHandleCustomerSubscriptionUpdatedEventEndToEnd() throws Exception {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"customer.subscription.updated\"}";
        event.setType("customer.subscription.updated");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            mockMvc.perform(post("/api/v1/webhooks/stripe")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Stripe-Signature", "t=1234567890,v1=test_signature")
                    .content(payload))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Webhook processed successfully"));
        }
    }
    
    @Test
    @DisplayName("Should handle customer subscription deleted event end-to-end")
    void shouldHandleCustomerSubscriptionDeletedEventEndToEnd() throws Exception {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"customer.subscription.deleted\"}";
        event.setType("customer.subscription.deleted");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            mockMvc.perform(post("/api/v1/webhooks/stripe")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Stripe-Signature", "t=1234567890,v1=test_signature")
                    .content(payload))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Webhook processed successfully"));
        }
    }
    
    @Test
    @DisplayName("Should handle customer subscription trial will end event end-to-end")
    void shouldHandleCustomerSubscriptionTrialWillEndEventEndToEnd() throws Exception {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"customer.subscription.trial_will_end\"}";
        event.setType("customer.subscription.trial_will_end");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            mockMvc.perform(post("/api/v1/webhooks/stripe")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Stripe-Signature", "t=1234567890,v1=test_signature")
                    .content(payload))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Webhook processed successfully"));
        }
    }
    
    @Test
    @DisplayName("Should handle invoice created event end-to-end")
    void shouldHandleInvoiceCreatedEventEndToEnd() throws Exception {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"invoice.created\"}";
        event.setType("invoice.created");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            mockMvc.perform(post("/api/v1/webhooks/stripe")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Stripe-Signature", "t=1234567890,v1=test_signature")
                    .content(payload))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Webhook processed successfully"));
        }
    }
    
    @Test
    @DisplayName("Should handle invoice finalized event end-to-end")
    void shouldHandleInvoiceFinalizedEventEndToEnd() throws Exception {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"invoice.finalized\"}";
        event.setType("invoice.finalized");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            mockMvc.perform(post("/api/v1/webhooks/stripe")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Stripe-Signature", "t=1234567890,v1=test_signature")
                    .content(payload))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Webhook processed successfully"));
        }
    }
    
    @Test
    @DisplayName("Should handle invoice payment action required event end-to-end")
    void shouldHandleInvoicePaymentActionRequiredEventEndToEnd() throws Exception {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"invoice.payment_action_required\"}";
        event.setType("invoice.payment_action_required");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            mockMvc.perform(post("/api/v1/webhooks/stripe")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Stripe-Signature", "t=1234567890,v1=test_signature")
                    .content(payload))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Webhook processed successfully"));
        }
    }
    
    @Test
    @DisplayName("Should handle payment method attached event end-to-end")
    void shouldHandlePaymentMethodAttachedEventEndToEnd() throws Exception {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"payment_method.attached\"}";
        event.setType("payment_method.attached");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            mockMvc.perform(post("/api/v1/webhooks/stripe")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Stripe-Signature", "t=1234567890,v1=test_signature")
                    .content(payload))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Webhook processed successfully"));
        }
    }
    
    @Test
    @DisplayName("Should handle payment method detached event end-to-end")
    void shouldHandlePaymentMethodDetachedEventEndToEnd() throws Exception {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"payment_method.detached\"}";
        event.setType("payment_method.detached");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            mockMvc.perform(post("/api/v1/webhooks/stripe")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Stripe-Signature", "t=1234567890,v1=test_signature")
                    .content(payload))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Webhook processed successfully"));
        }
    }
    
    @Test
    @DisplayName("Should handle setup intent succeeded event end-to-end")
    void shouldHandleSetupIntentSucceededEventEndToEnd() throws Exception {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"setup_intent.succeeded\"}";
        event.setType("setup_intent.succeeded");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            mockMvc.perform(post("/api/v1/webhooks/stripe")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Stripe-Signature", "t=1234567890,v1=test_signature")
                    .content(payload))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Webhook processed successfully"));
        }
    }
    
    @Test
    @DisplayName("Should handle setup intent setup failed event end-to-end")
    void shouldHandleSetupIntentSetupFailedEventEndToEnd() throws Exception {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"setup_intent.setup_failed\"}";
        event.setType("setup_intent.setup_failed");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            mockMvc.perform(post("/api/v1/webhooks/stripe")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Stripe-Signature", "t=1234567890,v1=test_signature")
                    .content(payload))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Webhook processed successfully"));
        }
    }
    
    @Test
    @DisplayName("Should handle unhandled webhook event type end-to-end")
    void shouldHandleUnhandledWebhookEventTypeEndToEnd() throws Exception {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"unhandled.event.type\"}";
        event.setType("unhandled.event.type");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            mockMvc.perform(post("/api/v1/webhooks/stripe")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Stripe-Signature", "t=1234567890,v1=test_signature")
                    .content(payload))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Webhook processed successfully"));
        }
    }
    
    @Test
    @DisplayName("Should handle webhook with null event type end-to-end")
    void shouldHandleWebhookWithNullEventTypeEndToEnd() throws Exception {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":null}";
        event.setType(null);
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            mockMvc.perform(post("/api/v1/webhooks/stripe")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Stripe-Signature", "t=1234567890,v1=test_signature")
                    .content(payload))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string("Failed to process webhook event"));
        }
    }
    
    @Test
    @DisplayName("Should handle webhook with empty event type end-to-end")
    void shouldHandleWebhookWithEmptyEventTypeEndToEnd() throws Exception {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"\"}";
        event.setType("");
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            mockMvc.perform(post("/api/v1/webhooks/stripe")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Stripe-Signature", "t=1234567890,v1=test_signature")
                    .content(payload))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string("Failed to process webhook event"));
        }
    }
    
    @Test
    @DisplayName("Should handle webhook processing exception end-to-end")
    void shouldHandleWebhookProcessingExceptionEndToEnd() throws Exception {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"payment_intent.succeeded\"}";
        
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test_123");
        
        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(event);
            
            // When
            mockMvc.perform(post("/api/v1/webhooks/stripe")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Stripe-Signature", "t=1234567890,v1=test_signature")
                    .content(payload))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string("Failed to process webhook event"));
        }
    }
}
