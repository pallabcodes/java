package com.netflix.productivity.security;

import com.netflix.productivity.multitenancy.TenantContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final Key key;

    public JwtAuthenticationFilter(@Value("${spring.security.jwt.secret:netflix-productivity-jwt-secret-key}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            try {
                Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(auth.substring(7)).getBody();
                String userId = claims.getSubject();
                String tenant = (String) claims.get("tenant");
                List<?> roles = (List<?>) claims.get("roles");
                if (tenant != null) {
                    TenantContext.setCurrentTenant(tenant);
                }
                AuthUser principal = new AuthUser(userId, tenant, roles == null ? List.of() : roles.stream().map(Object::toString).toList());
                var authToken = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (Exception ignored) { }
        }
        filterChain.doFilter(request, response);
    }
}


