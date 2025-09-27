package com.netflix.springai.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.micrometer.core.annotation.Timed;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.netflix.springai.service.SpringAiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/chat")
@Validated
@Tag(name = "Chat", description = "LLM chat endpoint")
public class ChatController {
    private final SpringAiService springAiService;

    public ChatController(SpringAiService springAiService) {
        this.springAiService = springAiService;
    }

    @PostMapping
    @Timed(value = "chat.request", description = "Latency for chat endpoint")
    @Operation(summary = "Chat with model", description = "Returns assistant message content for given input")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Validation error")
    public ResponseEntity<String> chat(@Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(springAiService.chat(request.input()));
    }

    public record ChatRequest(@NotBlank @Size(max = 4000) String input) {}
}
