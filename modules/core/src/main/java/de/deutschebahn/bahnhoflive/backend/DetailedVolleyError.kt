package de.deutschebahn.bahnhoflive.backend

import com.android.volley.Request
import com.android.volley.VolleyError

class DetailedVolleyError(
    val request: Request<*>,
    cause: Throwable?
) : VolleyError(
    (cause as? VolleyError)?.let {
        "Status code ${it.networkResponse.statusCode}: ${request.url}"
    } ?: "Failed: ${request.url}",
    cause)