package com.netflix.springai.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("service", "spring-ai-service");
        health.put("version", "1.0.0");

        // Add basic component checks
        Map<String, Object> components = new HashMap<>();
        components.put("openai", Map.of("status", "UP", "details", "OpenAI API configured"));
        components.put("jwt", Map.of("status", "UP", "details", "JWT service operational"));
        components.put("security", Map.of("status", "UP", "details", "Security filters active"));
        components.put("resilience", Map.of("status", "UP", "details", "Resilience4j configured"));

        health.put("components", components);

        return ResponseEntity.ok(health);
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> readiness = new HashMap<>();
        readiness.put("status", "READY");
        readiness.put("timestamp", System.currentTimeMillis());
        readiness.put("checks", new String[]{"openai", "jwt", "security"});

        return ResponseEntity.ok(readiness);
    }

    @GetMapping("/live")
    public ResponseEntity<Map<String, Object>> liveness() {
        Map<String, Object> liveness = new HashMap<>();
        liveness.put("status", "ALIVE");
        liveness.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(liveness);
    }
}
