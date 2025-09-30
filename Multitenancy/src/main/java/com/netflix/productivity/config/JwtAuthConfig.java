package com.netflix.productivity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class JwtAuthConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:https://idp.example.com/tenant/.well-known/jwks.json}")
    private String jwkSetUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.audiences:productivity-api}")
    private List<String> allowedAudiences;

    @Value("${spring.security.oauth2.resourceserver.jwt.principal-claim:sub}")
    private String principalClaim;

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        decoder.setJwtValidator(new TenantAwareJwtValidator(allowedAudiences));
        return decoder;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("ROLE_");
        authoritiesConverter.setAuthoritiesClaimName("roles");

        return new JwtAuthenticationConverter() {
            @Override
            protected Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
                Collection<GrantedAuthority> base = authoritiesConverter.convert(jwt);
                // Optionally map scopes to roles as needed
                return base;
            }

            @Override
            protected AbstractAuthenticationToken convert(Jwt jwt) {
                AbstractAuthenticationToken token = super.convert(jwt);
                if (token != null) {
                    token.setDetails(jwt.getClaim(principalClaim));
                }
                return token;
            }
        };
    }
}

