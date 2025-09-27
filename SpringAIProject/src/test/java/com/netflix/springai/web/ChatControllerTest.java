package com.netflix.springai.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.netflix.springai.service.SpringAiService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ChatController.class)
class ChatControllerTest {
    @Autowired MockMvc mvc;
    @MockBean SpringAiService springAiService;

    @Test
    void chatEndpointWorks() throws Exception {
        when(springAiService.chat("hi")).thenReturn("ok");

        mvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON).content("{\"input\":\"hi\"}"))
            .andExpect(status().isOk())
            .andExpect(content().string("ok"));
    }
}
