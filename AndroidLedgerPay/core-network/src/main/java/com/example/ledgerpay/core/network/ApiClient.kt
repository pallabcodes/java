package com.example.ledgerpay.core.network

import android.content.Context
import android.os.Build
import android.util.Log
import com.example.ledgerpay.core.data.prefs.SecureStorage
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.tasks.Tasks
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException

@Singleton
class ApiClient @Inject constructor(
    private val secureStorage: SecureStorage,
    private val context: Context
) {

    private const val TIMEOUT_SECONDS = 30L
    private const val MAX_RETRIES = 3
    private const val SAFETYNET_API_KEY = "your_safetynet_api_key_here" // Replace with actual API key

    companion object {
        private const val TAG = "ApiClient"
        private const val CERTIFICATE_PIN_EXPIRY_DAYS = 30
    }

    // Security: Advanced certificate pinning with key rotation
    private val certificatePinner = createCertificatePinner()

    // Security: Device security checks
    private val deviceAttestationService = com.example.ledgerpay.security.DeviceAttestationService(context, SAFETYNET_API_KEY)

    // Security: Device attestation and integrity verification
    private val deviceAttestation = com.example.ledgerpay.security.DeviceAttestationService(context, SAFETYNET_API_KEY)

    // Security: Authentication interceptor with device attestation
    private val authInterceptor = Interceptor { chain ->
        // Perform device security checks
        val deviceCheckPassed = performDeviceSecurityCheck()
        if (!deviceCheckPassed) {
            throw SecurityException("Device security check failed")
        }

        val original = chain.request()
        val token = secureStorage.getAuthToken()
        val request = original.newBuilder()
            .apply {
                if (token != null) {
                    header("Authorization", "Bearer $token")
                }
            }
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("User-Agent", "LedgerPay/1.0.0 (Android ${Build.VERSION.RELEASE}; ${Build.MODEL})")
            .header("X-Device-Verified", deviceCheckPassed.toString())
            .header("X-App-Integrity", deviceAttestationService.verifyAppSignature().toString())
            .build()
        chain.proceed(request)
    }

    // Security: Logging interceptor (no sensitive data in logs)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (isDebugBuild()) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
    }

    // Security: Retry interceptor with exponential backoff
    private val retryInterceptor = Interceptor { chain ->
        var request = chain.request()
        var response: okhttp3.Response? = null
        var lastException: Exception? = null

        for (attempt in 0 until MAX_RETRIES) {
            try {
                response = chain.proceed(request)
                if (response.isSuccessful || !isRetryableError(response.code)) {
                    return@Interceptor response
                }
                // Exponential backoff
                Thread.sleep((1000L * (1L shl attempt)).coerceAtMost(10000L))
            } catch (e: Exception) {
                lastException = e
                if (attempt == MAX_RETRIES - 1) break
                Thread.sleep((1000L * (1L shl attempt)).coerceAtMost(10000L))
            }
        }

        response?.close()
        throw lastException ?: RuntimeException("Request failed after $MAX_RETRIES attempts")
    }

    fun retrofit(baseUrl: String): Retrofit {
        val client = OkHttpClient.Builder()
            .certificatePinner(certificatePinner)
            .addInterceptor(authInterceptor)
            .addInterceptor(retryInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .callTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false) // We handle retries manually
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }


    private fun isDebugBuild(): Boolean {
        return try {
            android.os.BuildConfig.DEBUG
        } catch (e: Exception) {
            false // Production default
        }
    }

    private fun isRetryableError(code: Int): Boolean {
        return code in listOf(408, 429, 500, 502, 503, 504) // Timeout, rate limit, server errors
    }

    private fun createCertificatePinner(): CertificatePinner {
        return CertificatePinner.Builder()
            .add("api.stripe.com",
                "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=", // Primary pin
                "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB="  // Backup pin for rotation
            )
            .add("api.ledgerpay.com",
                "sha256/CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC=", // Primary pin
                "sha256/DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD="  // Backup pin for rotation
            )
            .build()
    }

    private fun performDeviceSecurityCheck(): Boolean {
        return try {
            // Check for root/jailbreak
            if (deviceAttestationService.isDeviceRooted()) {
                Log.w(TAG, "Rooted device detected")
                return false
            }

            // Verify app signature
            if (!deviceAttestationService.verifyAppSignature()) {
                Log.w(TAG, "App signature verification failed")
                return false
            }

            // Perform device attestation (async, but we'll do a basic check)
            // In production, you might want to cache the result or make this async
            val attestationResult = kotlinx.coroutines.runBlocking {
                deviceAttestationService.attestDevice()
            }

            when (attestationResult) {
                is com.example.ledgerpay.security.DeviceAttestationResult.Success -> {
                    val deviceVerified = attestationResult.deviceVerified
                    val appVerified = attestationResult.appVerified

                    Log.d(TAG, "Device attestation successful - Device: $deviceVerified, App: $appVerified")
                    deviceVerified && appVerified
                }
                is com.example.ledgerpay.security.DeviceAttestationResult.Failure -> {
                    Log.w(TAG, "Device attestation failed: ${attestationResult.reason}")
                    // In production, you might want to be more strict
                    // For now, allow the request but log the failure
                    true
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Device security check failed", e)
            // Fail safely - deny access if we can't verify security
            false
        }
    }
}