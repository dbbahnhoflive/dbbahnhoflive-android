package de.deutschebahn.bahnhoflive.backend.db.ris

import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.DbRequest

abstract class RISStationsRequest<T>(
    urlSuffix: String,
    apiKeyDbAuthorizationTool: DbAuthorizationTool,
    restListener: VolleyRestListener<T>
) :
    DbRequest<T>(
        Method.GET,
        "https://apis.deutschebahn.com/db/apis/ris-stations/v1/$urlSuffix",
        apiKeyDbAuthorizationTool,
        restListener,
        "db-api-key"
    ) {

    override fun getCountKey(): String? = null

}