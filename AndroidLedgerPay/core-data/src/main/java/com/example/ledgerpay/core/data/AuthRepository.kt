package com.example.ledgerpay.core.data

import com.example.ledgerpay.core.data.prefs.SecureStorage
import com.example.ledgerpay.core.network.LoginRequest
import com.example.ledgerpay.core.network.PaymentsApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val secureStorage: SecureStorage,
    private val api: PaymentsApi
) {

    data class UserSession(val userId: String, val email: String, val token: String)

    sealed class Result<T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error<T>(val exception: Exception) : Result<T>()
    }

    suspend fun login(email: String, password: String): Result<UserSession> {
        return withContext(Dispatchers.IO) {
            try {
                // Call authentication API
                val response = api.login(LoginRequest(email, password))

                // Store session data securely
                secureStorage.saveAuthToken(response.token)
                secureStorage.saveUserId(response.userId)

                val session = UserSession(response.userId, response.email, response.token)
                Result.Success(session)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    suspend fun checkSession(): Result<UserSession> {
        return withContext(Dispatchers.IO) {
            try {
                val token = secureStorage.getAuthToken()
                val userId = secureStorage.getUserId()

                if (token == null || userId == null) {
                    Result.Error(SecurityException("No valid session"))
                } else {
                    // Validate token with server if needed
                    val session = UserSession(userId, "", token) // Email not stored locally
                    Result.Success(session)
                }
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    suspend fun restoreSession(): Result<UserSession> {
        return checkSession() // Same logic for biometric restore
    }

    fun logout() {
        secureStorage.clearAuthToken()
        secureStorage.clearAll()
    }

    fun isLoggedIn(): Boolean {
        return secureStorage.getAuthToken() != null && secureStorage.getUserId() != null
    }

    fun getCurrentUserId(): String? {
        return secureStorage.getUserId()
    }

    fun getCurrentToken(): String? {
        return secureStorage.getAuthToken()
    }
}
