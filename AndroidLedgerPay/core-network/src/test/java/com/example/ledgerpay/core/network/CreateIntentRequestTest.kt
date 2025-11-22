package com.example.ledgerpay.core.network

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertFailsWith

class CreateIntentRequestTest {

    @Test
    fun `valid request creates successfully`() {
        // Given
        val amount = 1000L
        val currency = "USD"

        // When
        val request = CreateIntentRequest(amount, currency)

        // Then
        assertEquals(amount, request.amountMinor)
        assertEquals(currency, request.currency)
    }

    @Test
    fun `negative amount throws exception`() {
        // When & Then
        val exception = assertFailsWith<IllegalArgumentException> {
            CreateIntentRequest(-100L, "USD")
        }
        assertEquals("Amount must be positive", exception.message)
    }

    @Test
    fun `zero amount throws exception`() {
        // When & Then
        val exception = assertFailsWith<IllegalArgumentException> {
            CreateIntentRequest(0L, "USD")
        }
        assertEquals("Amount must be positive", exception.message)
    }

    @Test
    fun `amount too large throws exception`() {
        // When & Then
        val exception = assertFailsWith<IllegalArgumentException> {
            CreateIntentRequest(1_000_000_01L, "USD") // $10,000.01
        }
        assertEquals("Amount too large", exception.message)
    }

    @Test
    fun `maximum allowed amount succeeds`() {
        // When
        val request = CreateIntentRequest(1_000_000_00L, "USD") // $10,000.00

        // Then
        assertEquals(1_000_000_00L, request.amountMinor)
    }

    @Test
    fun `empty currency throws exception`() {
        // When & Then
        val exception = assertFailsWith<IllegalArgumentException> {
            CreateIntentRequest(1000L, "")
        }
        assertEquals("Currency is required", exception.message)
    }

    @Test
    fun `blank currency throws exception`() {
        // When & Then
        val exception = assertFailsWith<IllegalArgumentException> {
            CreateIntentRequest(1000L, "   ")
        }
        assertEquals("Currency is required", exception.message)
    }

    @Test
    fun `currency too short throws exception`() {
        // When & Then
        val exception = assertFailsWith<IllegalArgumentException> {
            CreateIntentRequest(1000L, "US")
        }
        assertEquals("Currency must be 3 characters", exception.message)
    }

    @Test
    fun `currency too long throws exception`() {
        // When & Then
        val exception = assertFailsWith<IllegalArgumentException> {
            CreateIntentRequest(1000L, "USDD")
        }
        assertEquals("Currency must be 3 characters", exception.message)
    }

    @Test
    fun `invalid currency code throws exception`() {
        // When & Then
        val exception = assertFailsWith<IllegalArgumentException> {
            CreateIntentRequest(1000L, "XYZ")
        }
        assertEquals("Invalid currency code", exception.message)
    }

    @Test
    fun `valid currencies succeed`() {
        val validCurrencies = listOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD")

        validCurrencies.forEach { currency ->
            // When
            val request = CreateIntentRequest(1000L, currency)

            // Then
            assertEquals(currency, request.currency)
        }
    }

    @Test
    fun `lowercase currency throws exception`() {
        // When & Then
        val exception = assertFailsWith<IllegalArgumentException> {
            CreateIntentRequest(1000L, "usd")
        }
        assertEquals("Invalid currency code", exception.message)
    }
}
