package com.ecommerce.gateway;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Value("${jwt.secret:netflix-ecommerce-jwt-secret-key}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiry:3600000}")
    private long accessTokenExpiryMs;

    @Value("${jwt.refresh-token-expiry:86400000}")
    private long refreshTokenExpiryMs;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Simple in-memory user store for demo purposes
    // In production, this would be a database with proper user management
    private static final Map<String, User> USERS = new HashMap<>();
    static {
        USERS.put("admin", new User("admin", passwordEncoder.encode("admin123"), "ADMIN"));
        USERS.put("user1", new User("user1", passwordEncoder.encode("user123"), "USER"));
        USERS.put("user2", new User("user2", passwordEncoder.encode("user123"), "USER"));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, Object>>> login(@RequestBody LoginRequest request) {
        User user = USERS.get(request.username());

        if (user == null || !passwordEncoder.matches(request.password(), user.password())) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid credentials");
            error.put("message", "Username or password is incorrect");
            error.put("timestamp", System.currentTimeMillis());
            return Mono.just(ResponseEntity.status(401).body(error));
        }

        // Generate tokens
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.username());
        claims.put("roles", user.role());

        String accessToken = generateAccessToken(user.username(), claims);
        String refreshToken = generateRefreshToken(user.username());

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("tokenType", "Bearer");
        response.put("expiresIn", accessTokenExpiryMs / 1000);
        response.put("user", Map.of(
            "username", user.username(),
            "role", user.role()
        ));

        log.info("User {} logged in successfully", user.username());
        return Mono.just(ResponseEntity.ok(response));
    }

    @PostMapping("/validate")
    public Mono<ResponseEntity<Map<String, Object>>> validate(@RequestBody Map<String, String> request) {
        String token = request.get("token");

        if (token == null || !validateToken(token)) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            return Mono.just(ResponseEntity.ok(response));
        }

        String username = getSubjectFromToken(token);
        Map<String, Object> claims = getClaimsFromToken(token);

        Map<String, Object> response = new HashMap<>();
        response.put("valid", true);
        response.put("username", username);
        response.put("role", claims.get("roles"));

        return Mono.just(ResponseEntity.ok(response));
    }

    private String generateAccessToken(String subject, Map<String, Object> claims) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();

        return Jwts.builder()
                .setSubject(subject)
                .addClaims(claims)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(accessTokenExpiryMs)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private String generateRefreshToken(String subject) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();

        return Jwts.builder()
                .setSubject(subject)
                .claim("type", "refresh")
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(refreshTokenExpiryMs)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private boolean validateToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String getSubjectFromToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Object> getClaimsFromToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    public record LoginRequest(String username, String password) {}
    public record User(String username, String password, String role) {}
}
