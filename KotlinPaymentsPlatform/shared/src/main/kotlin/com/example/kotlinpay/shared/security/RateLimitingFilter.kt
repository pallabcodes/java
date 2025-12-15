package com.example.kotlinpay.shared.security

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

/**
 * Rate limiting filter for API protection.
 * 
 * Implements token bucket algorithm to prevent API abuse.
 */
@Component
@Order(1)
class RateLimitingFilter(
    private val rateLimitingConfig: RateLimitingConfig
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(RateLimitingFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // Skip rate limiting for health checks
        if (request.requestURI.startsWith("/actuator/health") ||
            request.requestURI.startsWith("/health") ||
            request.requestURI.startsWith("/api/v1/risk/health") ||
            request.requestURI.startsWith("/api/v1/payments/health") ||
            request.requestURI.startsWith("/api/v1/ledger/health")
        ) {
            filterChain.doFilter(request, response)
            return
        }

        // Get client identifier (IP address or user ID)
        val clientKey = getClientKey(request)
        
        // Get rate limit bucket
        val bucket = rateLimitingConfig.getBucket(clientKey)

        // Try to consume a token
        if (bucket.tryConsume(1)) {
            // Add rate limit headers
            response.setHeader("X-RateLimit-Limit", (bucket.availableTokens + 1).toString())
            response.setHeader("X-RateLimit-Remaining", bucket.availableTokens.toString())
            response.setHeader("X-RateLimit-Reset", (System.currentTimeMillis() / 1000 + 60).toString())

            filterChain.doFilter(request, response)
        } else {
            // Rate limit exceeded
            logger.warn("Rate limit exceeded for client: {}", clientKey)
            
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = "application/json"
            response.setHeader("Retry-After", "60")
            
            response.writer.write(
                """{"error":"Rate limit exceeded","message":"Too many requests. Please retry after 60 seconds.","retryAfter":60}"""
            )
        }
    }

    /**
     * Get client identifier for rate limiting
     */
    private fun getClientKey(request: HttpServletRequest): String {
        // Try to get user ID from header or attribute
        val userId = request.getHeader("X-User-ID")
        if (!userId.isNullOrEmpty()) {
            return "user:$userId"
        }

        // Fall back to IP address
        val ipAddress = getClientIpAddress(request)
        return "ip:$ipAddress"
    }

    /**
     * Get client IP address
     */
    private fun getClientIpAddress(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        if (!xForwardedFor.isNullOrEmpty()) {
            return xForwardedFor.split(",")[0].trim()
        }

        val xRealIp = request.getHeader("X-Real-IP")
        if (!xRealIp.isNullOrEmpty()) {
            return xRealIp
        }

        return request.remoteAddr
    }
}

