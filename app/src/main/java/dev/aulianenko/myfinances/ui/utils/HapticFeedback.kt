package dev.aulianenko.myfinances.ui.utils

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/**
 * Utility class for providing haptic feedback in Compose UI.
 * Provides a consistent haptic experience across the app.
 */
class HapticFeedback(private val view: View) {

    /**
     * Light tap feedback - for button presses, toggles, etc.
     */
    fun click() {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    /**
     * Success feedback - for successful operations
     */
    fun success() {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }

    /**
     * Error feedback - for failed operations
     */
    fun error() {
        view.performHapticFeedback(HapticFeedbackConstants.REJECT)
    }

    /**
     * Long press feedback - for context menus, drag start
     */
    fun longPress() {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    /**
     * Light feedback - for subtle interactions
     */
    fun light() {
        view.performHapticFeedback(
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                HapticFeedbackConstants.GESTURE_START
            } else {
                HapticFeedbackConstants.VIRTUAL_KEY
            }
        )
    }

    /**
     * Heavy feedback - for important actions
     */
    fun heavy() {
        view.performHapticFeedback(
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                HapticFeedbackConstants.GESTURE_END
            } else {
                HapticFeedbackConstants.CONTEXT_CLICK
            }
        )
    }
}

/**
 * Remember a HapticFeedback instance for the current composition.
 * Usage: val haptic = rememberHapticFeedback()
 */
@Composable
fun rememberHapticFeedback(): HapticFeedback {
    val view = LocalView.current
    return remember(view) { HapticFeedback(view) }
}
