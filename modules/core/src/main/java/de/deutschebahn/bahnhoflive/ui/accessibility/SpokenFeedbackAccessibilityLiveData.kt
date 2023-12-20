package de.deutschebahn.bahnhoflive.ui.accessibility

import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.lifecycle.LiveData
import de.deutschebahn.bahnhoflive.util.accessibility.accessibilityManager
import de.deutschebahn.bahnhoflive.util.accessibility.isSpokenFeedbackAccessibilityEnabled

class SpokenFeedbackAccessibilityLiveData(context: Context) : LiveData<Boolean>() {

    private val applicationContext = context.applicationContext

    private val accessibilityStateChangeListener =
        AccessibilityManager.AccessibilityStateChangeListener {
            updateValue()
        }

    override fun onActive() {
        super.onActive()

        updateValue()

        applicationContext.accessibilityManager?.addAccessibilityStateChangeListener(
            accessibilityStateChangeListener
        )
    }

    private fun updateValue() {
        value = applicationContext.isSpokenFeedbackAccessibilityEnabled
    }

    override fun onInactive() {
        applicationContext.accessibilityManager?.removeAccessibilityStateChangeListener(
            accessibilityStateChangeListener
        )
        super.onInactive()
    }

}