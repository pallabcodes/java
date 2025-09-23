package com.netflix.springai.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/embeddings")
@Validated
@Tag(name = "Embeddings", description = "Embedding generation endpoint")
public class EmbeddingController {
	private final EmbeddingClient embeddingClient;

	public EmbeddingController(EmbeddingClient embeddingClient) {
		this.embeddingClient = embeddingClient;
	}

	@PostMapping
	@Operation(summary = "Generate embeddings", description = "Returns vector for provided text")
	@ApiResponse(responseCode = "200", description = "OK")
	@ApiResponse(responseCode = "400", description = "Validation error")
	public ResponseEntity<List<Double>> embed(@Valid @RequestBody EmbeddingRequest request) {
		var response = embeddingClient.embed(request.text());
		return ResponseEntity.ok(response);
	}

	public record EmbeddingRequest(@NotBlank String text) {}
}
