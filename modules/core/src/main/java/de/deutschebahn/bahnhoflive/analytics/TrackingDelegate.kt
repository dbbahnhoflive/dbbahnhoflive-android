package de.deutschebahn.bahnhoflive.analytics

import android.app.Activity

open class TrackingDelegate {
    open fun trackAction(tag: String, contextVariables: MutableMap<String, Any>) {
    }

    open fun trackState(tag: String, contextVariables: MutableMap<String, Any>) {
    }

    open fun collectLifecycleData(activity: Activity) {
    }

    open fun pauseCollectingLifecycleData() {
    }

    open var optOut: Boolean = false
}