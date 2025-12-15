package com.example.kotlinpay.shared.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * OpenAPI/Swagger configuration for API documentation.
 */
@Configuration
class OpenApiConfig {

    @Bean
    fun paymentsPlatformOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Kotlin Payments Platform API")
                    .description("""
                        Production-grade payment processing platform with comprehensive risk assessment,
                        secure payment processing, and enterprise monitoring.
                        
                        Features:
                        - Payment processing with multiple gateway support
                        - Real-time fraud detection and risk assessment
                        - Financial ledger and accounting
                        - PCI DSS Level 1 compliance
                        - Comprehensive audit logging
                    """.trimIndent())
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("Payments Platform Team")
                            .email("payments-team@example.com")
                    )
                    .license(
                        License()
                            .name("Proprietary")
                            .url("https://example.com/license")
                    )
            )
            .servers(
                listOf(
                    Server()
                        .url("https://api.payments-platform.com")
                        .description("Production Server"),
                    Server()
                        .url("https://staging-api.payments-platform.com")
                        .description("Staging Server"),
                    Server()
                        .url("http://localhost:8080")
                        .description("Local Development Server")
                )
            )
    }
}

