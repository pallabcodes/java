package org.example.config;

import org.example.service.JwtTokenService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtTokenService.validateToken(token)) {
                try {
                    String username = jwtTokenService.getSubjectFromToken(token);
                    String tokenType = jwtTokenService.getTokenType(token);

                    // Only allow access tokens for authentication
                    if ("access".equals(tokenType)) {
                        // Extract role from token claims (simplified)
                        String role = "USER"; // Default role

                        // In a real implementation, you'd extract this from the token claims
                        // For now, we'll use a simple role mapping
                        if ("admin".equals(username)) {
                            role = "ADMIN";
                        } else if ("producer".equals(username)) {
                            role = "PRODUCER";
                        } else if ("consumer".equals(username)) {
                            role = "CONSUMER";
                        }

                        UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                            );

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } catch (Exception e) {
                    // Invalid token or parsing error
                    SecurityContextHolder.clearContext();
                }
            }
        }

        chain.doFilter(request, response);
    }
}
