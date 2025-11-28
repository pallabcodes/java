package org.example.config;

import org.example.service.JwtTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtTokenService jwtTokenService;

    @Autowired
    private RateLimitingFilter rateLimitingFilter;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:http://localhost:8080/realms/producer-consumer}")
    private String issuerUri;

    @Value("${app.security.cors.allowed-origins:*}")
    private List<String> allowedOrigins;

    @Value("${app.security.headers.content-security-policy:default-src 'self'}")
    private String contentSecurityPolicy;

    public SecurityConfig(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for API endpoints
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**", "/health", "/ready", "/liveness").permitAll()
                .requestMatchers("/auth/**", "/oauth2/**").permitAll() // Allow authentication endpoints
                .requestMatchers("/producer/**", "/consumer/**").authenticated() // Require auth for core functionality
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.decoder(jwtDecoder()))
            )
            .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenService),
                           UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> headers
                .contentTypeOptions(contentTypeOptions -> {})
                .frameOptions(frameOptions -> frameOptions.deny())
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubdomains(true)
                    .preload(true)
                )
                .contentSecurityPolicy(contentSecurityPolicy -> contentSecurityPolicy
                    .policyDirectives(contentSecurityPolicy)
                )
                .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                .permissionsPolicy(permissionsPolicy -> permissionsPolicy
                    .policy("geolocation=(), microphone=(), camera=()")
                )
            );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri);

        // Add custom validators for enhanced security
        OAuth2TokenValidator<Jwt> audienceValidator = new JwtClaimValidator<List<String>>(
            "aud", aud -> aud != null && aud.contains("producer-consumer-client")
        );

        OAuth2TokenValidator<Jwt> issuerValidator = new JwtIssuerValidator(issuerUri);

        OAuth2TokenValidator<Jwt> timestampValidator = new JwtTimestampValidator(
            java.time.Duration.ofSeconds(60), // Clock skew tolerance
            java.time.Duration.ofSeconds(86400) // Max clock skew for exp
        );

        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
            issuerValidator, audienceValidator, timestampValidator
        );

        jwtDecoder.setJwtValidator(validator);
        return jwtDecoder;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(allowedOrigins); // Configurable origins
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With",
            "X-Correlation-Id", "X-Forwarded-For", "User-Agent"
        ));
        configuration.setExposedHeaders(Arrays.asList("X-Correlation-Id", "X-Rate-Limit-Remaining"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
