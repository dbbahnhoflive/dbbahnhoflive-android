package de.deutschebahn.bahnhoflive.backend.db.ris

import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.google.gson.Gson
import de.deutschebahn.bahnhoflive.backend.DetailedVolleyError
import de.deutschebahn.bahnhoflive.backend.ForcedCacheEntryFactory
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.ris.model.DepartureMatches
import java.io.ByteArrayInputStream
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

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
        val number: String?,
        val category: String?,
        val line: String? = null,
        val date: Long?
    ) {
        fun toUrlParameters() = listOfNotNull(
            number?.let { "number" to number },
            category?.let { "category" to category },
            line?.let { "line" to it },
            date?.let { "date" to SimpleDateFormat("yyyy-MM-dd", Locale.US).format(it) }
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
                ForcedCacheEntryFactory(TimeUnit.HOURS.toMillis(2).toInt()).createCacheEntry(
                    networkResponse
                )
            )
        }.getOrElse {
            Response.error(DetailedVolleyError(this, it))
        }
    }
}