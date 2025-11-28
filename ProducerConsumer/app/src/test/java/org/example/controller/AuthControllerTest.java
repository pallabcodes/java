package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.LoginRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void login_WithValidCredentials_ReturnsTokens() throws Exception {
        LoginRequest request = new LoginRequest("admin", "admin123");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("admin"))
                .andExpect(jsonPath("$.user.role").value("ADMIN"));
    }

    @Test
    public void login_WithInvalidCredentials_ReturnsUnauthorized() throws Exception {
        LoginRequest request = new LoginRequest("admin", "wrongpassword");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    public void login_WithBlankUsername_ReturnsBadRequest() throws Exception {
        LoginRequest request = new LoginRequest("", "password");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void login_WithBlankPassword_ReturnsBadRequest() throws Exception {
        LoginRequest request = new LoginRequest("admin", "");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void refresh_WithValidRefreshToken_ReturnsNewAccessToken() throws Exception {
        // First login to get a refresh token
        LoginRequest loginRequest = new LoginRequest("admin", "admin123");

        String loginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Extract refresh token from login response
        String refreshToken = extractRefreshToken(loginResponse);

        // Now test refresh
        String refreshRequest = "{ \"refreshToken\": \"" + refreshToken + "\" }";

        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    public void validate_WithValidToken_ReturnsValid() throws Exception {
        // First login to get a token
        LoginRequest loginRequest = new LoginRequest("admin", "admin123");

        String loginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Extract access token from login response
        String accessToken = extractAccessToken(loginResponse);

        // Now test validation
        String validateRequest = "{ \"token\": \"" + accessToken + "\" }";

        mockMvc.perform(post("/auth/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    public void validate_WithInvalidToken_ReturnsInvalid() throws Exception {
        String validateRequest = "{ \"token\": \"invalid.jwt.token\" }";

        mockMvc.perform(post("/auth/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false));
    }

    private String extractAccessToken(String response) {
        // Simple JSON parsing for test - in real code you'd use a proper JSON parser
        int start = response.indexOf("\"accessToken\":\"") + 15;
        int end = response.indexOf("\"", start);
        return response.substring(start, end);
    }

    private String extractRefreshToken(String response) {
        // Simple JSON parsing for test - in real code you'd use a proper JSON parser
        int start = response.indexOf("\"refreshToken\":\"") + 16;
        int end = response.indexOf("\"", start);
        return response.substring(start, end);
    }
}
