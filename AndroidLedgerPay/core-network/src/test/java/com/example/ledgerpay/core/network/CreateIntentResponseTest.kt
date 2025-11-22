package com.example.ledgerpay.core.network

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertFailsWith

class CreateIntentResponseTest {

    @Test
    fun `valid response creates successfully`() {
        // Given
        val id = "pi_test123"
        val status = "requires_payment_method"

        // When
        val response = CreateIntentResponse(id, status)

        // Then
        assertEquals(id, response.id)
        assertEquals(status, response.status)
    }

    @Test
    fun `empty id throws exception`() {
        // When & Then
        val exception = assertFailsWith<IllegalArgumentException> {
            CreateIntentResponse("", "succeeded")
        }
        assertEquals("Payment ID cannot be blank", exception.message)
    }

    @Test
    fun `blank id throws exception`() {
        // When & Then
        val exception = assertFailsWith<IllegalArgumentException> {
            CreateIntentResponse("   ", "succeeded")
        }
        assertEquals("Payment ID cannot be blank", exception.message)
    }

    @Test
    fun `id with invalid characters throws exception`() {
        // When & Then
        val exception = assertFailsWith<IllegalArgumentException> {
            CreateIntentResponse("pi_test@123", "succeeded")
        }
        assertEquals("Invalid payment ID format", exception.message)
    }

    @Test
    fun `valid payment IDs succeed`() {
        val validIds = listOf(
            "pi_test123",
            "pi_1234567890",
            "pi_abcDEF123",
            "pi_test_123",
            "pi_123-456"
        )

        validIds.forEach { id ->
            // When
            val response = CreateIntentResponse(id, "succeeded")

            // Then
            assertEquals(id, response.id)
        }
    }

    @Test
    fun `empty status throws exception`() {
        // When & Then
        val exception = assertFailsWith<IllegalArgumentException> {
            CreateIntentResponse("pi_test123", "")
        }
        assertEquals("Invalid payment status received", exception.message)
    }

    @Test
    fun `invalid status throws exception`() {
        // When & Then
        val exception = assertFailsWith<IllegalArgumentException> {
            CreateIntentResponse("pi_test123", "invalid_status")
        }
        assertEquals("Invalid payment status", exception.message)
    }

    @Test
    fun `valid statuses succeed`() {
        val validStatuses = listOf(
            "requires_payment_method",
            "requires_confirmation",
            "processing",
            "succeeded",
            "failed",
            "canceled"
        )

        validStatuses.forEach { status ->
            // When
            val response = CreateIntentResponse("pi_test123", status)

            // Then
            assertEquals(status, response.status)
        }
    }
}
