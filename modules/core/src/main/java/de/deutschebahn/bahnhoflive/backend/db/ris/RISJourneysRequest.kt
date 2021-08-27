package de.deutschebahn.bahnhoflive.backend.db.ris

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
    restListener
) {
    override fun getCountKey(): String = "ris-journeys"
}