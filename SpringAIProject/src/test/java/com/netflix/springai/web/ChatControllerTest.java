package com.netflix.springai.web;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ChatController.class)
class ChatControllerTest {
	@Autowired MockMvc mvc;
	@MockBean ChatClient chatClient;

	@Test
	void chatEndpointWorks() throws Exception {
		ChatResponse response = org.mockito.Mockito.mock(ChatResponse.class);
		Generation gen = org.mockito.Mockito.mock(Generation.class);
		when(gen.getOutput()).thenReturn(new org.springframework.ai.chat.messages.AssistantMessage("ok"));
		when(response.getResult()).thenReturn(gen);
		when(chatClient.call(any())).thenReturn(response);

		mvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON).content("{\"input\":\"hi\"}"))
			.andExpect(status().isOk())
			.andExpect(content().string("ok"));
	}
}
