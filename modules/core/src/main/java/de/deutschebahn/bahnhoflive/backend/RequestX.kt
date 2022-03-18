package de.deutschebahn.bahnhoflive.backend

import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response

fun <T> Request<T>.parse(
    response: NetworkResponse,
    parser: (networkResponse: NetworkResponse) -> T
): Response<T> = runCatching {
    parser(response)
}.fold(
    {
        val forcedCacheEntryFactory =
            ForcedCacheEntryFactory(ForcedCacheEntryFactory.DAY_IN_MILLISECONDS)

        Response.success(it, forcedCacheEntryFactory.createCacheEntry(response))
    },
    {
        Response.error(DetailedVolleyError(this, it))
    }
)