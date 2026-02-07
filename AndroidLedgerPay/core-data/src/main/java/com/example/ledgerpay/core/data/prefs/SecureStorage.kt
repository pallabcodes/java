package com.example.ledgerpay.core.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Security: Secure storage for sensitive data using AndroidX Security
 */
@Singleton
class SecureStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "ledgerpay_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveAuthToken(token: String) {
        sharedPreferences.edit()
            .putString(KEY_AUTH_TOKEN, token)
            .putLong(KEY_TOKEN_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    fun getAuthToken(): String? {
        val token = sharedPreferences.getString(KEY_AUTH_TOKEN, null)
        val timestamp = sharedPreferences.getLong(KEY_TOKEN_TIMESTAMP, 0)

        // Security: Check if token is expired (24 hours)
        if (System.currentTimeMillis() - timestamp > TOKEN_EXPIRY_MS) {
            clearAuthToken()
            return null
        }

        return token
    }

    fun clearAuthToken() {
        sharedPreferences.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_TOKEN_TIMESTAMP)
            .apply()
    }

    fun saveUserId(userId: String) {
        sharedPreferences.edit()
            .putString(KEY_USER_ID, userId)
            .apply()
    }

    fun getUserId(): String? {
        return sharedPreferences.getString(KEY_USER_ID, null)
    }

    fun saveDeviceId(deviceId: String) {
        sharedPreferences.edit()
            .putString(KEY_DEVICE_ID, deviceId)
            .apply()
    }

    fun getDeviceId(): String? {
        return sharedPreferences.getString(KEY_DEVICE_ID, null)
    }

    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }

    fun storeEncryptionKey(alias: String, key: ByteArray) {
        val encodedKey = android.util.Base64.encodeToString(key, android.util.Base64.NO_WRAP)
        sharedPreferences.edit()
            .putString("enc_key_$alias", encodedKey)
            .apply()
    }

    fun getEncryptionKey(alias: String): ByteArray? {
        val encodedKey = sharedPreferences.getString("enc_key_$alias", null) ?: return null
        return try {
            android.util.Base64.decode(encodedKey, android.util.Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_TOKEN_TIMESTAMP = "token_timestamp"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_DEVICE_ID = "device_id"
        private const val TOKEN_EXPIRY_MS = 24 * 60 * 60 * 1000L // 24 hours
    }
}
