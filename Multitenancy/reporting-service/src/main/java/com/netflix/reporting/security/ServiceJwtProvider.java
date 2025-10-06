package com.netflix.reporting.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class ServiceJwtProvider {

    private final byte[] secret;
    private final String issuer;
    private final String audience;
    private final long ttlSeconds;

    public ServiceJwtProvider(
            @Value("${security.s2s.jwt.hmac_secret:changemechangemechangemechangeme}") String secret,
            @Value("${security.s2s.jwt.issuer:multitenancy}") String issuer,
            @Value("${security.s2s.jwt.audience:core-service}") String audience,
            @Value("${security.s2s.jwt.ttl-seconds:300}") long ttlSeconds
    ) {
        this.secret = secret.getBytes();
        this.issuer = issuer;
        this.audience = audience;
        this.ttlSeconds = ttlSeconds;
    }

    public String mintToken(String subject) {
        try {
            Instant now = Instant.now();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issuer(issuer)
                    .subject(subject)
                    .audience(audience)
                    .jwtID(UUID.randomUUID().toString())
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusSeconds(ttlSeconds)))
                    .build();
            SignedJWT jwt = new SignedJWT(new com.nimbusds.jose.JWSHeader(JWSAlgorithm.HS256), claims);
            JWSSigner signer = new MACSigner(secret);
            jwt.sign(signer);
            return jwt.serialize();
        } catch (Exception e) {
            throw new IllegalStateException("failed to mint service jwt", e);
        }
    }
}


