package com.netflix.springai.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

public class SpringAiServiceTest {
	@Test
	void chatReturnsContent() {
		ChatClient chatClient = Mockito.mock(ChatClient.class);
		EmbeddingClient embeddingClient = Mockito.mock(EmbeddingClient.class);
		ChatResponse response = Mockito.mock(ChatResponse.class);
		Generation gen = Mockito.mock(Generation.class);
		Mockito.when(gen.getOutput()).thenReturn(new org.springframework.ai.chat.messages.AssistantMessage("ok"));
		Mockito.when(response.getResult()).thenReturn(gen);
		Mockito.when(chatClient.call(any(Prompt.class))).thenReturn(response);

		SpringAiService service = new SpringAiService(chatClient, embeddingClient);
		assertThat(service.chat("hi")).isEqualTo("ok");
	}

	@Test
	void embedReturnsVector() {
		ChatClient chatClient = Mockito.mock(ChatClient.class);
		EmbeddingClient embeddingClient = Mockito.mock(EmbeddingClient.class);
		Mockito.when(embeddingClient.embed("x")).thenReturn(List.of(0.1, 0.2));
		SpringAiService service = new SpringAiService(chatClient, embeddingClient);
		assertThat(service.embed("x")).containsExactly(0.1, 0.2);
	}
}
