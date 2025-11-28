package com.netflix.productivity.platform.security;

import com.netflix.productivity.platform.multitenancy.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain chain) throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        // JWT Token is in the form "Bearer token". Remove Bearer word and get only the Token
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtTokenService.getSubjectFromToken(jwtToken);
            } catch (Exception e) {
                log.warn("Unable to get JWT Token or JWT Token has expired: {}", e.getMessage());
            }
        } else {
            log.debug("JWT Token does not begin with Bearer String");
        }

        // Once we get the token validate it.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Validate token
            if (jwtTokenService.validateToken(jwtToken)) {

                // Get token type to ensure it's an access token
                String tokenType = jwtTokenService.getTokenType(jwtToken);
                if ("access".equals(tokenType)) {

                    // Extract tenant from token and set in context
                    String tenantId = jwtTokenService.getTenantFromToken(jwtToken);
                    if (tenantId != null) {
                        TenantContext.setTenantId(tenantId);
                    }

                    // Extract claims and roles
                    Map<String, Object> claims = jwtTokenService.getClaimsFromToken(jwtToken);

                    // Get roles from claims (default to USER if not specified)
                    List<String> roles = Collections.singletonList("USER");
                    if (claims.containsKey("roles")) {
                        Object rolesObj = claims.get("roles");
                        if (rolesObj instanceof List) {
                            roles = (List<String>) rolesObj;
                        }
                    }

                    // Create authorities from roles
                    var authorities = roles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .toList();

                    // Create authentication token
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);

                    usernamePasswordAuthenticationToken
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // After setting the Authentication in the context, we specify
                    // that the current user is authenticated. So it passes the Spring Security Configurations successfully.
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

                    log.debug("Set authentication for user: {} in tenant: {} with roles: {}", username, tenantId, roles);
                } else {
                    log.warn("Invalid token type for authentication: {}", tokenType);
                }
            } else {
                log.warn("Invalid JWT token for user: {}", username);
            }
        }

        chain.doFilter(request, response);
    }
}
