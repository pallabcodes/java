package com.example.ledgerpay.core.data

import com.example.ledgerpay.core.data.prefs.SecureStorage
import com.example.ledgerpay.core.network.PaymentsApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthRepositoryTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var mockSecureStorage: SecureStorage
    private lateinit var mockApi: PaymentsApi

    @Before
    fun setUp() {
        mockSecureStorage = mockk(relaxed = true)
        mockApi = mockk()
        authRepository = AuthRepository(mockSecureStorage, mockApi)
    }

    @Test
    fun `login success should store session data securely`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val expectedResponse = PaymentsApi.LoginResponse(
            userId = "user123",
            email = email,
            token = "jwt.token.here"
        )

        coEvery { mockApi.login(any()) } returns expectedResponse

        // When
        val result = authRepository.login(email, password)

        // Then
        assertTrue(result is AuthRepository.Result.Success)
        val session = (result as AuthRepository.Result.Success).data

        assertEquals("user123", session.userId)
        assertEquals(email, session.email)
        assertEquals("jwt.token.here", session.token)

        coVerify { mockSecureStorage.saveAuthToken("jwt.token.here") }
        coVerify { mockSecureStorage.saveUserId("user123") }
    }

    @Test
    fun `login failure should return error`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "wrongpassword"

        coEvery { mockApi.login(any()) } throws SecurityException("Invalid credentials")

        // When
        val result = authRepository.login(email, password)

        // Then
        assertTrue(result is AuthRepository.Result.Error)
        val error = (result as AuthRepository.Result.Error).exception
        assertTrue(error is SecurityException)
        assertEquals("Invalid credentials", error.message)
    }

    @Test
    fun `checkSession with valid token should return session`() = runTest {
        // Given
        coEvery { mockSecureStorage.getAuthToken() } returns "valid.jwt.token"
        coEvery { mockSecureStorage.getUserId() } returns "user123"

        // When
        val result = authRepository.checkSession()

        // Then
        assertTrue(result is AuthRepository.Result.Success)
        val session = (result as AuthRepository.Result.Success).data
        assertEquals("user123", session.userId)
        assertEquals("valid.jwt.token", session.token)
    }

    @Test
    fun `checkSession with missing token should return error`() = runTest {
        // Given
        coEvery { mockSecureStorage.getAuthToken() } returns null
        coEvery { mockSecureStorage.getUserId() } returns "user123"

        // When
        val result = authRepository.checkSession()

        // Then
        assertTrue(result is AuthRepository.Result.Error)
        val error = (result as AuthRepository.Result.Error).exception
        assertTrue(error is SecurityException)
    }

    @Test
    fun `logout should clear all session data`() {
        // When
        authRepository.logout()

        // Then
        coVerify { mockSecureStorage.clearAuthToken() }
        coVerify { mockSecureStorage.clearAll() }
    }

    @Test
    fun `isLoggedIn returns true when both token and userId exist`() {
        // Given
        coEvery { mockSecureStorage.getAuthToken() } returns "token"
        coEvery { mockSecureStorage.getUserId() } returns "user123"

        // When & Then
        assertTrue(authRepository.isLoggedIn())
    }
}
