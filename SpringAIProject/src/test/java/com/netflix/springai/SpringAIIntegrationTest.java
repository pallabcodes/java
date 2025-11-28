package com.netflix.springai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class SpringAIIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void completeAuthenticationAndChatFlow() throws Exception {
        // Step 1: Login and get JWT token
        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest("ai-user", "aiuser123");

        String loginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.role").value("AI_USER"))
                .andReturn().getResponse().getContentAsString();

        // Extract access token
        JsonNode loginJson = objectMapper.readTree(loginResponse);
        String accessToken = loginJson.get("accessToken").asText();

        // Step 2: Validate the token
        String validateRequest = "{ \"token\": \"" + accessToken + "\" }";
        mockMvc.perform(post("/auth/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.username").value("ai-user"))
                .andExpect(jsonPath("$.role").value("AI_USER"));

        // Step 3: Use the token to access chat endpoint
        ChatController.ChatRequest chatRequest = new ChatController.ChatRequest("Hello AI!");

        mockMvc.perform(post("/api/chat")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(chatRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.notNullValue()));

        // Step 4: Use the token to access embedding endpoint
        EmbeddingController.EmbeddingRequest embedRequest = new EmbeddingController.EmbeddingRequest("Hello world");

        mockMvc.perform(post("/api/embeddings")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(embedRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // Step 5: Check health endpoints
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("spring-ai-service"));

        mockMvc.perform(get("/health/ready"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"));

        mockMvc.perform(get("/health/live"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ALIVE"));
    }

    @Test
    public void chatEndpoint_WithoutAuth_ReturnsUnauthorized() throws Exception {
        ChatController.ChatRequest chatRequest = new ChatController.ChatRequest("Hello AI!");

        mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(chatRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void guestUser_CannotAccessAIEndpoints() throws Exception {
        // Login as guest user
        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest("guest", "guest123");

        String loginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode loginJson = objectMapper.readTree(loginResponse);
        String accessToken = loginJson.get("accessToken").asText();

        // Try to access chat endpoint
        ChatController.ChatRequest chatRequest = new ChatController.ChatRequest("Hello AI!");

        mockMvc.perform(post("/api/chat")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(chatRequest)))
                .andExpect(status().isForbidden()); // Should be forbidden due to role check
    }
}
