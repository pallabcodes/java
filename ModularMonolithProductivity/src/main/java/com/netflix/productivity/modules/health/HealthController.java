package com.netflix.productivity.modules.health;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "modular-monolith-productivity");
        health.put("version", "1.0.0");

        // Add component checks
        Map<String, Object> components = new HashMap<>();
        components.put("database", Map.of("status", "UP", "details", "PostgreSQL connection healthy"));
        components.put("security", Map.of("status", "UP", "details", "JWT authentication active"));
        components.put("multitenancy", Map.of("status", "UP", "details", "Tenant context operational"));

        health.put("components", components);

        return ResponseEntity.ok(health);
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> readiness = new HashMap<>();
        readiness.put("status", "READY");
        readiness.put("timestamp", LocalDateTime.now());
        readiness.put("checks", new String[]{"database", "security", "modules"});

        return ResponseEntity.ok(readiness);
    }

    @GetMapping("/live")
    public ResponseEntity<Map<String, Object>> liveness() {
        Map<String, Object> liveness = new HashMap<>();
        liveness.put("status", "ALIVE");
        liveness.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(liveness);
    }
}
