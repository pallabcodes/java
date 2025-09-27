package com.netflix.springai.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.micrometer.core.annotation.Timed;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.netflix.springai.service.SpringAiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/embeddings")
@Validated
@Tag(name = "Embeddings", description = "Embedding generation endpoint")
public class EmbeddingController {
    private final SpringAiService springAiService;

    public EmbeddingController(SpringAiService springAiService) {
        this.springAiService = springAiService;
    }

    @PostMapping
    @Timed(value = "embeddings.request", description = "Latency for embeddings endpoint")
    @Operation(summary = "Generate embeddings", description = "Returns vector for provided text")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Validation error")
    public ResponseEntity<List<Double>> embed(@Valid @RequestBody EmbeddingRequest request) {
        return ResponseEntity.ok(springAiService.embed(request.text()));
    }

    public record EmbeddingRequest(@NotBlank @Size(max = 8000) String text) {}
}
