package com.example.kotlinpay.shared.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import java.time.Instant
import java.util.UUID

/**
 * Global exception handler for consistent error responses across all payment services.
 * 
 * Provides:
 * - Consistent error response format
 * - Proper HTTP status codes
 * - Error correlation IDs for tracing
 * - Security-conscious error messages (no sensitive data exposure)
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.associate { 
            it.field to (it.defaultMessage ?: "Invalid value")
        }

        val errorId = UUID.randomUUID().toString()
        logger.warn("Validation failed [errorId={}, path={}]", errorId, request.getDescription(false))

        val error = ErrorResponse(
            errorId = errorId,
            timestamp = Instant.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Failed",
            message = "Request validation failed",
            path = request.getDescription(false).replace("uri=", ""),
            details = errors
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorId = UUID.randomUUID().toString()
        logger.warn("Illegal argument [errorId={}, message={}]", errorId, ex.message)

        val error = ErrorResponse(
            errorId = errorId,
            timestamp = Instant.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = ex.message ?: "Invalid request",
            path = request.getDescription(false).replace("uri=", "")
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(
        ex: IllegalStateException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorId = UUID.randomUUID().toString()
        logger.warn("Illegal state [errorId={}, message={}]", errorId, ex.message)

        val error = ErrorResponse(
            errorId = errorId,
            timestamp = Instant.now(),
            status = HttpStatus.CONFLICT.value(),
            error = "Conflict",
            message = ex.message ?: "Invalid state",
            path = request.getDescription(false).replace("uri=", "")
        )

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error)
    }

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(
        ex: ResourceNotFoundException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorId = UUID.randomUUID().toString()
        logger.debug("Resource not found [errorId={}, resource={}]", errorId, ex.resourceType)

        val error = ErrorResponse(
            errorId = errorId,
            timestamp = Instant.now(),
            status = HttpStatus.NOT_FOUND.value(),
            error = "Not Found",
            message = ex.message ?: "Resource not found",
            path = request.getDescription(false).replace("uri=", "")
        )

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }

    @ExceptionHandler(PaymentProcessingException::class)
    fun handlePaymentProcessingException(
        ex: PaymentProcessingException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorId = UUID.randomUUID().toString()
        logger.error("Payment processing failed [errorId={}, code={}]", errorId, ex.errorCode, ex)

        val error = ErrorResponse(
            errorId = errorId,
            timestamp = Instant.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Payment Processing Failed",
            message = "Payment processing failed. Please retry or contact support.",
            path = request.getDescription(false).replace("uri=", ""),
            errorCode = ex.errorCode
        )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }

    @ExceptionHandler(SecurityException::class)
    fun handleSecurityException(
        ex: SecurityException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorId = UUID.randomUUID().toString()
        logger.warn("Security violation [errorId={}]", errorId, ex)

        val error = ErrorResponse(
            errorId = errorId,
            timestamp = Instant.now(),
            status = HttpStatus.FORBIDDEN.value(),
            error = "Security Violation",
            message = "Access denied",
            path = request.getDescription(false).replace("uri=", "")
        )

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorId = UUID.randomUUID().toString()
        logger.error("Unexpected error [errorId={}]", errorId, ex)

        val error = ErrorResponse(
            errorId = errorId,
            timestamp = Instant.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = "An unexpected error occurred. Please contact support with error ID: $errorId",
            path = request.getDescription(false).replace("uri=", "")
        )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }

    /**
     * Standardized error response structure
     */
    data class ErrorResponse(
        val errorId: String,
        val timestamp: Instant,
        val status: Int,
        val error: String,
        val message: String,
        val path: String,
        val details: Map<String, Any>? = null,
        val errorCode: String? = null
    )
}

/**
 * Custom exceptions for payment platform
 */
class ResourceNotFoundException(
    val resourceType: String,
    val resourceId: String,
    message: String = "$resourceType with id '$resourceId' not found"
) : RuntimeException(message)

class PaymentProcessingException(
    val errorCode: String,
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

