package org.example.controller;

import org.example.dto.LoginRequest;
import org.example.dto.LoginResponse;
import org.example.service.JwtTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtTokenService jwtTokenService;

    // Simple in-memory user store for demo purposes
    // In production, this would be a database
    private static final Map<String, String> USERS = new HashMap<>();
    static {
        USERS.put("admin", "admin123"); // Default admin user
        USERS.put("producer", "producer123"); // Producer user
        USERS.put("consumer", "consumer123"); // Consumer user
    }

    public AuthController(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        // Simple authentication - check username/password
        String storedPassword = USERS.get(request.getUsername());
        if (storedPassword == null || !storedPassword.equals(request.getPassword())) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid credentials");
            errorResponse.put("message", "Username or password is incorrect");
            errorResponse.put("timestamp", java.time.LocalDateTime.now());
            return ResponseEntity.status(401).body(errorResponse);
        }

        // Generate tokens
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", request.getUsername());
        claims.put("role", getUserRole(request.getUsername()));

        String accessToken = jwtTokenService.generateAccessToken(request.getUsername(), claims);
        String refreshToken = jwtTokenService.generateRefreshToken(request.getUsername());

        // Create response
        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("tokenType", "Bearer");
        response.put("expiresIn", 3600); // 1 hour
        response.put("user", Map.of(
            "username", request.getUsername(),
            "role", getUserRole(request.getUsername())
        ));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || !jwtTokenService.validateToken(refreshToken)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid refresh token");
            return ResponseEntity.status(401).body(errorResponse);
        }

        String tokenType = jwtTokenService.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid token type");
            return ResponseEntity.status(401).body(errorResponse);
        }

        String username = jwtTokenService.getSubjectFromToken(refreshToken);

        // Generate new access token
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("role", getUserRole(username));

        String newAccessToken = jwtTokenService.generateAccessToken(username, claims);

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", newAccessToken);
        response.put("tokenType", "Bearer");
        response.put("expiresIn", 3600);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        if (token == null || !jwtTokenService.validateToken(token)) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            return ResponseEntity.ok(response);
        }

        String username = jwtTokenService.getSubjectFromToken(token);
        String tokenType = jwtTokenService.getTokenType(token);

        Map<String, Object> response = new HashMap<>();
        response.put("valid", true);
        response.put("username", username);
        response.put("tokenType", tokenType);
        response.put("role", getUserRole(username));

        return ResponseEntity.ok(response);
    }

    private String getUserRole(String username) {
        return switch (username) {
            case "admin" -> "ADMIN";
            case "producer" -> "PRODUCER";
            case "consumer" -> "CONSUMER";
            default -> "USER";
        };
    }
}
