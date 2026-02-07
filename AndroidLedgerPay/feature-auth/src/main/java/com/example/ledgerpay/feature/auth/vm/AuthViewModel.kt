package com.example.ledgerpay.feature.auth.vm

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ledgerpay.core.data.AuthRepository
import com.example.ledgerpay.core.data.telemetry.Monitoring
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import javax.inject.Inject

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data object BiometricPrompt : AuthState()
    data class Success(val userId: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val monitoring: Monitoring,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _biometricAvailable = MutableStateFlow(false)
    val biometricAvailable: StateFlow<Boolean> = _biometricAvailable.asStateFlow()

    init {
        checkBiometricAvailability()
        checkExistingSession()
    }

    fun login(email: String, password: String) {
        monitoring.logUserAction("login_attempted", mapOf("email" to email))

        // Security: Input validation
        if (!isValidEmail(email)) {
            monitoring.logSecurityEvent("invalid_email_format", mapOf("email" to email))
            _authState.value = AuthState.Error("Invalid email format")
            return
        }

        if (!isValidPassword(password)) {
            monitoring.logSecurityEvent("invalid_password_format")
            _authState.value = AuthState.Error("Password must be at least 8 characters")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            when (val result = authRepository.login(email, password)) {
                is AuthRepository.Result.Success -> {
                    monitoring.logUserAction("login_success", mapOf("user_id" to result.data.userId))
                    _authState.value = AuthState.Success(result.data.userId)
                }
                is AuthRepository.Result.Error -> {
                    monitoring.logUserAction("login_failed", mapOf("error" to result.exception.message))
                    _authState.value = AuthState.Error(mapErrorMessage(result.exception))
                }
            }
        }
    }

    fun authenticateWithBiometrics(activity: FragmentActivity, executor: Executor) {
        monitoring.logUserAction("biometric_auth_attempted")

        val biometricManager = BiometricManager.from(context)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Authenticate with Biometrics")
                    .setSubtitle("Use your fingerprint or face to login")
                    .setNegativeButtonText("Use Password")
                    .build()

                val biometricPrompt = BiometricPrompt(activity, executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            viewModelScope.launch {
                                monitoring.logUserAction("biometric_auth_success")
                                when (val sessionResult = authRepository.restoreSession()) {
                                    is AuthRepository.Result.Success -> {
                                        _authState.value = AuthState.Success(sessionResult.data.userId)
                                    }
                                    is AuthRepository.Result.Error -> {
                                        _authState.value = AuthState.Error("Session expired. Please login with password.")
                                    }
                                }
                            }
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            monitoring.logUserAction("biometric_auth_error", mapOf(
                                "error_code" to errorCode,
                                "error_message" to errString.toString()
                            ))
                            _authState.value = AuthState.Error("Biometric authentication failed: $errString")
                        }

                        override fun onAuthenticationFailed() {
                            monitoring.logUserAction("biometric_auth_failed")
                            _authState.value = AuthState.Error("Biometric authentication failed")
                        }
                    })

                _authState.value = AuthState.BiometricPrompt
                biometricPrompt.authenticate(promptInfo)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                monitoring.logSecurityEvent("biometric_not_supported", mapOf("reason" to "no_hardware"))
                _authState.value = AuthState.Error("Biometric authentication not supported on this device")
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                monitoring.logSecurityEvent("biometric_not_available", mapOf("reason" to "hardware_unavailable"))
                _authState.value = AuthState.Error("Biometric hardware unavailable")
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                monitoring.logSecurityEvent("biometric_not_enrolled", mapOf("reason" to "no_enrollment"))
                _authState.value = AuthState.Error("No biometric credentials enrolled")
            }
        }
    }

    fun logout() {
        monitoring.logUserAction("logout")
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = AuthState.Idle
        }
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Idle
        }
    }

    private fun checkBiometricAvailability() {
        val biometricManager = BiometricManager.from(context)
        _biometricAvailable.value = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
                BiometricManager.BIOMETRIC_SUCCESS
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            when (val result = authRepository.checkSession()) {
                is AuthRepository.Result.Success -> {
                    monitoring.logUserAction("session_restored", mapOf("user_id" to result.data.userId))
                    _authState.value = AuthState.Success(result.data.userId)
                }
                is AuthRepository.Result.Error -> {
                    // Session invalid, stay in idle state
                    monitoring.logUserAction("session_invalid")
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }

    private fun mapErrorMessage(exception: Exception): String {
        return when (exception) {
            is SecurityException -> "Invalid credentials"
            is IllegalArgumentException -> exception.message ?: "Invalid input"
            else -> when (exception.message) {
                "Network unavailable" -> "No internet connection. Please check your connection and try again."
                "Request timeout" -> "Request timed out. Please try again."
                "Rate limit exceeded" -> "Too many login attempts. Please wait and try again."
                "Server error" -> "Server temporarily unavailable. Please try again later."
                else -> "Login failed. Please check your credentials and try again."
            }
        }
    }
}
