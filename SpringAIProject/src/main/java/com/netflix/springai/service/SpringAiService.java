package com.netflix.springai.service;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.annotation.Timed;
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
	@RateLimiter(name = "chat", fallbackMethod = "chatRateLimitFallback")
	@Timed(value = "ai.chat.duration", description = "Time taken for AI chat calls")
	public String chat(String input) {
		Prompt prompt = new PromptTemplate("{{input}}").create(Map.of("input", input));
		var response = chatClient.call(prompt);
		return response.getResult().getOutput().getContent();
	}

	@SuppressWarnings("unused")
	private String chatFallback(String input, Throwable t) {
		return "AI service temporarily unavailable. Please try again later.";
	}

	@SuppressWarnings("unused")
	private String chatRateLimitFallback(String input, Throwable t) {
		return "Rate limit exceeded. Please wait before making another request.";
	}

	@Retry(name = "embed", fallbackMethod = "embedFallback")
	@RateLimiter(name = "embedding", fallbackMethod = "embedRateLimitFallback")
	@Timed(value = "ai.embedding.duration", description = "Time taken for AI embedding calls")
	public List<Double> embed(String text) {
		return embeddingClient.embed(text);
	}

	@SuppressWarnings("unused")
	private List<Double> embedFallback(String text, Throwable t) {
		return java.util.Collections.emptyList();
	}

	@SuppressWarnings("unused")
	private List<Double> embedRateLimitFallback(String text, Throwable t) {
		return java.util.Collections.emptyList();
	}
}
