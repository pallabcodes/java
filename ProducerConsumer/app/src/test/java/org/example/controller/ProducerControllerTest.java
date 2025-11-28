package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.EventRequest;
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
@WebMvcTest(ProducerController.class)
public class ProducerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void sendEventToKafka_WithoutAuth_ReturnsUnauthorized() throws Exception {
        EventRequest request = new EventRequest("test event data");

        mockMvc.perform(post("/producer/event")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void sendEventToKafka_ValidRequest_ReturnsSuccess() throws Exception {
        EventRequest request = new EventRequest("test event data");

        // Note: In a full integration test, you'd obtain a valid JWT token first
        // For this unit test, we're testing the endpoint structure
        // Authentication is tested separately in AuthControllerTest
        mockMvc.perform(post("/producer/event")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer valid-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Event sent to Kafka"))
                .andExpect(jsonPath("$.topic").exists());
    }

    @Test
    public void sendEventToKafka_EmptyEventData_ReturnsBadRequest() throws Exception {
        EventRequest request = new EventRequest("");

        mockMvc.perform(post("/producer/event")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer valid-jwt-token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void sendEventToKafka_NullEventData_ReturnsBadRequest() throws Exception {
        EventRequest request = new EventRequest();
        request.setEventData(null);

        mockMvc.perform(post("/producer/event")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer valid-jwt-token"))
                .andExpect(status().isBadRequest());
    }
}
