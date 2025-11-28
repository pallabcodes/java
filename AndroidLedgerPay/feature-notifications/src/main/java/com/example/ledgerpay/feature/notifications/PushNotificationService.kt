package com.example.ledgerpay.feature.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.ledgerpay.MainActivity
import com.example.ledgerpay.core.data.prefs.SecureStorage
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PushNotificationService @Inject constructor(
    private val context: Context,
    private val secureStorage: SecureStorage
) {

    companion object {
        private const val TAG = "PushNotificationService"
        private const val CHANNEL_ID_HIGH_PRIORITY = "high_priority_channel"
        private const val CHANNEL_ID_DEFAULT = "default_channel"
        private const val CHANNEL_ID_SECURITY = "security_channel"
        private const val ENCRYPTION_KEY_ALIAS = "notification_encryption_key"
        private const val GCM_TAG_LENGTH = 128
    }

    private val firebaseMessaging = FirebaseMessaging.getInstance()
    private val notificationManager = NotificationManagerCompat.from(context)

    private val _notificationState = MutableStateFlow<NotificationState>(NotificationState.Idle)
    val notificationState = _notificationState.asStateFlow()

    private val _fcmToken = MutableStateFlow<String?>(null)
    val fcmToken = _fcmToken.asStateFlow()

    init {
        createNotificationChannels()
        initializeFCM()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_ID_HIGH_PRIORITY,
                    "High Priority Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Critical payment alerts and security notifications"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 250, 250, 250)
                    enableLights(true)
                    lightColor = android.graphics.Color.RED
                },

                NotificationChannel(
                    CHANNEL_ID_DEFAULT,
                    "Payment Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "General payment and transaction notifications"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 100, 200, 100)
                },

                NotificationChannel(
                    CHANNEL_ID_SECURITY,
                    "Security Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Security-related notifications and alerts"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                    enableLights(true)
                    lightColor = android.graphics.Color.YELLOW
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
                }
            )

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channels.forEach { notificationManager.createNotificationChannel(it) }
        }
    }

    private fun initializeFCM() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = firebaseMessaging.token.await()
                _fcmToken.value = token
                secureStorage.storeFCMToken(token)
                Log.d(TAG, "FCM token initialized: ${token.take(20)}...")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get FCM token", e)
                _notificationState.value = NotificationState.Error("FCM initialization failed: ${e.message}")
            }
        }
    }

    suspend fun sendEncryptedNotification(
        title: String,
        message: String,
        priority: NotificationPriority = NotificationPriority.NORMAL,
        data: Map<String, String> = emptyMap()
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Encrypt sensitive notification data
                val encryptedTitle = encryptNotificationData(title)
                val encryptedMessage = encryptNotificationData(message)
                val encryptedData = encryptNotificationData(JSONObject(data).toString())

                // Create notification payload
                val notificationId = generateNotificationId()
                val notificationData = mapOf(
                    "notification_id" to notificationId,
                    "encrypted_title" to encryptedTitle,
                    "encrypted_message" to encryptedMessage,
                    "encrypted_data" to encryptedData,
                    "priority" to priority.name,
                    "timestamp" to System.currentTimeMillis().toString(),
                    "ttl" to "86400" // 24 hours
                )

                // In a real implementation, you would send this to your backend
                // which would then send the FCM message
                Log.d(TAG, "Notification prepared for sending: $notificationId")

                _notificationState.value = NotificationState.Sending(notificationId)

                // Simulate sending (replace with actual FCM call)
                delay(500)

                _notificationState.value = NotificationState.Sent(notificationId)
                Result.success(notificationId)

            } catch (e: Exception) {
                Log.e(TAG, "Failed to send notification", e)
                _notificationState.value = NotificationState.Error("Failed to send: ${e.message}")
                Result.failure(e)
            }
        }
    }

    fun displayNotification(
        title: String,
        message: String,
        priority: NotificationPriority = NotificationPriority.NORMAL,
        notificationId: Int = generateNotificationId().hashCode()
    ) {
        try {
            val channelId = when (priority) {
                NotificationPriority.HIGH -> CHANNEL_ID_HIGH_PRIORITY
                NotificationPriority.SECURITY -> CHANNEL_ID_SECURITY
                else -> CHANNEL_ID_DEFAULT
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your app icon
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(when (priority) {
                    NotificationPriority.HIGH, NotificationPriority.SECURITY -> NotificationCompat.PRIORITY_HIGH
                    else -> NotificationCompat.PRIORITY_DEFAULT
                })
                .apply {
                    if (priority == NotificationPriority.SECURITY) {
                        setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                        setCategory(NotificationCompat.CATEGORY_ALARM)
                    }
                }
                .build()

            if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                notificationManager.notify(notificationId, notification)
                Log.d(TAG, "Notification displayed: $notificationId")
            } else {
                Log.w(TAG, "Notifications are disabled")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to display notification", e)
        }
    }

    suspend fun decryptNotificationData(encryptedData: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val parts = encryptedData.split(":")
                if (parts.size != 3) throw IllegalArgumentException("Invalid encrypted data format")

                val iv = android.util.Base64.decode(parts[0], android.util.Base64.NO_WRAP)
                val encryptedBytes = android.util.Base64.decode(parts[1], android.util.Base64.NO_WRAP)
                val keyAlias = parts[2]

                val encryptionKey = getEncryptionKey(keyAlias)
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)

                cipher.init(Cipher.DECRYPT_MODE, encryptionKey, spec)
                val decryptedBytes = cipher.doFinal(encryptedBytes)

                String(decryptedBytes, StandardCharsets.UTF_8)

            } catch (e: Exception) {
                Log.e(TAG, "Failed to decrypt notification data", e)
                throw e
            }
        }
    }

    private suspend fun encryptNotificationData(data: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val encryptionKey = getEncryptionKey(ENCRYPTION_KEY_ALIAS)
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")

                val iv = ByteArray(12)
                java.security.SecureRandom().nextBytes(iv)
                val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)

                cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, parameterSpec)
                val encryptedBytes = cipher.doFinal(data.toByteArray(StandardCharsets.UTF_8))

                val ivEncoded = android.util.Base64.encodeToString(iv, android.util.Base64.NO_WRAP)
                val encryptedEncoded = android.util.Base64.encodeToString(encryptedBytes, android.util.Base64.NO_WRAP)

                "$ivEncoded:$encryptedEncoded:$ENCRYPTION_KEY_ALIAS"

            } catch (e: Exception) {
                Log.e(TAG, "Failed to encrypt notification data", e)
                throw e
            }
        }
    }

    private fun getEncryptionKey(keyAlias: String): SecretKey {
        val storedKey = secureStorage.getEncryptionKey(keyAlias)
        if (storedKey != null) {
            return SecretKeySpec(storedKey, "AES")
        }

        // Generate new key
        val keyGenerator = java.security.KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        val newKey = keyGenerator.generateKey()

        // Store the key securely
        secureStorage.storeEncryptionKey(keyAlias, newKey.encoded)

        return newKey
    }

    private fun generateNotificationId(): String {
        return "notification_${UUID.randomUUID()}"
    }

    fun subscribeToTopic(topic: String) {
        firebaseMessaging.subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Subscribed to topic: $topic")
                } else {
                    Log.e(TAG, "Failed to subscribe to topic: $topic", task.exception)
                }
            }
    }

    fun unsubscribeFromTopic(topic: String) {
        firebaseMessaging.unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Unsubscribed from topic: $topic")
                } else {
                    Log.e(TAG, "Failed to unsubscribe from topic: $topic", task.exception)
                }
            }
    }

    suspend fun updateNotificationPreferences(preferences: NotificationPreferences) {
        try {
            secureStorage.storeNotificationPreferences(preferences)
            Log.d(TAG, "Notification preferences updated")

            // Update FCM subscriptions based on preferences
            if (preferences.paymentAlerts) {
                subscribeToTopic("payments")
            } else {
                unsubscribeFromTopic("payments")
            }

            if (preferences.securityAlerts) {
                subscribeToTopic("security")
            } else {
                unsubscribeFromTopic("security")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to update notification preferences", e)
        }
    }

    fun getNotificationPreferences(): NotificationPreferences {
        return secureStorage.getNotificationPreferences()
    }

    fun cleanup() {
        _notificationState.value = NotificationState.Idle
        _fcmToken.value = null
    }
}

enum class NotificationPriority {
    LOW, NORMAL, HIGH, SECURITY
}

sealed class NotificationState {
    object Idle : NotificationState()
    data class Sending(val notificationId: String) : NotificationState()
    data class Sent(val notificationId: String) : NotificationState()
    data class Error(val message: String) : NotificationState()
}

data class NotificationPreferences(
    val paymentAlerts: Boolean = true,
    val securityAlerts: Boolean = true,
    val promotionalAlerts: Boolean = false,
    val transactionAlerts: Boolean = true,
    val marketingEmails: Boolean = false
)

// FCM Service class for handling incoming messages
class LedgerPayFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM", "Message received from: ${remoteMessage.from}")

        // Handle encrypted notification data
        remoteMessage.data["encrypted_title"]?.let { encryptedTitle ->
            remoteMessage.data["encrypted_message"]?.let { encryptedMessage ->
                // In a real app, you would decrypt these using the PushNotificationService
                // For demo, we'll show a placeholder notification

                val notificationId = remoteMessage.data["notification_id"]?.toIntOrNull() ?: 0

                showNotification(
                    "Secure Notification",
                    "You have received a secure notification",
                    notificationId
                )
            }
        }

        // Handle regular notifications
        remoteMessage.notification?.let { notification ->
            showNotification(
                notification.title ?: "LedgerPay",
                notification.body ?: "You have a new notification",
                remoteMessage.messageId?.hashCode() ?: 0
            )
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New FCM token: ${token.take(20)}...")
        // Send token to your backend server
    }

    private fun showNotification(title: String, message: String, notificationId: Int) {
        // Create and show notification using NotificationManager
        // Implementation would use the PushNotificationService.displayNotification method
        Log.d("FCM", "Showing notification: $title - $message")
    }
}
