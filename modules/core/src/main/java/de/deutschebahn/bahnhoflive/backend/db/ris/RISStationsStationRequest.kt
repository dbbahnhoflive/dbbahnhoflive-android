package de.deutschebahn.bahnhoflive.backend.db.ris

import com.android.volley.NetworkResponse
import com.android.volley.Response
import de.deutschebahn.bahnhoflive.backend.GsonResponseParser
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.ris.model.RISStation
import de.deutschebahn.bahnhoflive.backend.parse

class RISStationsStationRequest(
    val stadaId: String,
    restListener: VolleyRestListener<RISStation>,
    dbAuthorizationTool: DbAuthorizationTool
) :
    RISStationsRequest<RISStation>(
        "/stations/$stadaId",
        dbAuthorizationTool,
        restListener

    ) {

    override fun parseNetworkResponse(response: NetworkResponse): Response<RISStation>? {
        super.parseNetworkResponse(response)

        return parse(response) {
            GsonResponseParser(RISStation::class.java).parseResponse(it)
        }
    }

}