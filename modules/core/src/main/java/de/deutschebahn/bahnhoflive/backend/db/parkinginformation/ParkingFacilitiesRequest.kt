/*
 * SPDX-FileCopyrightText: 2022 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.db.parkinginformation

import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import de.deutschebahn.bahnhoflive.backend.DetailedVolleyError
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.model.parking.ParkingFacility
import org.json.JSONObject

class ParkingFacilitiesRequest(
    stopPlaceId: String,
    listener: VolleyRestListener<List<ParkingFacility>>,
    dbAuthorizationTool: DbAuthorizationTool
) : ParkingInformationRequest<List<ParkingFacility>>(
    Method.GET,
    "parking-facilities?stopPlaceId=$stopPlaceId&withPassengerRelevance=true",
    listener,
    dbAuthorizationTool
) {


    override fun parseNetworkResponse(response: NetworkResponse): Response<List<ParkingFacility>>? {
        super.parseNetworkResponse(response)

        return response?.let { networkResponse ->
            try {
                JSONParkingFacilityConverter()
                    .parse(JSONObject(String(networkResponse.data)))?.let {
                        Response.success(it, HttpHeaderParser.parseCacheHeaders(networkResponse))
                    }
            } catch (e: Exception) {
                Response.error<List<ParkingFacility>>(DetailedVolleyError(this, e))
            }
        } ?: Response.error<List<ParkingFacility>>(
            DetailedVolleyError(
                this,
                Exception("Response empty")
            )
        )
    }


}
