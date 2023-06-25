package de.deutschebahn.bahnhoflive.backend.db.ris

import com.android.volley.DefaultRetryPolicy
import com.android.volley.RetryPolicy
import de.deutschebahn.bahnhoflive.BuildConfig
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.DbRequest

open class RISJourneysRequest<T>(
    urlSuffix: String,
    dbAuthorizationTool: DbAuthorizationTool,
    restListener: VolleyRestListener<T>
) : DbRequest<T>(
    Method.GET,
    "${BuildConfig.RIS_JOURNEYS_BASE_URL}$urlSuffix",
    dbAuthorizationTool,
    restListener,
    "db-api-key"
) {
    init {
        retryPolicy = DefaultRetryPolicy(30000, 1, 1.0f)
    }
    override fun getCountKey(): String = "ris-journeys"
}