package com.netflix.springai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.ai.openai")
public class OpenAIConfig {
	private String apiKey;
	private String baseUrl;
	private String chatModel;
	private String embeddingModel;

	public String getApiKey() { return apiKey; }
	public void setApiKey(String apiKey) { this.apiKey = apiKey; }
	public String getBaseUrl() { return baseUrl; }
	public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
	public String getChatModel() { return chatModel; }
	public void setChatModel(String chatModel) { this.chatModel = chatModel; }
	public String getEmbeddingModel() { return embeddingModel; }
	public void setEmbeddingModel(String embeddingModel) { this.embeddingModel = embeddingModel; }
}
