package org.example.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtTokenService {

    private final Key key;
    private final long accessTokenExpiryMs;
    private final long refreshTokenExpiryMs;

    public JwtTokenService(
            @Value("${jwt.secret:netflix-producer-consumer-secret-key}") String secret,
            @Value("${jwt.access-token-expiry:3600000}") long accessTokenExpiryMs,
            @Value("${jwt.refresh-token-expiry:86400000}") long refreshTokenExpiryMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiryMs = accessTokenExpiryMs;
        this.refreshTokenExpiryMs = refreshTokenExpiryMs;
    }

    public String generateAccessToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        Map<String, Object> tokenClaims = new HashMap<>();
        if (claims != null) {
            tokenClaims.putAll(claims);
        }
        tokenClaims.put("type", "access");

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
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(refreshTokenExpiryMs)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getSubjectFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
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
            return null;
        }
    }
}
