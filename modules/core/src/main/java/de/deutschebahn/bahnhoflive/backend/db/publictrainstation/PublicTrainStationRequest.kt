package de.deutschebahn.bahnhoflive.backend.db.publictrainstation

import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.DbRequest

abstract class PublicTrainStationRequest<T>(
    method: Int,
    urlSuffix: String,
    dbAuthorizationTool: DbAuthorizationTool,
    listener: VolleyRestListener<T>
) : DbRequest<T>(
        method,
        BASE_URL + urlSuffix,
    dbAuthorizationTool,
        listener
) {
    companion object {
        const val BASE_URL =
            "https://gateway.businesshub.deutschebahn.com/public-transport-stations/v1/"
    }
}