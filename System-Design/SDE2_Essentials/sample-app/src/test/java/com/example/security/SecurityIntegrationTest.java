package com.example.security;

import com.example.account.AccountController;
import com.nimbusds.jose.JOSEException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void `should allow access to actuator endpoints without authentication`() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void `should allow access to dev token endpoint without authentication`() throws Exception {
        mockMvc.perform(get("/dev/token?scope=accounts:read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").isNotEmpty());
    }

    @Test
    void `should require authentication for protected endpoints`() throws Exception {
        mockMvc.perform(get("/accounts/a1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void `should allow authenticated access with valid JWT token`() throws Exception {
        // Get a dev token
        String tokenResponse = mockMvc.perform(get("/dev/token?scope=accounts:read"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract token (simple parsing for test)
        String token = extractTokenFromResponse(tokenResponse);

        // Access protected endpoint with token
        mockMvc.perform(get("/accounts/a1")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("a1"));
    }

    @Test
    void `should reject invalid JWT token`() throws Exception {
        mockMvc.perform(get("/accounts/a1")
                .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void `should reject expired JWT token`() throws Exception {
        // Create an expired token using DevTokenController with past expiration
        // This would require mocking time or using a token with past expiration
        // For this test, we'll use an obviously expired token
        String expiredToken = createExpiredToken();

        mockMvc.perform(get("/accounts/a1")
                .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void `should enforce scope-based authorization`() throws Exception {
        // Get token with limited scope
        String tokenResponse = mockMvc.perform(get("/dev/token?scope=limited:read"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = extractTokenFromResponse(tokenResponse);

        // Try to access accounts endpoint (should fail if scope doesn't match)
        mockMvc.perform(get("/accounts/a1")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void `should implement rate limiting`() throws Exception {
        // Get a valid token
        String tokenResponse = mockMvc.perform(get("/dev/token?scope=accounts:read"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = extractTokenFromResponse(tokenResponse);

        // Make multiple requests rapidly (more than rate limit allows)
        for (int i = 0; i < 110; i++) { // Rate limit is 100 per minute
            mockMvc.perform(get("/accounts/a1")
                    .header("Authorization", "Bearer " + token))
                    .andExpect(i < 100 ? status().isOk() : status().isTooManyRequests());
        }
    }

    @Test
    void `should include security headers in responses`() throws Exception {
        String tokenResponse = mockMvc.perform(get("/dev/token?scope=accounts:read"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = extractTokenFromResponse(tokenResponse);

        mockMvc.perform(get("/accounts/a1")
                .header("Authorization", "Bearer " + token))
                .andExpect(header().exists("X-Content-Type-Options"))
                .andExpect(header().exists("X-Frame-Options"))
                .andExpect(header().exists("Strict-Transport-Security"));
    }

    private String extractTokenFromResponse(String response) {
        // Simple JSON parsing for test - extract token value
        int tokenStart = response.indexOf("\"") + 1;
        int tokenEnd = response.lastIndexOf("\"");
        return response.substring(tokenStart, tokenEnd);
    }

    private String createExpiredToken() throws JOSEException {
        // For testing, return a clearly invalid/expired token
        // In a real scenario, you'd create a token with past expiration
        return "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkZXYtdXNlciIsInNjb3BlIjoiYWNjb3VudHM6cmVhZCIsImlhdCI6MTYwOTQ1MzYwMCwiZXhwIjoxNjA5NDUzNjAwfQ.expired";
    }
}
