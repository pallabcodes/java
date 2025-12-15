package com.example.kotlinpay.shared.idempotency

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.nio.charset.StandardCharsets
import java.time.Duration

/**
 * Idempotency filter for handling idempotency keys in API requests.
 */
@Component
@Order(2)
class IdempotencyFilter(
    private val idempotencyService: IdempotencyService,
    private val objectMapper: ObjectMapper
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(IdempotencyFilter::class.java)
    
    companion object {
        private const val IDEMPOTENCY_KEY_HEADER = "Idempotency-Key"
        private const val IDEMPOTENCY_REPLAY_HEADER = "Idempotency-Replay"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (!isMutatingOperation(request)) {
            filterChain.doFilter(request, response)
            return
        }

        val idempotencyKey = request.getHeader(IDEMPOTENCY_KEY_HEADER)
        
        if (idempotencyKey.isNullOrBlank()) {
            filterChain.doFilter(request, response)
            return
        }

        if (!isValidIdempotencyKey(idempotencyKey)) {
            sendErrorResponse(response, HttpStatus.BAD_REQUEST, 
                "Invalid idempotency key format. Must be a valid UUID or alphanumeric string.")
            return
        }

        if (idempotencyService.isProcessed(idempotencyKey)) {
            val storedResponse = idempotencyService.getStoredResponse(idempotencyKey)
            if (storedResponse != null) {
                logger.debug("Returning stored response for idempotency key: {}", idempotencyKey)
                response.status = HttpStatus.OK.value()
                response.contentType = MediaType.APPLICATION_JSON_VALUE
                response.setHeader(IDEMPOTENCY_REPLAY_HEADER, "true")
                response.writer.write(storedResponse)
                return
            }

            logger.warn("Idempotency key already processed but no stored response: {}", idempotencyKey)
            sendErrorResponse(response, HttpStatus.CONFLICT,
                "Request with this idempotency key was already processed.")
            return
        }

        val claimed = idempotencyService.claim(idempotencyKey, Duration.ofHours(24))
        if (!claimed) {
            sendErrorResponse(response, HttpStatus.CONFLICT,
                "Request with this idempotency key is being processed.")
            return
        }

        val wrappedRequest = ContentCachingRequestWrapper(request)
        val wrappedResponse = ContentCachingResponseWrapper(response)

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse)

            if (wrappedResponse.status in 200..299) {
                val responseBody = wrappedResponse.contentAsByteArray
                if (responseBody.isNotEmpty()) {
                    val responseBodyString = String(responseBody, StandardCharsets.UTF_8)
                    idempotencyService.storeResponse(idempotencyKey, responseBodyString, Duration.ofHours(24))
                }
            } else {
                idempotencyService.release(idempotencyKey)
            }

            wrappedResponse.copyBodyToResponse()
        } catch (e: Exception) {
            idempotencyService.release(idempotencyKey)
            throw e
        }
    }

    private fun isMutatingOperation(request: HttpServletRequest): Boolean {
        val method = request.method
        return HttpMethod.POST.matches(method) ||
               HttpMethod.PUT.matches(method) ||
               HttpMethod.PATCH.matches(method) ||
               HttpMethod.DELETE.matches(method)
    }

    private fun isValidIdempotencyKey(key: String): Boolean {
        if (key.isBlank()) {
            return false
        }
        
        return key.length in 1..255 && key.matches(Regex("^[a-zA-Z0-9\\-_]+$"))
    }

    private fun sendErrorResponse(response: HttpServletResponse, status: HttpStatus, message: String) {
        response.status = status.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = StandardCharsets.UTF_8.name()
        
        val errorJson = """{"error":"${status.reasonPhrase}","message":"$message","status":${status.value()}}"""
        response.writer.write(errorJson)
    }
}

