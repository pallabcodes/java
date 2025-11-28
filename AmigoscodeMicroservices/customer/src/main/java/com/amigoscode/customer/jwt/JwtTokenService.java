package com.amigoscode.customer.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class JwtTokenService {

    private final Key key;
    private final long accessTokenExpiryMs;
    private final long refreshTokenExpiryMs;

    public JwtTokenService(
            @Value("${app.security.jwt.secret:customer-service-jwt-secret-key-change-in-production}") String secret,
            @Value("${app.security.jwt.expiration:86400000}") long accessTokenExpiryMs,
            @Value("${app.security.jwt.refresh-expiration:604800000}") long refreshTokenExpiryMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiryMs = accessTokenExpiryMs;
        this.refreshTokenExpiryMs = refreshTokenExpiryMs;
        log.info("JWT Token Service initialized with access token expiry: {}ms, refresh token expiry: {}ms",
                accessTokenExpiryMs, refreshTokenExpiryMs);
    }

    public String generateAccessToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        Map<String, Object> tokenClaims = new HashMap<>();
        if (claims != null) {
            tokenClaims.putAll(claims);
        }
        tokenClaims.put("type", "access");
        tokenClaims.put("issued_at", now.toEpochMilli());

        return Jwts.builder()
                .setSubject(subject)
                .addClaims(tokenClaims)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(accessTokenExpiryMs)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String subject) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(subject)
                .claim("type", "refresh")
                .claim("issued_at", now.toEpochMilli())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(refreshTokenExpiryMs)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getSubjectFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            log.warn("Failed to extract subject from token: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String getTokenType(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("type", String.class);
        } catch (Exception e) {
            log.warn("Failed to extract token type: {}", e.getMessage());
            return null;
        }
    }

    public Map<String, Object> getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.warn("Failed to extract claims from token: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}
