/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend

import android.util.Log
import com.android.volley.Request
import com.android.volley.VolleyError

abstract class BaseRequest<T>(
    method: Int,
    url: String?,
    private val restListener: VolleyRestListener<T>
) : Request<T>(method, url, RestErrorListener(restListener)) {

    init {
        Log.d("cr","request: " + url)
    }

    override fun parseNetworkError(volleyError: VolleyError): VolleyError {
        if(volleyError.message!=null)
            Log.d("cr","request: " + url +  " error (" + volleyError.message + ")")
        else
            Log.d("cr","request: " + url +  " error ()")

        return volleyError as? DetailedVolleyError
            ?: DetailedVolleyError(
                this,
                volleyError
            )
    }

    override fun deliverResponse(response: T) {
        Log.d("cr","request: " + url +  " OK")
        restListener.onSuccess(response)
    }

}