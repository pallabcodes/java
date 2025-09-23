package com.netflix.productivity.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI platformOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Netflix Productivity Platform API")
                        .description("Multi-tenant productivity API with strict tenant isolation")
                        .version("v1"))
                .externalDocs(new ExternalDocumentation()
                        .description("API Standards")
                        .url("https://internal-docs/api-standards"));
    }
}


