package de.deutschebahn.bahnhoflive.backend.db.ris

import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.google.gson.Gson
import de.deutschebahn.bahnhoflive.backend.DetailedVolleyError
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.ris.model.JourneyEventBased
import java.io.ByteArrayInputStream

class RISJourneysEventbasedRequest(
    journeyID: String,
    dbAuthorizationTool: DbAuthorizationTool,
    restListener: VolleyRestListener<JourneyEventBased>
) : RISJourneysRequest<JourneyEventBased>(
    "$journeyID",
    dbAuthorizationTool,
    restListener
) {
// RIS-API kostet Geld !!!!!!!!!!!

    init {
        // default-timeout is 2500ms, backoffMult:1.0
        retryPolicy = DefaultRetryPolicy(20 * 1000, 1, 1.0f)
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<JourneyEventBased> {
        super.parseNetworkResponse(response)

        return kotlin.runCatching {
            val departureMatches = Gson().fromJson(
                ByteArrayInputStream(response.data).reader(),
                JourneyEventBased::class.java
            )

            Response.success(
                departureMatches,
                HttpHeaderParser.parseCacheHeaders(response)
            )
        }.getOrElse {
            Response.error(DetailedVolleyError(this, it))
        }
    }


}