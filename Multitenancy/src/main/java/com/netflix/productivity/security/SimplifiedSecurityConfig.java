package com.netflix.productivity.security;

import com.netflix.productivity.multitenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@Slf4j
public class SimplifiedSecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final TenantSecurityService tenantSecurityService;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(new TenantPermissionEvaluator(tenantSecurityService));
        return handler;
    }
    
    @Bean
    public TenantSecurityService tenantSecurityService() {
        return new TenantSecurityService();
    }
    
    public static class TenantSecurityService {
        
        public boolean belongsToTenant(String tenantId, String userId) {
            try {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth == null || !auth.isAuthenticated()) {
                    return false;
                }
                
                String currentTenantId = TenantContext.getCurrentTenant();
                String currentUserId = auth.getName();
                
                return tenantId.equals(currentTenantId) && userId.equals(currentUserId);
            } catch (Exception e) {
                log.error("Error checking tenant membership", e);
                return false;
            }
        }
        
        public boolean hasAccessToProject(String projectId) {
            try {
                String currentTenantId = TenantContext.getCurrentTenant();
                // This would check if the project belongs to the current tenant
                // and if the user has access to it
                return true; // Simplified for now
            } catch (Exception e) {
                log.error("Error checking project access", e);
                return false;
            }
        }
        
        public boolean hasAccessToIssue(String issueId) {
            try {
                String currentTenantId = TenantContext.getCurrentTenant();
                // This would check if the issue belongs to the current tenant
                // and if the user has access to it
                return true; // Simplified for now
            } catch (Exception e) {
                log.error("Error checking issue access", e);
                return false;
            }
        }
        
        public boolean hasRole(String role) {
            try {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth == null || !auth.isAuthenticated()) {
                    return false;
                }
                
                return auth.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
            } catch (Exception e) {
                log.error("Error checking role", e);
                return false;
            }
        }
    }
}
