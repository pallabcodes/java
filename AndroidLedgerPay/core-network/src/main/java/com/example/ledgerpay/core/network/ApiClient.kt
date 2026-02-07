package com.example.ledgerpay.core.network

import android.content.Context
import android.os.Build
import android.util.Log
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient(
    private val context: Context,
    private val authTokenProvider: () -> String? = { null },
    private val deviceIntegrityTokenProvider: () -> String? = { null },
    private val additionalPinsByHost: Map<String, List<String>> = emptyMap(),
    private val enableVerboseLogging: Boolean = false
) {

    private val timeoutSeconds = 30L

    companion object {
        private const val TAG = "ApiClient"
        private const val DEVICE_CHECK_CACHE_TTL_MS = 5 * 60 * 1000L
    }

    private val certificatePinner = createCertificatePinner()
    private val userAgent = "LedgerPay/1.0.0 (Android ${Build.VERSION.RELEASE}; ${Build.MODEL})"
    private val deviceCheckLock = Any()

    @Volatile
    private var cachedDeviceCheckAtMs = 0L

    @Volatile
    private var cachedDeviceCheckResult: Boolean? = null

    private val authInterceptor = Interceptor { chain ->
        val deviceCheckPassed = performDeviceSecurityCheck()
        if (!deviceCheckPassed) {
            throw SecurityException("Device security check failed")
        }

        val original = chain.request()
        val token = authTokenProvider()
        val deviceIntegrityToken = deviceIntegrityTokenProvider()
        val request = original.newBuilder()
            .apply {
                if (token != null) {
                    header("Authorization", "Bearer $token")
                }
                if (deviceIntegrityToken != null) {
                    header("X-Device-Integrity-Token", deviceIntegrityToken)
                }
            }
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("User-Agent", userAgent)
            .header("X-Device-Verified", deviceCheckPassed.toString())
            .build()
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (enableVerboseLogging) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    fun retrofit(baseUrl: String): Retrofit {
        val client = OkHttpClient.Builder()
            .certificatePinner(certificatePinner)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .callTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    private fun createCertificatePinner(): CertificatePinner {
        val builder = CertificatePinner.Builder()
            .add("api.stripe.com",
                "sha256/4y/HZzNGpAX5X0dGL2hI9sE3XGQz6wK8kEb9zTQ6QK8="
            )

        additionalPinsByHost.forEach { (host, pins) ->
            pins.filter { it.isNotBlank() }.forEach { pin ->
                builder.add(host, pin)
            }
        }

        return builder.build()
    }

    private fun performDeviceSecurityCheck(): Boolean {
        val now = System.currentTimeMillis()
        cachedDeviceCheckResult?.let { cached ->
            if (now - cachedDeviceCheckAtMs < DEVICE_CHECK_CACHE_TTL_MS) {
                return cached
            }
        }

        return synchronized(deviceCheckLock) {
            val syncNow = System.currentTimeMillis()
            cachedDeviceCheckResult?.let { cached ->
                if (syncNow - cachedDeviceCheckAtMs < DEVICE_CHECK_CACHE_TTL_MS) {
                    return@synchronized cached
                }
            }

            val evaluated = evaluateDeviceSecurityCheck()
            cachedDeviceCheckResult = evaluated
            cachedDeviceCheckAtMs = syncNow
            evaluated
        }
    }

    private fun evaluateDeviceSecurityCheck(): Boolean {
        return try {
            if (isDeviceRooted()) {
                Log.w(TAG, "Rooted device detected")
                false
            } else if (!verifyAppSignature()) {
                Log.w(TAG, "App signature verification failed")
                false
            } else {
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Device security check failed", e)
            false
        }
    }

    private fun isDeviceRooted(): Boolean {
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }

        val rootPaths = arrayOf(
            "/system/app/Superuser.apk",
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/data/local/bin/su",
            "/data/local/xbin/su"
        )
        return rootPaths.any { path ->
            try {
                java.io.File(path).exists()
            } catch (_: Exception) {
                false
            }
        }
    }

    private fun verifyAppSignature(): Boolean {
        return try {
            val packageManager = context.packageManager
            val hasSignatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES
                )
                val signingInfo = packageInfo.signingInfo
                if (signingInfo == null) {
                    false
                } else if (signingInfo.hasMultipleSigners()) {
                    !signingInfo.apkContentsSigners.isNullOrEmpty()
                } else {
                    !signingInfo.signingCertificateHistory.isNullOrEmpty()
                }
            } else {
                @Suppress("DEPRECATION")
                val packageInfo = packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.GET_SIGNATURES
                )
                @Suppress("DEPRECATION")
                !packageInfo.signatures.isNullOrEmpty()
            }
            hasSignatures
        } catch (e: Exception) {
            false
        }
    }
}
