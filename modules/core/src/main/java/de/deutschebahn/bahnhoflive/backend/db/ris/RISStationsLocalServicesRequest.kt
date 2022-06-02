package de.deutschebahn.bahnhoflive.backend.db.ris

import com.android.volley.NetworkResponse
import com.android.volley.Response
import de.deutschebahn.bahnhoflive.backend.GsonResponseParser
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.ris.model.LocalServices
import de.deutschebahn.bahnhoflive.backend.parse

class RISStationsLocalServicesRequest(
    val stadaId: String,
    restListener: VolleyRestListener<LocalServices>,
    dbAuthorizationTool: DbAuthorizationTool,
    clientIdDbAuthorizationTool: DbAuthorizationTool?
) :
    RISStationsRequest<LocalServices>(
        "local-services/by-key?keyType=STATION_ID&key=$stadaId",
        dbAuthorizationTool,
        restListener
    ) {

    override fun parseNetworkResponse(response: NetworkResponse): Response<LocalServices>? {
        super.parseNetworkResponse(response)

        return parse(response) {
            GsonResponseParser(LocalServices::class.java).parseResponse(it)
        }
    }

}