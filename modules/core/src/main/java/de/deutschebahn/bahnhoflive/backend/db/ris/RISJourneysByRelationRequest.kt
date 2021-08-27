package de.deutschebahn.bahnhoflive.backend.db.ris

import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.google.gson.Gson
import de.deutschebahn.bahnhoflive.backend.DetailedVolleyError
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.ris.model.DepartureMatches
import java.io.ByteArrayInputStream
import java.net.URLEncoder

class RISJourneysByRelationRequest(
    parameters: Parameters,
    dbAuthorizationTool: DbAuthorizationTool,
    restListener: VolleyRestListener<DepartureMatches>
) : RISJourneysRequest<DepartureMatches>(
    "byrelation?${parameters.toUrlParameters()}",
    dbAuthorizationTool,
    restListener
) {

    class Parameters(
        val number: String,
        val category: String,
        val line: String? = null
    ) {
        fun toUrlParameters() = listOfNotNull(
            "number" to number,
            "category" to category,
            line?.let { "line" to it }
        ).joinToString("&") { (key, value) ->
            "$key=${URLEncoder.encode(value, Charsets.UTF_8.name())}"
        }
    }

    override fun parseNetworkResponse(networkResponse: NetworkResponse): Response<DepartureMatches> {
        super.parseNetworkResponse(networkResponse)

        return kotlin.runCatching {
            val departureMatches = Gson().fromJson(
                ByteArrayInputStream(networkResponse.data).reader(),
                DepartureMatches::class.java
            )

            Response.success(
                departureMatches,
                HttpHeaderParser.parseCacheHeaders(networkResponse)
            )
        }.getOrElse {
            Response.error(DetailedVolleyError(this, it))
        }
    }
}