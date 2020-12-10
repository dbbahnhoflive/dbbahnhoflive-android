/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.analytics

import android.app.Activity

open class TrackingDelegate {
    open fun trackAction(tag: String, contextVariables: Map<String, Any>) {
    }

    open fun trackState(tag: String, contextVariables: Map<String, Any>) {
    }

    open fun collectLifecycleData(activity: Activity) {
    }

    open fun pauseCollectingLifecycleData() {
    }

    open var consentState: ConsentState = ConsentState.PENDING

}