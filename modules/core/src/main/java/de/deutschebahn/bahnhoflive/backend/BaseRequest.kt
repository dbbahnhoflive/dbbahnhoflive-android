/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend

import com.android.volley.Request
import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.util.DebugX

abstract class BaseRequest<T>(
    method: Int,
    url: String?,
    private val restListener: VolleyRestListener<T>
) : Request<T>(method, url, RestErrorListener(restListener)) {

    init {
        DebugX.logVolleyRequest(this,url)
    }

    override fun parseNetworkError(volleyError: VolleyError): VolleyError {

        DebugX.logVolleyResponseError(
            this,
            url,
            volleyError
        )

        return volleyError as? DetailedVolleyError
            ?: DetailedVolleyError(
                this,
                volleyError
            )
    }

    override fun deliverResponse(response: T) {
        DebugX.logVolleyResponseOk(this,url)
        restListener.onSuccess(response)
    }

}