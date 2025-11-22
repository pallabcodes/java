package com.example.ledgerpay.core.network

import com.example.ledgerpay.core.data.prefs.SecureStorage
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiClient @Inject constructor(
    private val secureStorage: SecureStorage
) {

    private const val TIMEOUT_SECONDS = 30L
    private const val MAX_RETRIES = 3

    // Security: Certificate pinning for production
    private val certificatePinner = CertificatePinner.Builder()
        .add("api.stripe.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=") // Placeholder - replace with actual cert
        .build()

    // Security: Authentication interceptor
    private val authInterceptor = Interceptor { chain ->
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
            .header("User-Agent", "LedgerPay/1.0.0")
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
}