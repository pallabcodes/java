package com.netflix.springai.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/chat")
@Validated
@Tag(name = "Chat", description = "LLM chat endpoint")
public class ChatController {
	private final ChatClient chatClient;

	public ChatController(ChatClient chatClient) {
		this.chatClient = chatClient;
	}

	@PostMapping
	@Operation(summary = "Chat with model", description = "Returns assistant message content for given input")
	@ApiResponse(responseCode = "200", description = "OK")
	@ApiResponse(responseCode = "400", description = "Validation error")
	public ResponseEntity<String> chat(@Valid @RequestBody ChatRequest request) {
		PromptTemplate template = new PromptTemplate("{{input}}");
		Prompt prompt = template.create(Map.of("input", request.input()));
		var response = chatClient.call(prompt);
		return ResponseEntity.ok(response.getResult().getOutput().getContent());
	}

	public record ChatRequest(@NotBlank @Size(max = 4000) String input) {}
}
