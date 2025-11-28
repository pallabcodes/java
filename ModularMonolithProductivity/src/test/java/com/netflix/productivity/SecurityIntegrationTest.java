package com.netflix.productivity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.productivity.modules.auth.AuthController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldAllowLoginAndReturnTokens() throws Exception {
        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest("admin", "admin123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("admin"))
                .andExpect(jsonPath("$.user.role").value("ADMIN"));
    }

    @Test
    void shouldRejectInvalidLoginCredentials() throws Exception {
        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest("admin", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    void shouldRequireAuthenticationForProtectedEndpoints() throws Exception {
        // Try to access protected endpoint without authentication
        mockMvc.perform(get("/api/issues"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAuthenticatedAccessToIssues() throws Exception {
        // Login first
        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest("user1", "user123");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Extract access token
        String accessToken = extractAccessToken(loginResponse);

        // Access protected endpoint with token
        mockMvc.perform(get("/api/issues")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowAuthenticatedAccessToProjects() throws Exception {
        // Login first
        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest("user1", "user123");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Extract access token
        String accessToken = extractAccessToken(loginResponse);

        // Access protected endpoint with token
        mockMvc.perform(get("/api/projects")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void shouldValidateJWTToken() throws Exception {
        // Login first
        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest("user1", "user123");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Extract access token
        String accessToken = extractAccessToken(loginResponse);

        // Validate token
        String validateRequest = "{ \"token\": \"" + accessToken + "\" }";
        mockMvc.perform(post("/api/auth/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void shouldRejectInvalidJWTToken() throws Exception {
        String validateRequest = "{ \"token\": \"invalid.jwt.token\" }";
        mockMvc.perform(post("/api/auth/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false));
    }

    @Test
    void shouldAllowHealthEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("modular-monolith-productivity"));
    }

    private String extractAccessToken(String response) {
        // Simple JSON parsing for test
        int start = response.indexOf("\"accessToken\":\"") + 15;
        int end = response.indexOf("\"", start);
        return response.substring(start, end);
    }
}
