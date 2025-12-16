package com.netflix.streaming.infrastructure.security;

import com.netflix.streaming.infrastructure.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
class SecurityIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    private MockMvc mockMvc;

    @Autowired
    void setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    @WithAnonymousUser
    void shouldRejectUnauthenticatedRequests() throws Exception {
        mockMvc.perform(get("/api/v1/events"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldAllowAuthenticatedRequests() throws Exception {
        mockMvc.perform(get("/api/v1/health")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldRejectRequestsWithoutProperRole() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/events"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowRequestsWithProperRole() throws Exception {
        mockMvc.perform(get("/api/v1/admin/health")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void shouldHaveSecurityHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
            .andExpect(header().exists("X-Frame-Options"))
            .andExpect(header().exists("X-Content-Type-Options"))
            .andExpect(header().exists("X-XSS-Protection"))
            .andExpect(header().exists("Strict-Transport-Security"))
            .andExpect(header().string("X-Frame-Options", "DENY"))
            .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void shouldPreventSQLInjection() throws Exception {
        String maliciousInput = "'; DROP TABLE users; --";

        mockMvc.perform(post("/api/v1/events/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"query\": \"" + maliciousInput + "\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldPreventXSS() throws Exception {
        String xssPayload = "<script>alert('xss')</script>";

        mockMvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"data\": \"" + xssPayload + "\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldValidateInput() throws Exception {
        // Test empty required fields
        mockMvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void shouldEnforceRateLimiting() throws Exception {
        // Make multiple rapid requests
        for (int i = 0; i < 150; i++) {
            mockMvc.perform(get("/api/v1/health"));
        }

        // The last request should be rate limited
        mockMvc.perform(get("/api/v1/health"))
            .andExpect(status().isTooManyRequests())
            .andExpect(header().exists("Retry-After"));
    }

    @Test
    void shouldPreventDirectoryTraversal() throws Exception {
        String pathTraversal = "../../../etc/passwd";

        mockMvc.perform(get("/api/v1/files/" + pathTraversal))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleLargePayloadsGracefully() throws Exception {
        StringBuilder largePayload = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            largePayload.append("x");
        }

        mockMvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"data\": \"" + largePayload.toString() + "\"}"))
            .andExpect(status().isPayloadTooLarge());
    }

    @Test
    void shouldValidateContentType() throws Exception {
        mockMvc.perform(post("/api/v1/events")
                .contentType(MediaType.TEXT_PLAIN)
                .content("invalid json"))
            .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void shouldPreventHTTPVerbTampering() throws Exception {
        // Try to POST to a GET-only endpoint
        mockMvc.perform(post("/api/v1/health"))
            .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void shouldValidateJWTTokenFormat() throws Exception {
        mockMvc.perform(get("/api/v1/events")
                .header("Authorization", "Bearer invalid-jwt-token"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldHandleMalformedJSON() throws Exception {
        mockMvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldPreventMassAssignment() throws Exception {
        // Try to set internal fields
        mockMvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\": \"123\", \"internalField\": \"hacked\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldEnforceFieldSizeLimits() throws Exception {
        StringBuilder oversizedField = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            oversizedField.append("x");
        }

        mockMvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"" + oversizedField.toString() + "\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldValidateEnumValues() throws Exception {
        mockMvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"INVALID_STATUS\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldPreventCommandInjection() throws Exception {
        String commandInjection = "$(rm -rf /)";

        mockMvc.perform(post("/api/v1/events/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"query\": \"" + commandInjection + "\"}"))
            .andExpect(status().isBadRequest());
    }
}
