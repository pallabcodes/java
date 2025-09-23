package com.netflix.productivity.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
public class JwtTenantExtractor {

    private final Key key;

    public JwtTenantExtractor(@Value("${spring.security.jwt.secret:netflix-productivity-jwt-secret-key}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String extractTenant(String bearerToken) {
        try {
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                return null;
            }
            String token = bearerToken.substring(7);
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            Object tenant = claims.get("tenant");
            return tenant != null ? tenant.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }
}


