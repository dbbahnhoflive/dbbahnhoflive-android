/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.db.ris

import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.backend.GsonResponseParser
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.ris.model.StopPlaces
import de.deutschebahn.bahnhoflive.repository.InternalStation
import java.net.URLEncoder

// doku
// https://developers.deutschebahn.com/db-api-marketplace/apis/product/ris-stations/api/ris-stations#/RISStations_1150/operation/%2Fstop-places%2F{evaNumber}/get

class RISStationsStopPlacesRequestByEvaId(
    listener: VolleyRestListener<InternalStation?>,
    dbAuthorizationTool: DbAuthorizationTool,
    evaId: String
) : RISStationsRequest<InternalStation?>( //         "https://apis.deutschebahn.com/db/apis/ris-stations/v1/$urlSuffix",
    "stop-places/by-key?keyType=EVA&key=" + URLEncoder.encode(evaId, "UTF-8") ,
//    "stop-places/" + URLEncoder.encode(evaId, "UTF-8") + "/groups",
    dbAuthorizationTool,
    listener
) {

    init {
        setShouldCache(false)
        retryPolicy = DefaultRetryPolicy(35000,1,1.0f)
    }

    override fun getCountKey() = "RIS/stations/stop-places"

    override fun parseNetworkResponse(response: NetworkResponse): Response<InternalStation?> {
        super.parseNetworkResponse(response)

        return try {

            val parser = GsonResponseParser(StopPlaces::class.java)
            val parserResponse = parser.parseResponse(response)
            val station = parserResponse?.stopPlaces?.get(0)?.asInternalStation

            // wenn stationID fehlt (z,B.: OEPNV-Bushaltestelle) => station=null
            Response.success(station, null)
        } catch (e: Exception) {
            Response.error(VolleyError(e))
        }
    }

}