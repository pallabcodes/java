package com.example.ledgerpay.security

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.tasks.Tasks
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.google.android.play.core.integrity.IntegrityTokenResponse
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class DeviceAttestationService(
    private val context: Context,
    private val safetyNetApiKey: String
) {

    companion object {
        private const val TAG = "DeviceAttestation"
        private const val TIMEOUT_SECONDS = 30L
        private const val NONCE_LENGTH = 16
    }

    private val integrityManager by lazy {
        IntegrityManagerFactory.create(context)
    }

    /**
     * Performs device attestation using Play Integrity API (recommended for Android 5.0+)
     * Falls back to SafetyNet on older devices or when Play Integrity is unavailable
     */
    suspend fun attestDevice(nonce: String? = null): DeviceAttestationResult {
        return try {
            if (isPlayIntegrityAvailable()) {
                performPlayIntegrityAttestation(nonce ?: generateNonce())
            } else {
                performSafetyNetAttestation(nonce ?: generateNonce())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Device attestation failed", e)
            DeviceAttestationResult.Failure("Attestation failed: ${e.message}")
        }
    }

    private suspend fun performPlayIntegrityAttestation(nonce: String): DeviceAttestationResult {
        return withTimeoutOrNull(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS)) {
            suspendCancellableCoroutine { continuation ->
                try {
                    val integrityTokenResponse = Tasks.await(
                        integrityManager.requestIntegrityToken(
                            IntegrityTokenRequest.builder()
                                .setNonce(nonce)
                                .build()
                        ),
                        TIMEOUT_SECONDS,
                        TimeUnit.SECONDS
                    )

                    val token = integrityTokenResponse.token()
                    val result = parsePlayIntegrityToken(token)

                    continuation.resume(result)

                } catch (e: Exception) {
                    Log.e(TAG, "Play Integrity attestation failed", e)
                    continuation.resumeWithException(e)
                }
            }
        } ?: DeviceAttestationResult.Failure("Play Integrity attestation timeout")
    }

    private suspend fun performSafetyNetAttestation(nonce: String): DeviceAttestationResult {
        return withTimeoutOrNull(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS)) {
            suspendCancellableCoroutine { continuation ->
                try {
                    val safetyNetResponse = Tasks.await(
                        SafetyNet.getClient(context).attest(nonce.toByteArray(), safetyNetApiKey),
                        TIMEOUT_SECONDS,
                        TimeUnit.SECONDS
                    )

                    val result = parseSafetyNetResponse(safetyNetResponse.jwsResult)
                    continuation.resume(result)

                } catch (e: Exception) {
                    Log.e(TAG, "SafetyNet attestation failed", e)
                    continuation.resumeWithException(e)
                }
            }
        } ?: DeviceAttestationResult.Failure("SafetyNet attestation timeout")
    }

    private fun parsePlayIntegrityToken(token: String): DeviceAttestationResult {
        return try {
            // In a real implementation, you would send this token to your backend
            // for verification with Google's servers. For demo purposes, we'll do basic parsing.

            // Token structure: header.payload.signature
            val parts = token.split(".")
            if (parts.size != 3) {
                return DeviceAttestationResult.Failure("Invalid token format")
            }

            val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE))
            val jsonPayload = JSONObject(payload)

            val deviceIntegrity = jsonPayload.optJSONObject("deviceIntegrity")
            val accountDetails = jsonPayload.optJSONObject("accountDetails")
            val appIntegrity = jsonPayload.optJSONObject("appIntegrity")

            val isDeviceVerified = deviceIntegrity?.optJSONArray("deviceRecognitionVerdict")
                ?.toString()?.contains("MEETS_DEVICE_INTEGRITY") ?: false

            val isAppVerified = appIntegrity?.optString("appRecognitionVerdict")
                ?.contains("PLAY_RECOGNIZED") ?: false

            if (isDeviceVerified && isAppVerified) {
                DeviceAttestationResult.Success(
                    deviceVerified = true,
                    appVerified = true,
                    token = token,
                    details = mapOf(
                        "deviceIntegrity" to deviceIntegrity.toString(),
                        "appIntegrity" to appIntegrity.toString(),
                        "accountDetails" to (accountDetails?.toString() ?: "N/A")
                    )
                )
            } else {
                DeviceAttestationResult.Success(
                    deviceVerified = isDeviceVerified,
                    appVerified = isAppVerified,
                    token = token,
                    details = mapOf(
                        "warnings" to "Device or app integrity checks failed"
                    )
                )
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse Play Integrity token", e)
            DeviceAttestationResult.Failure("Token parsing failed: ${e.message}")
        }
    }

    private fun parseSafetyNetResponse(jwsResult: String): DeviceAttestationResult {
        return try {
            // Parse JWS response - in production, verify signature on backend
            val parts = jwsResult.split(".")
            if (parts.size != 3) {
                return DeviceAttestationResult.Failure("Invalid JWS format")
            }

            val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.DEFAULT))
            val jsonPayload = JSONObject(payload)

            val basicIntegrity = jsonPayload.optBoolean("basicIntegrity", false)
            val ctsProfileMatch = jsonPayload.optBoolean("ctsProfileMatch", false)
            val evaluationType = jsonPayload.optString("evaluationType", "")

            val isDeviceVerified = basicIntegrity && ctsProfileMatch

            DeviceAttestationResult.Success(
                deviceVerified = isDeviceVerified,
                appVerified = true, // SafetyNet doesn't verify apps specifically
                token = jwsResult,
                details = mapOf(
                    "basicIntegrity" to basicIntegrity.toString(),
                    "ctsProfileMatch" to ctsProfileMatch.toString(),
                    "evaluationType" to evaluationType
                )
            )

        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse SafetyNet response", e)
            DeviceAttestationResult.Failure("SafetyNet parsing failed: ${e.message}")
        }
    }

    private fun isPlayIntegrityAvailable(): Boolean {
        return try {
            // Play Integrity is available on Android 5.0+ with Google Play Services
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
        } catch (e: Exception) {
            Log.w(TAG, "Failed to check Play Integrity availability", e)
            false
        }
    }

    private fun generateNonce(): String {
        val nonceBytes = ByteArray(NONCE_LENGTH)
        java.security.SecureRandom().nextBytes(nonceBytes)
        return android.util.Base64.encodeToString(nonceBytes, android.util.Base64.NO_WRAP)
    }

    /**
     * Checks if the device appears to be rooted or jailbroken
     */
    fun isDeviceRooted(): Boolean {
        return try {
            // Common root detection methods
            val buildTags = android.os.Build.TAGS
            if (buildTags != null && buildTags.contains("test-keys")) {
                return true
            }

            // Check for common root files
            val rootFiles = arrayOf(
                "/system/app/Superuser.apk",
                "/sbin/su",
                "/system/bin/su",
                "/system/xbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/su"
            )

            rootFiles.any { file ->
                try {
                    java.io.File(file).exists()
                } catch (e: Exception) {
                    false
                }
            }

        } catch (e: Exception) {
            Log.w(TAG, "Root detection check failed", e)
            false
        }
    }

    /**
     * Verifies app signature integrity
     */
    fun verifyAppSignature(): Boolean {
        return try {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(context.packageName,
                android.content.pm.PackageManager.GET_SIGNATURES)

            val signatures = packageInfo.signatures
            if (signatures.isNullOrEmpty()) {
                return false
            }

            // In production, you would compare against known good signatures
            // For demo, we just check that signatures exist
            signatures.isNotEmpty()

        } catch (e: Exception) {
            Log.w(TAG, "App signature verification failed", e)
            false
        }
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
