/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.analytics

import android.util.Log
import de.deutschebahn.bahnhoflive.BaseApplication

open class IssueTracker(application: BaseApplication) {

    companion object {
        val TAG by lazy { IssueTracker::class.java.simpleName }

        private lateinit var instanceRetriever: () -> IssueTracker

        val instance by lazy { instanceRetriever() }
    }

    init {
        instanceRetriever = { application.issueTracker }
    }

    open fun log(msg: String) {
        Log.d(TAG, msg)
    }

    open fun dispatchThrowable(throwable: Throwable) {
        Log.i(TAG, "Catched", throwable)
    }

    open fun setCondition(key: String, value: String?) {
        log("Condition '$key' is now '$value'.")
    }

}