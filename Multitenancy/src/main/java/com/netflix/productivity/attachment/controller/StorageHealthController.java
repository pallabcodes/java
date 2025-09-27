package com.netflix.productivity.attachment.controller;

import com.netflix.productivity.api.ApiResponse;
import com.netflix.productivity.api.ResponseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/storage/health")
@Tag(name = "Attachments")
public class StorageHealthController {

    private final ResponseMapper responses;

    public StorageHealthController(ResponseMapper responses) {
        this.responses = responses;
    }

    @Value("${attachments.storage.provider:local}")
    private String provider;

    @Value("${attachments.storage.s3.endpoint:http://localhost:9000}")
    private String endpoint;

    @Operation(summary = "Storage health and provider")
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        return responses.ok(Map.of(
                "provider", provider,
                "endpoint", endpoint
        ));
    }
}

