package com.example.kotlinpay.shared.api.versioning

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * API versioning configuration.
 */
@Configuration
class ApiVersioningConfig : WebMvcConfigurer {

    object ApiVersions {
        const val V1 = "v1"
        const val V2 = "v2"
        const val CURRENT = V1
        const val DEPRECATED: String? = null
    }

    @Bean
    fun apiVersionInterceptor(): ApiVersionInterceptor {
        return ApiVersionInterceptor()
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(apiVersionInterceptor())
    }
}

