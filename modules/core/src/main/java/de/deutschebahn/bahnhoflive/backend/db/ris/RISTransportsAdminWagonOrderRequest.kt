package de.deutschebahn.bahnhoflive.backend.db.ris

import com.android.volley.Cache
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkResponse
import com.android.volley.Response
import de.deutschebahn.bahnhoflive.BuildConfig
import de.deutschebahn.bahnhoflive.backend.DetailedVolleyError
import de.deutschebahn.bahnhoflive.backend.ForcedCacheEntryFactory
import de.deutschebahn.bahnhoflive.backend.GsonResponseParser
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.DbRequest
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.RisAdminWagonOrders
import java.util.concurrent.TimeUnit

// https://apis.deutschebahn.com/db/apis/ris-transports/v3/vehicle-sequences/administrations

open class RISTransportsAdminWagonOrderRequestCoreRequest<T>(
    urlSuffix: String,
    dbAuthorizationTool: DbAuthorizationTool,
    restListener: VolleyRestListener<T>
) : DbRequest<T>(
    Method.GET,
    "${BuildConfig.RIS_TRANSPORTS_BASE_URL}$urlSuffix",
    dbAuthorizationTool,
    restListener,
    "db-api-key"
) {
    init {
        retryPolicy = DefaultRetryPolicy(30000, 1, 1.0f)
        setShouldCache(true)
    }
    override fun getCountKey(): String = "ris-transports_vehicle_sequences"
}

class RISTransportsAdminWagonOrderRequest(
    restListener: VolleyRestListener<RisAdminWagonOrders>,
    dbAuthorizationTool: DbAuthorizationTool
) :
    RISTransportsAdminWagonOrderRequestCoreRequest<RisAdminWagonOrders>(
        "vehicle-sequences/administrations",
        dbAuthorizationTool,
        restListener
    ) {

    override fun parseNetworkResponse(response: NetworkResponse): Response<RisAdminWagonOrders> {
        super.parseNetworkResponse(response)

        return kotlin.runCatching {

            val data = GsonResponseParser(RisAdminWagonOrders::class.java).parseResponse(response)

            val cacheEntry: Cache.Entry = ForcedCacheEntryFactory(
                TimeUnit.HOURS.toMillis(24).toInt()
            ).createCacheEntry(response)

            Response.success(data, cacheEntry)

        }
            .getOrElse {
                Response.error(DetailedVolleyError(this, it))
            }

    }

}
