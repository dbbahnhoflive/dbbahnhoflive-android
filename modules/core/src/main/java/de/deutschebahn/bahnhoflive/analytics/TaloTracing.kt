/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.analytics

import com.android.volley.NetworkResponse
import de.deutschebahn.bahnhoflive.BaseApplication

object TaloTracing {

    const val HEADER_KEY = "talo-tracing-traceId"

    private val observers = mutableSetOf<Listener>()

    @Volatile
    var traceId: String? = null
        set(value) {
            if (field != null) {
                if (field != value) {
                    BaseApplication.get().issueTracker.log("Dropping talo traceId $value")
                }
                return
            }

            field = value

            observers.forEach { observer ->
                observer(field)
            }
        }

    fun updateTraceIdFromResponse(networkResponse: NetworkResponse?) {
        networkResponse?.headers?.get(HEADER_KEY)?.let {
            traceId = it
        }
    }

    fun putTraceHeader(headers: MutableMap<String, String>) = headers.apply {
        traceId?.also {
            headers[HEADER_KEY] = it
        }
    }

    fun addTraceIdListener(listener: Listener) {
        observers += listener

        traceId?.let {
            listener(it)
        }
    }

}

typealias Listener = ((String?) -> Unit)
