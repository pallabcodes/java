package com.netflix.productivity.auth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtTokenService {

    private final Key key;
    private final long expiryMs;

    public JwtTokenService(
            @Value("${spring.security.jwt.secret:netflix-productivity-jwt-secret-key}") String secret,
            @Value("${spring.security.jwt.expiration:3600000}") long expiryMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiryMs = expiryMs;
    }

    public String issue(String subject, String tenant, int tokenVersion, Map<String, Object> claims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(subject)
                .addClaims(claims)
                .claim("tenant", tenant)
                .claim("ver", tokenVersion)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(expiryMs)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}


