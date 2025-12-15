package com.example.kotlinpay.shared.backpressure

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * Backpressure filter for handling request queue limits and load shedding.
 */
@Component
@Order(3)
class BackpressureFilter(
    meterRegistry: MeterRegistry
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(BackpressureFilter::class.java)
    
    private val activeRequests = AtomicInteger(0)
    private val rejectedRequests = AtomicLong(0)
    private val totalRequests = AtomicLong(0)
    
    private val maxConcurrentRequests = System.getProperty("backpressure.max.concurrent.requests", "100").toInt()
    private val maxQueueSize = System.getProperty("backpressure.max.queue.size", "50").toInt()

    init {
        Gauge.builder("backpressure.active_requests", activeRequests) { it.get().toDouble() }
            .description("Number of currently active requests")
            .register(meterRegistry)
        
        Gauge.builder("backpressure.rejected_requests", rejectedRequests) { it.get().toDouble() }
            .description("Total number of rejected requests due to backpressure")
            .register(meterRegistry)
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (request.requestURI.startsWith("/actuator/health") ||
            request.requestURI.startsWith("/health")) {
            filterChain.doFilter(request, response)
            return
        }

        totalRequests.incrementAndGet()

        val currentActive = activeRequests.get()
        if (currentActive >= maxConcurrentRequests) {
            rejectedRequests.incrementAndGet()
            logger.warn("Request rejected due to backpressure. Active: {}/{}, Queue: {}", 
                currentActive, maxConcurrentRequests, getQueueSize())
            
            sendBackpressureResponse(response)
            return
        }

        activeRequests.incrementAndGet()

        try {
            filterChain.doFilter(request, response)
        } finally {
            activeRequests.decrementAndGet()
        }
    }

    private fun sendBackpressureResponse(response: HttpServletResponse) {
        response.status = HttpStatus.SERVICE_UNAVAILABLE.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = StandardCharsets.UTF_8.name()
        response.setHeader("Retry-After", "60")
        
        val errorJson = """{"error":"Service Unavailable","message":"Service is currently overloaded. Please retry after 60 seconds.","retryAfter":60}"""
        response.writer.write(errorJson)
    }

    private fun getQueueSize(): Int {
        return 0 // Placeholder
    }

    fun getActiveRequests(): Int = activeRequests.get()
    fun getRejectedRequests(): Long = rejectedRequests.get()
    fun isUnderBackpressure(): Boolean = activeRequests.get() >= maxConcurrentRequests
}

