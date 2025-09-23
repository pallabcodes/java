package com.example.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Date;

@RestController
public class DevTokenController {
    @Value("${spring.security.oauth2.resourceserver.jwt.secret-key}")
    private String secret;

    @GetMapping("/dev/token")
    public String token(@RequestParam(defaultValue = "accounts:read") String scope) throws Exception {
        JWSSigner signer = new MACSigner(secret.getBytes());
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer("dev-issuer")
                .subject("dev-user")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(600)))
                .claim("scope", scope)
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(signer);
        return jwt.serialize();
    }
}


