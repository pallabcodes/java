package com.example.ledgerpay.core.ui.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccessibilityManager @Inject constructor(
    private val context: Context
) {

    companion object {
        private const val TAG = "AccessibilityManager"
        private const val MIN_TALKBACK_VERSION = 8.0f
        private const val MIN_VOICE_ACCESS_VERSION = 5.0f
    }

    private val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

    private val _accessibilityState = MutableStateFlow(AccessibilityState())
    val accessibilityState = _accessibilityState.asStateFlow()

    init {
        updateAccessibilityState()
    }

    fun updateAccessibilityState() {
        val isEnabled = accessibilityManager.isEnabled
        val isTouchExplorationEnabled = accessibilityManager.isTouchExplorationEnabled
        val enabledServices = getEnabledAccessibilityServices()

        val state = AccessibilityState(
            isEnabled = isEnabled,
            isTouchExplorationEnabled = isTouchExplorationEnabled,
            isTalkBackEnabled = enabledServices.contains("com.google.android.marvin.talkback"),
            isVoiceAccessEnabled = enabledServices.contains("com.google.android.apps.accessibility.voiceaccess"),
            isSwitchAccessEnabled = enabledServices.contains("com.android.switchaccess"),
            isSelectToSpeakEnabled = enabledServices.contains("com.google.android.accessibility.selecttospeak"),
            enabledServices = enabledServices,
            recommendedSettings = getRecommendedSettings()
        )

        _accessibilityState.value = state
        Log.d(TAG, "Accessibility state updated: $state")
    }

    private fun getEnabledAccessibilityServices(): Set<String> {
        return try {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )

            enabledServices?.split(":")?.mapNotNull { service ->
                service.substringBefore("/")
            }?.toSet() ?: emptySet()

        } catch (e: Exception) {
            Log.w(TAG, "Failed to get enabled accessibility services", e)
            emptySet()
        }
    }

    private fun getRecommendedSettings(): List<AccessibilityRecommendation> {
        val recommendations = mutableListOf<AccessibilityRecommendation>()

        if (!accessibilityManager.isEnabled) {
            recommendations.add(
                AccessibilityRecommendation(
                    type = AccessibilityRecommendationType.ENABLE_ACCESSIBILITY,
                    title = "Enable Accessibility",
                    description = "Enable accessibility services for better app usability",
                    severity = AccessibilitySeverity.HIGH
                )
            )
        }

        if (!accessibilityManager.isTouchExplorationEnabled) {
            recommendations.add(
                AccessibilityRecommendation(
                    type = AccessibilityRecommendationType.ENABLE_TOUCH_EXPLORATION,
                    title = "Enable Touch Exploration",
                    description = "Enable touch exploration to navigate the screen",
                    severity = AccessibilitySeverity.MEDIUM
                )
            )
        }

        // Check for minimum required versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                val talkBackVersion = getTalkBackVersion()
                if (talkBackVersion < MIN_TALKBACK_VERSION) {
                    recommendations.add(
                        AccessibilityRecommendation(
                            type = AccessibilityRecommendationType.UPDATE_TALKBACK,
                            title = "Update TalkBack",
                            description = "Update TalkBack to version $MIN_TALKBACK_VERSION or higher for best experience",
                            severity = AccessibilitySeverity.MEDIUM
                        )
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to check TalkBack version", e)
            }
        }

        return recommendations
    }

    private fun getTalkBackVersion(): Float {
        return try {
            val packageInfo = context.packageManager.getPackageInfo("com.google.android.marvin.talkback", 0)
            val versionName = packageInfo.versionName
            versionName.substringBefore(".").toFloat()
        } catch (e: Exception) {
            0.0f
        }
    }

    fun announceForAccessibility(text: String) {
        if (accessibilityManager.isEnabled) {
            val event = AccessibilityEvent.obtain().apply {
                eventType = AccessibilityEvent.TYPE_ANNOUNCEMENT
                this.text.add(text)
                className = javaClass.name
                packageName = context.packageName
            }
            accessibilityManager.sendAccessibilityEvent(event)
            Log.d(TAG, "Accessibility announcement: $text")
        }
    }

    fun performAccessibilityAction(action: AccessibilityAction) {
        when (action) {
            AccessibilityAction.NEXT -> announceForAccessibility("Moving to next item")
            AccessibilityAction.PREVIOUS -> announceForAccessibility("Moving to previous item")
            AccessibilityAction.SELECT -> announceForAccessibility("Item selected")
            AccessibilityAction.ACTIVATE -> announceForAccessibility("Action activated")
            AccessibilityAction.SCROLL_UP -> announceForAccessibility("Scrolling up")
            AccessibilityAction.SCROLL_DOWN -> announceForAccessibility("Scrolling down")
        }
    }

    fun getAccessibilitySettingsIntent() {
        // Return intent to open accessibility settings
        // Caller should use: startActivity(accessibilityManager.getAccessibilitySettingsIntent())
    }

    fun isAccessibilityServiceEnabled(serviceId: String): Boolean {
        return getEnabledAccessibilityServices().contains(serviceId)
    }

    fun getContentDescription(text: String, context: String? = null): String {
        return if (context.isNullOrBlank()) {
            text
        } else {
            "$text, $context"
        }
    }

    fun validateColorContrast(foregroundColor: Int, backgroundColor: Int): Boolean {
        // Basic color contrast validation (WCAG AA requires 4.5:1 for normal text, 3:1 for large text)
        // This is a simplified implementation - in production, use a proper color contrast library
        val contrastRatio = calculateContrastRatio(foregroundColor, backgroundColor)
        return contrastRatio >= 4.5
    }

    private fun calculateContrastRatio(color1: Int, color2: Int): Double {
        // Simplified contrast calculation
        // In production, use proper luminance calculation
        val lum1 = getRelativeLuminance(color1)
        val lum2 = getRelativeLuminance(color2)

        val lighter = maxOf(lum1, lum2)
        val darker = minOf(lum1, lum2)

        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun getRelativeLuminance(color: Int): Double {
        // Extract RGB components
        val r = (color shr 16) and 0xFF
        val g = (color shr 8) and 0xFF
        val b = color and 0xFF

        // Convert to linear RGB
        val rLinear = if (r <= 10) r / 3294.0 else Math.pow((r + 14.025) / 269.025, 2.4)
        val gLinear = if (g <= 10) g / 3294.0 else Math.pow((g + 14.025) / 269.025, 2.4)
        val bLinear = if (b <= 10) b / 3294.0 else Math.pow((b + 14.025) / 269.025, 2.4)

        // Calculate relative luminance
        return 0.2126 * rLinear + 0.7152 * gLinear + 0.0722 * bLinear
    }

    fun getRecommendedTimeoutMillis(): Long {
        // Return recommended timeout based on accessibility needs
        // WCAG recommends at least 20 seconds for time limits
        return when {
            accessibilityState.value.isTalkBackEnabled -> 30000L // 30 seconds for screen readers
            accessibilityState.value.isVoiceAccessEnabled -> 25000L // 25 seconds for voice control
            else -> 20000L // 20 seconds minimum
        }
    }

    fun shouldShowExtraTime(): Boolean {
        return accessibilityState.value.isTalkBackEnabled ||
               accessibilityState.value.isVoiceAccessEnabled ||
               accessibilityState.value.isSwitchAccessEnabled
    }

    fun getFontScale(): Float {
        return try {
            Settings.System.getFloat(context.contentResolver, Settings.System.FONT_SCALE, 1.0f)
        } catch (e: Exception) {
            1.0f
        }
    }

    fun isHighContrastEnabled(): Boolean {
        return try {
            Settings.Secure.getInt(context.contentResolver, "high_contrast", 0) == 1
        } catch (e: Exception) {
            false
        }
    }

    fun isReduceMotionEnabled(): Boolean {
        return try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1) == 0
        } catch (e: Exception) {
            false
        }
    }
}

data class AccessibilityState(
    val isEnabled: Boolean = false,
    val isTouchExplorationEnabled: Boolean = false,
    val isTalkBackEnabled: Boolean = false,
    val isVoiceAccessEnabled: Boolean = false,
    val isSwitchAccessEnabled: Boolean = false,
    val isSelectToSpeakEnabled: Boolean = false,
    val enabledServices: Set<String> = emptySet(),
    val recommendedSettings: List<AccessibilityRecommendation> = emptyList()
)

data class AccessibilityRecommendation(
    val type: AccessibilityRecommendationType,
    val title: String,
    val description: String,
    val severity: AccessibilitySeverity
)

enum class AccessibilityRecommendationType {
    ENABLE_ACCESSIBILITY,
    ENABLE_TOUCH_EXPLORATION,
    UPDATE_TALKBACK,
    UPDATE_VOICE_ACCESS,
    ADJUST_FONT_SIZE,
    ENABLE_HIGH_CONTRAST
}

enum class AccessibilitySeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

enum class AccessibilityAction {
    NEXT, PREVIOUS, SELECT, ACTIVATE, SCROLL_UP, SCROLL_DOWN
}

// ViewModel for accessibility management
class AccessibilityViewModel : ViewModel() {

    private val _accessibilityEnabled = mutableStateOf(false)
    val accessibilityEnabled: State<Boolean> = _accessibilityEnabled

    private val _fontScale = mutableStateOf(1.0f)
    val fontScale: State<Float> = _fontScale

    private val _highContrastEnabled = mutableStateOf(false)
    val highContrastEnabled: State<Boolean> = _highContrastEnabled

    private val _reduceMotionEnabled = mutableStateOf(false)
    val reduceMotionEnabled: State<Boolean> = _reduceMotionEnabled

    fun updateAccessibilitySettings(accessibilityManager: AccessibilityManager) {
        viewModelScope.launch {
            accessibilityManager.updateAccessibilityState()

            _accessibilityEnabled.value = accessibilityManager.accessibilityState.value.isEnabled
            _fontScale.value = accessibilityManager.getFontScale()
            _highContrastEnabled.value = accessibilityManager.isHighContrastEnabled()
            _reduceMotionEnabled.value = accessibilityManager.isReduceMotionEnabled()
        }
    }

    fun announceAction(action: String) {
        // This would be called from the composable using LocalAccessibilityManager.current
    }
}

// Composable function for accessibility-aware UI
@Composable
fun rememberAccessibilityState(accessibilityManager: AccessibilityManager): State<AccessibilityState> {
    val state = remember { mutableStateOf(AccessibilityState()) }

    LaunchedEffect(Unit) {
        accessibilityManager.accessibilityState.collect { newState ->
            state.value = newState
        }
    }

    return state
}

// CompositionLocal for accessibility manager
val LocalAccessibilityManager = staticCompositionLocalOf<AccessibilityManager> {
    error("AccessibilityManager not provided")
}
