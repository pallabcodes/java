package com.example.ledgerpay.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PinValidationTest {

    @Test
    fun `debug allows empty pins`() {
        val pins = PinValidation.validatedPins(
            isDebugBuild = true,
            primaryPin = "",
            backupPin = ""
        )

        assertTrue(pins.isEmpty())
    }

    @Test(expected = IllegalStateException::class)
    fun `release rejects empty pins`() {
        PinValidation.validatedPins(
            isDebugBuild = false,
            primaryPin = "",
            backupPin = ""
        )
    }

    @Test(expected = IllegalStateException::class)
    fun `release requires backup pin for rotation`() {
        PinValidation.validatedPins(
            isDebugBuild = false,
            primaryPin = "sha256/4y/HZzNGpAX5X0dGL2hI9sE3XGQz6wK8kEb9zTQ6QK8=",
            backupPin = ""
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects malformed pin prefix`() {
        PinValidation.validatedPins(
            isDebugBuild = false,
            primaryPin = "abc/4y/HZzNGpAX5X0dGL2hI9sE3XGQz6wK8kEb9zTQ6QK8=",
            backupPin = "sha256/vH+6S8v7dUt2E8H4pN6v8u5q2l0x9r3w7k1m4n2p6Q8="
        )
    }

    @Test(expected = IllegalStateException::class)
    fun `rejects duplicate pins`() {
        val pin = "sha256/4y/HZzNGpAX5X0dGL2hI9sE3XGQz6wK8kEb9zTQ6QK8="
        PinValidation.validatedPins(
            isDebugBuild = false,
            primaryPin = pin,
            backupPin = pin
        )
    }

    @Test
    fun `accepts valid release pins`() {
        val pins = PinValidation.validatedPins(
            isDebugBuild = false,
            primaryPin = "sha256/4y/HZzNGpAX5X0dGL2hI9sE3XGQz6wK8kEb9zTQ6QK8=",
            backupPin = "sha256/vH+6S8v7dUt2E8H4pN6v8u5q2l0x9r3w7k1m4n2p6Q8="
        )

        assertEquals(2, pins.size)
    }
}
