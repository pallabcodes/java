package com.example.ledgerpay.core.ui.accessibility

import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager as PlatformAccessibilityManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AccessibilityManager(
    private val context: Context
) {
    private val platformManager =
        context.getSystemService(Context.ACCESSIBILITY_SERVICE) as PlatformAccessibilityManager

    private val _accessibilityState = MutableStateFlow(AccessibilityState())
    val accessibilityState = _accessibilityState.asStateFlow()

    init {
        updateAccessibilityState()
    }

    fun updateAccessibilityState() {
        val enabled = platformManager.isEnabled
        val touchExploration = platformManager.isTouchExplorationEnabled
        _accessibilityState.value = AccessibilityState(
            isEnabled = enabled,
            isTouchExplorationEnabled = touchExploration,
            isTalkBackEnabled = touchExploration
        )
    }

    fun announceForAccessibility(text: String) {
        if (!platformManager.isEnabled) {
            return
        }

        val event = AccessibilityEvent.obtain().apply {
            eventType = AccessibilityEvent.TYPE_ANNOUNCEMENT
            this.text.add(text)
            className = javaClass.name
            packageName = context.packageName
        }
        platformManager.sendAccessibilityEvent(event)
    }

    fun getFontScale(): Float {
        return try {
            Settings.System.getFloat(context.contentResolver, Settings.System.FONT_SCALE, 1.0f)
        } catch (_: Exception) {
            1.0f
        }
    }

    fun isHighContrastEnabled(): Boolean {
        return try {
            Settings.Secure.getInt(context.contentResolver, "high_text_contrast_enabled", 0) == 1
        } catch (_: Exception) {
            false
        }
    }

    fun isReduceMotionEnabled(): Boolean {
        return try {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            ) == 0f
        } catch (_: Exception) {
            false
        }
    }
}

data class AccessibilityState(
    val isEnabled: Boolean = false,
    val isTouchExplorationEnabled: Boolean = false,
    val isTalkBackEnabled: Boolean = false
)

@Composable
fun rememberAccessibilityState(accessibilityManager: AccessibilityManager): State<AccessibilityState> {
    return accessibilityManager.accessibilityState.collectAsState()
}

val LocalAccessibilityManager = staticCompositionLocalOf<AccessibilityManager> {
    error("AccessibilityManager not provided")
}
