package com.amigoscode.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class CustomerSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldAllowRegistrationWithoutAuthentication() throws Exception {
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "John", "Doe", "john.doe@example.com", "password123"
        );

        mockMvc.perform(post("/api/v1/customers/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void shouldAllowLoginAndReturnTokens() throws Exception {
        // First register a customer
        CustomerRegistrationRequest registerRequest = new CustomerRegistrationRequest(
            "Jane", "Smith", "jane.smith@example.com", "password123"
        );

        mockMvc.perform(post("/api/v1/customers/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Now try to login
        LoginRequest loginRequest = new LoginRequest("jane.smith@example.com", "password123");

        mockMvc.perform(post("/api/v1/customers/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("jane.smith@example.com"));
    }

    @Test
    void shouldRejectInvalidLoginCredentials() throws Exception {
        LoginRequest loginRequest = new LoginRequest("nonexistent@example.com", "wrongpassword");

        mockMvc.perform(post("/api/v1/customers/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void shouldRequireAuthenticationForProtectedEndpoints() throws Exception {
        // Try to access protected endpoint without authentication
        mockMvc.perform(get("/api/v1/customers/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectRegistrationWithExistingEmail() throws Exception {
        // Register first customer
        CustomerRegistrationRequest request1 = new CustomerRegistrationRequest(
            "John", "Doe", "john@example.com", "password123"
        );

        mockMvc.perform(post("/api/v1/customers/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // Try to register with same email
        CustomerRegistrationRequest request2 = new CustomerRegistrationRequest(
            "Jane", "Smith", "john@example.com", "password456"
        );

        mockMvc.perform(post("/api/v1/customers/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectRegistrationWithInvalidData() throws Exception {
        // Test with empty first name
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "", "Doe", "john@example.com", "password123"
        );

        mockMvc.perform(post("/api/v1/customers/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldSanitizeInputToPreventXSS() throws Exception {
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "<script>alert('xss')</script>", "Doe", "john@example.com", "password123"
        );

        mockMvc.perform(post("/api/v1/customers/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("&lt;script&gt;alert(&#x27;xss&#x27;)&lt;/script&gt;"));
    }
}
