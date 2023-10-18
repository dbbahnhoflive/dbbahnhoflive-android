package de.deutschebahn.bahnhoflive.backend.db.ris

import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser
import com.google.gson.Gson
import de.deutschebahn.bahnhoflive.backend.DetailedVolleyError
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.ris.model.JourneyEventBased
import de.deutschebahn.bahnhoflive.util.DebugX
import de.deutschebahn.bahnhoflive.util.DebugX.Companion.logVolleyResponseError
import java.io.ByteArrayInputStream

class RISJourneysEventbasedRequest(
    journeyID: String,
    dbAuthorizationTool: DbAuthorizationTool,
    restListener: VolleyRestListener<JourneyEventBased>
) : RISJourneysRequest<JourneyEventBased>(
    "eventbased/$journeyID",
    dbAuthorizationTool,
    restListener
) {
// RIS-API kostet Geld !!!!!!!!!!!

    init {
        // default-timeout is 2500ms, backoffMult:1.0
        retryPolicy = DefaultRetryPolicy(20 * 1000, 1, 1.0f)
    }

    override fun parseNetworkResponse(networkResponse: NetworkResponse): Response<JourneyEventBased> {
        super.parseNetworkResponse(networkResponse)

        return kotlin.runCatching {
            val departureMatches = Gson().fromJson(
                ByteArrayInputStream(networkResponse.data).reader(),
                JourneyEventBased::class.java
            )


            DebugX.logVolleyResponseOk(this,url)

            Response.success(
                departureMatches,
                HttpHeaderParser.parseCacheHeaders(networkResponse)
            )
        }.getOrElse {
            Response.error(DetailedVolleyError(this, it))
        }
    }

    override fun parseNetworkError(volleyError: VolleyError): VolleyError {
        logVolleyResponseError(this, url, volleyError)
        return super.parseNetworkError(volleyError!!)
    }

}