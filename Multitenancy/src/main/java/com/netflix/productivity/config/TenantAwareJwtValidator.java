package com.netflix.productivity.config;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public class TenantAwareJwtValidator implements OAuth2TokenValidator<Jwt> {

    private final List<String> allowedAudiences;

    public TenantAwareJwtValidator(List<String> allowedAudiences) {
        this.allowedAudiences = allowedAudiences;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        List<String> aud = token.getAudience();
        if (aud == null || aud.isEmpty()) {
            return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Missing audience", null));
        }
        boolean ok = aud.stream().anyMatch(allowedAudiences::contains);
        if (!ok) {
            return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Invalid audience", null));
        }
        return OAuth2TokenValidatorResult.success();
    }
}

