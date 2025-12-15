package com.netflix.streaming.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for API documentation.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI streamingPlatformOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Event-Driven Streaming Platform API")
                        .description("""
                            Complete Netflix-grade Event-Driven Architecture API for video streaming analytics.
                            
                            Features:
                            - Event-driven architecture with CQRS and Event Sourcing
                            - Real-time analytics and WebSocket streaming
                            - Machine learning pipeline orchestration
                            - Comprehensive compliance (GDPR, SOX)
                            - Production-grade observability
                            """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Streaming Platform Team")
                                .email("platform-team@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("https://api.streaming-platform.com")
                                .description("Production Server"),
                        new Server()
                                .url("https://staging-api.streaming-platform.com")
                                .description("Staging Server"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server")
                ));
    }
}

