package com.netflix.reporting.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

@Component
public class FeignAuthInterceptor implements RequestInterceptor {

    private final ServiceJwtProvider serviceJwtProvider;

    public FeignAuthInterceptor(ServiceJwtProvider serviceJwtProvider) {
        this.serviceJwtProvider = serviceJwtProvider;
    }

    @Override
    public void apply(RequestTemplate template) {
        String token = serviceJwtProvider.mintToken("reporting-service");
        template.header("Authorization", "Bearer " + token);
    }
}


