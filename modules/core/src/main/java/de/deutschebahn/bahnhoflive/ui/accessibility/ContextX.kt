package de.deutschebahn.bahnhoflive.ui.accessibility

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.core.content.getSystemService

val Context.accessibilityManager get() = getSystemService<AccessibilityManager>()

val Context.isSpokenFeedbackAccessibilityEnabled
    get() = accessibilityManager?.run {
        isEnabled && getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_SPOKEN).isNotEmpty()
    } == true