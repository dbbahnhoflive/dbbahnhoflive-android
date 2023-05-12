package de.deutschebahn.bahnhoflive.ui.accessibility

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.SharedPreferences
import android.view.accessibility.AccessibilityManager
import androidx.core.content.getSystemService

val Context.accessibilityManager get() = getSystemService<AccessibilityManager>()

val Context.isSpokenFeedbackAccessibilityEnabled
    get() = accessibilityManager?.run {
        isEnabled && getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_SPOKEN).isNotEmpty()
    } == true

val Context.GlobalPreferences: SharedPreferences
    get() = getSharedPreferences("global_settings.prefs", Context.MODE_PRIVATE)

val Context.TrackingPreferences: SharedPreferences
    get() = getSharedPreferences("tracking.prefs", Context.MODE_PRIVATE)

