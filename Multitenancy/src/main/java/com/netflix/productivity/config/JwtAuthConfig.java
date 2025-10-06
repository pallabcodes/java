package com.netflix.productivity.config;

import com.netflix.productivity.security.RevokedTokenStore;
import com.nimbusds.jose.jwk.source.DefaultJWKSetCache;
import com.nimbusds.jose.jwk.source.JWKSetCache;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.ResourceRetriever;
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
import java.net.MalformedURLException;
import java.net.URL;

@Configuration
public class JwtAuthConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:https://idp.example.com/tenant/.well-known/jwks.json}")
    private String jwkSetUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.audiences:productivity-api}")
    private List<String> allowedAudiences;

    @Value("${spring.security.oauth2.resourceserver.jwt.principal-claim:sub}")
    private String principalClaim;

    @Value("${security.jwks.connect-timeout-ms:2000}")
    private int jwksConnectTimeoutMs;

    @Value("${security.jwks.read-timeout-ms:2000}")
    private int jwksReadTimeoutMs;

    @Value("${security.jwks.cache-lifespan-seconds:300}")
    private long jwksCacheLifespanSeconds;

    @Value("${security.jwks.cache-refresh-seconds:300}")
    private long jwksCacheRefreshSeconds;

    @Bean
    public JwtDecoder jwtDecoder(OAuth2TokenValidator<Jwt> tenantAwareJwtValidator) throws MalformedURLException {
        ResourceRetriever retriever = new DefaultResourceRetriever(jwksConnectTimeoutMs, jwksReadTimeoutMs);
        JWKSetCache cache = new DefaultJWKSetCache(jwksCacheLifespanSeconds, jwksCacheRefreshSeconds);
        JWKSource<SecurityContext> jwkSource = new RemoteJWKSet<>(new URL(jwkSetUri), retriever, cache);
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSource(jwkSource).build();
        decoder.setJwtValidator(tenantAwareJwtValidator);
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

    @Bean
    public OAuth2TokenValidator<Jwt> tenantAwareJwtValidator(RevokedTokenStore revokedTokenStore) {
        return new TenantAwareJwtValidator(allowedAudiences, revokedTokenStore);
    }
}

