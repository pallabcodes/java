package com.netflix.productivity.modules.auth;

import com.netflix.productivity.platform.security.JwtTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;

    // Simple in-memory user store for demo purposes
    // In production, this would be a proper user management module
    private static final Map<String, User> USERS = new HashMap<>();
    static {
        USERS.put("admin", new User("admin", passwordEncoder.encode("admin123"), "ADMIN", "default"));
        USERS.put("user1", new User("user1", passwordEncoder.encode("user123"), "USER", "tenant1"));
        USERS.put("user2", new User("user2", passwordEncoder.encode("user123"), "USER", "tenant2"));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request,
                                                   @RequestHeader(value = "X-Tenant-Id", defaultValue = "default") String tenantId) {
        User user = USERS.get(request.username());

        if (user == null || !passwordEncoder.matches(request.password(), user.password())) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid credentials");
            error.put("message", "Username or password is incorrect");
            error.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(401).body(error);
        }

        // Generate tokens
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.username());
        claims.put("roles", user.role());

        String accessToken = jwtTokenService.generateAccessToken(user.username(), tenantId, claims);
        String refreshToken = jwtTokenService.generateRefreshToken(user.username(), tenantId);

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("tokenType", "Bearer");
        response.put("expiresIn", 3600); // 1 hour
        response.put("user", Map.of(
            "username", user.username(),
            "role", user.role(),
            "tenant", tenantId
        ));

        log.info("User {} logged in successfully for tenant {}", user.username(), tenantId);
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
        String tenantId = jwtTokenService.getTenantFromToken(token);
        Map<String, Object> claims = jwtTokenService.getClaimsFromToken(token);

        Map<String, Object> response = new HashMap<>();
        response.put("valid", true);
        response.put("username", username);
        response.put("tenant", tenantId);
        response.put("role", claims.get("roles"));

        return ResponseEntity.ok(response);
    }

    public record LoginRequest(
        @jakarta.validation.constraints.NotBlank(message = "Username is required") String username,
        @jakarta.validation.constraints.NotBlank(message = "Password is required") String password
    ) {}

    public record User(String username, String password, String role, String defaultTenant) {}
}
