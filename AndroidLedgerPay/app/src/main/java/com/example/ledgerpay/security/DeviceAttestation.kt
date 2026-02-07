package com.example.ledgerpay.security

import android.content.Context
import android.os.Build
import android.util.Log
import com.example.ledgerpay.core.data.prefs.SecureStorage
import com.example.ledgerpay.core.network.IntegrityVerificationRequest
import com.example.ledgerpay.core.network.PaymentsApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceAttestationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val secureStorage: SecureStorage,
    private val paymentsApi: PaymentsApi
) {
    companion object {
        private const val TAG = "DeviceAttestation"
        private const val DEFAULT_MIN_VALIDITY_MS = 10 * 60 * 1000L
    }

    private val attestationMutex = Mutex()

    suspend fun ensureValidToken(minValidityMs: Long = DEFAULT_MIN_VALIDITY_MS): String? {
        secureStorage.getDeviceIntegrityToken(minValidityMs)?.let { return it }

        return attestationMutex.withLock {
            secureStorage.getDeviceIntegrityToken(minValidityMs)?.let { return@withLock it }
            when (val result = attestAndCache()) {
                is DeviceAttestationResult.Success -> result.token
                is DeviceAttestationResult.Failure -> null
            }
        }
    }

    suspend fun attestAndCache(nonce: String? = null): DeviceAttestationResult {
        val effectiveNonce = nonce ?: generateNonce()
        val rooted = isDeviceRooted()
        val signatureValid = verifyAppSignature()
        if (rooted || !signatureValid) {
            secureStorage.clearDeviceIntegrityToken()
            return DeviceAttestationResult.Failure("Local integrity checks failed")
        }

        return runCatching {
            val response = paymentsApi.verifyIntegrity(
                IntegrityVerificationRequest(
                    nonce = effectiveNonce,
                    rooted = rooted,
                    signatureValid = signatureValid
                )
            )
            val token = response.token
            val expiresAtEpochMs = response.expiresAtEpochMs
            if (!response.accepted || token.isNullOrBlank() || expiresAtEpochMs == null || expiresAtEpochMs <= System.currentTimeMillis()) {
                secureStorage.clearDeviceIntegrityToken()
                return@runCatching DeviceAttestationResult.Failure("Backend integrity verification rejected")
            }

            secureStorage.saveDeviceIntegrityToken(token, expiresAtEpochMs)
            DeviceAttestationResult.Success(
                deviceVerified = true,
                appVerified = true,
                token = token,
                details = mapOf(
                    "rooted" to rooted.toString(),
                    "signature_valid" to signatureValid.toString(),
                    "server_verified" to "true"
                )
            )
        }.getOrElse { error ->
            Log.e(TAG, "Integrity verification failed", error)
            secureStorage.clearDeviceIntegrityToken()
            DeviceAttestationResult.Failure("Attestation failed: ${error.message}")
        }
    }

    fun getCachedIntegrityToken(): String? = secureStorage.getDeviceIntegrityToken()

    fun isDeviceRooted(): Boolean {
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }

        val rootFiles = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su"
        )

        return rootFiles.any { path ->
            try {
                java.io.File(path).exists()
            } catch (_: Exception) {
                false
            }
        }
    }

    fun verifyAppSignature(): Boolean {
        return try {
            val packageManager = context.packageManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES
                )
                val signingInfo = packageInfo.signingInfo ?: return false
                if (signingInfo.hasMultipleSigners()) {
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
        } catch (e: Exception) {
            Log.w(TAG, "Signature verification failed", e)
            false
        }
    }

    private fun generateNonce(): String {
        val nonceBytes = ByteArray(16)
        java.security.SecureRandom().nextBytes(nonceBytes)
        return android.util.Base64.encodeToString(nonceBytes, android.util.Base64.NO_WRAP)
    }
}

sealed class DeviceAttestationResult {
    data class Success(
        val deviceVerified: Boolean,
        val appVerified: Boolean,
        val token: String,
        val details: Map<String, String>
    ) : DeviceAttestationResult()

    data class Failure(
        val reason: String
    ) : DeviceAttestationResult()
}
