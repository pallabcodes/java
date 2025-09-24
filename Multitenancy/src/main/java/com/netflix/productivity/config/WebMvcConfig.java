package com.netflix.productivity.config;

import com.netflix.productivity.web.HttpMetricsInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final HttpMetricsInterceptor httpMetricsInterceptor;

    public WebMvcConfig(HttpMetricsInterceptor httpMetricsInterceptor) {
        this.httpMetricsInterceptor = httpMetricsInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(httpMetricsInterceptor);
    }
}


