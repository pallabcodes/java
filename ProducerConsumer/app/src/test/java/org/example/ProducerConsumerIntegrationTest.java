package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.EventRequest;
import org.example.dto.LoginRequest;
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
public class ProducerConsumerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void completeAuthenticationAndProducerFlow() throws Exception {
        // Step 1: Login and get JWT token
        LoginRequest loginRequest = new LoginRequest("producer", "producer123");

        String loginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.role").value("PRODUCER"))
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
                .andExpect(jsonPath("$.username").value("producer"))
                .andExpect(jsonPath("$.role").value("PRODUCER"));

        // Step 3: Use the token to send an event to Kafka
        EventRequest eventRequest = new EventRequest("Integration test event from authenticated producer");

        mockMvc.perform(post("/producer/event")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Event sent to Kafka"))
                .andExpect(jsonPath("$.topic").exists());

        // Step 4: Test health endpoints
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("producer-consumer-service"));

        mockMvc.perform(get("/health/ready"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"));

        mockMvc.perform(get("/health/live"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ALIVE"));
    }

    @Test
    public void producerEndpoint_WithoutAuth_ReturnsUnauthorized() throws Exception {
        EventRequest eventRequest = new EventRequest("Test event without authentication");

        mockMvc.perform(post("/producer/event")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void consumerRole_CannotAccessProducerEndpoint() throws Exception {
        // Login as consumer
        LoginRequest loginRequest = new LoginRequest("consumer", "consumer123");

        String loginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode loginJson = objectMapper.readTree(loginResponse);
        String accessToken = loginJson.get("accessToken").asText();

        // Try to access producer endpoint
        EventRequest eventRequest = new EventRequest("Test event from consumer");

        mockMvc.perform(post("/producer/event")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventRequest)))
                .andExpect(status().isForbidden()); // Should be forbidden due to role check
    }

    @Test
    public void rateLimiting_PreventsExcessiveRequests() throws Exception {
        // Login first
        LoginRequest loginRequest = new LoginRequest("producer", "producer123");

        String loginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode loginJson = objectMapper.readTree(loginResponse);
        String accessToken = loginJson.get("accessToken").asText();

        // Make multiple requests to trigger rate limiting
        EventRequest eventRequest = new EventRequest("Rate limit test event");

        // Make 70 requests (over the 60 per minute limit)
        for (int i = 0; i < 65; i++) {
            mockMvc.perform(post("/producer/event")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(eventRequest)))
                    .andExpect(status().is(200)); // Should succeed initially
        }

        // The next request should be rate limited
        mockMvc.perform(post("/producer/event")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventRequest)))
                .andExpect(status().is(429)); // Too Many Requests
    }
}
