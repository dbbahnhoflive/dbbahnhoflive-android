/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.analytics

import android.util.Log
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.repository.Station

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

    open fun dispatchThrowable(throwable: Throwable, hint: String? = null) {
        Log.i(TAG, "Catched", throwable)
    }

    open fun setTag(key: String, value: String?) {
        log("Tag '$key' is now '$value'.")
    }

    open fun setContext(name: String, values: Map<String, out Any>?) {
        log("Context $name")
        values?.forEach { entry ->
            setTag("$name.${entry.key}", entry.value.toString())
        }
    }

}

fun Station.toContextMap() = mapOf(
    "id" to id,
    "name" to title
)