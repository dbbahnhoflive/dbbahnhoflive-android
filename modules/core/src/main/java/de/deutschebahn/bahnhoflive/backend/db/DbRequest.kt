/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.backend.db

import androidx.annotation.CallSuper
import com.android.volley.AuthFailureError
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.analytics.TaloTracing.putTraceHeader
import de.deutschebahn.bahnhoflive.analytics.TaloTracing.updateTraceIdFromResponse
import de.deutschebahn.bahnhoflive.backend.BaseRequest
import de.deutschebahn.bahnhoflive.backend.Countable
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.util.asMutable

abstract class DbRequest<T>(
    method: Int,
    url: String?,
    private val dbAuthorizationTool: DbAuthorizationTool?,
    restListener: VolleyRestListener<T>,
    private val authorizationHeaderKey: String = "key"
) : BaseRequest<T>(method, url, restListener), Countable {
    @Throws(AuthFailureError::class)
    override fun getHeaders(): Map<String, String> {
        return putTraceHeader(
            dbAuthorizationTool?.putAuthorizationHeader(
                super.getHeaders(), authorizationHeaderKey
            ).asMutable()
        )
    }

    override fun parseNetworkError(volleyError: VolleyError): VolleyError {
        updateTraceIdFromResponse(volleyError.networkResponse)
        return super.parseNetworkError(volleyError)
    }

    @CallSuper
    override fun parseNetworkResponse(response: NetworkResponse): Response<T>? {
        updateTraceIdFromResponse(response)
        return null
    }
}