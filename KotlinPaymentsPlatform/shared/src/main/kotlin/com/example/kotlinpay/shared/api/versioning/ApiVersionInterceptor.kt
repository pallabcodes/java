package com.example.kotlinpay.shared.api.versioning

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

/**
 * Interceptor for API versioning and deprecation warnings.
 */
@Component
class ApiVersionInterceptor : HandlerInterceptor {

    private val logger = LoggerFactory.getLogger(ApiVersionInterceptor::class.java)
    
    companion object {
        private const val API_VERSION_HEADER = "X-API-Version"
        private const val API_DEPRECATION_HEADER = "Deprecation"
        private const val API_SUNSET_HEADER = "Sunset"
    }

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val requestPath = request.requestURI
        val apiVersion = extractVersionFromPath(requestPath)
        
        if (apiVersion != null) {
            response.setHeader(API_VERSION_HEADER, apiVersion)
            
            if (ApiVersioningConfig.ApiVersions.DEPRECATED != null && 
                apiVersion == ApiVersioningConfig.ApiVersions.DEPRECATED) {
                response.setHeader(API_DEPRECATION_HEADER, "true")
                response.setHeader(API_SUNSET_HEADER, "2025-12-31")
                logger.warn("Deprecated API version accessed: {} from {}", apiVersion, request.remoteAddr)
            }
            
            if (apiVersion != ApiVersioningConfig.ApiVersions.CURRENT) {
                response.setHeader("X-API-Version-Warning", 
                    "You are using an older API version. Consider upgrading to ${ApiVersioningConfig.ApiVersions.CURRENT}")
            }
        }
        
        return true
    }

    private fun extractVersionFromPath(path: String?): String? {
        if (path == null) {
            return null
        }
        
        val pattern = Regex("/api/(v\\d+)/")
        return pattern.find(path)?.groupValues?.get(1)
    }
}

