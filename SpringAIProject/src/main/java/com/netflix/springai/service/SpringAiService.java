package com.netflix.springai.service;

import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SpringAiService {
	private final ChatClient chatClient;
	private final EmbeddingClient embeddingClient;

	public SpringAiService(ChatClient chatClient, EmbeddingClient embeddingClient) {
		this.chatClient = chatClient;
		this.embeddingClient = embeddingClient;
	}

	@Retry(name = "chat", fallbackMethod = "chatFallback")
	public String chat(String input) {
		Prompt prompt = new PromptTemplate("{{input}}").create(Map.of("input", input));
		var response = chatClient.call(prompt);
		return response.getResult().getOutput().getContent();
	}

	@SuppressWarnings("unused")
	private String chatFallback(String input, Throwable t) {
		return "temporarily unavailable";
	}

	@Retry(name = "embed", fallbackMethod = "embedFallback")
	public List<Double> embed(String text) {
		return embeddingClient.embed(text);
	}

	@SuppressWarnings("unused")
	private List<Double> embedFallback(String text, Throwable t) {
		return java.util.Collections.emptyList();
	}
}
