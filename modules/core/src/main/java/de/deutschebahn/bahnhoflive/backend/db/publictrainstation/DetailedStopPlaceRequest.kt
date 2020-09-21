/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.db.publictrainstation

import android.location.Location
import com.android.volley.NetworkResponse
import com.android.volley.Response
import de.deutschebahn.bahnhoflive.backend.ForcedCacheEntryFactory
import de.deutschebahn.bahnhoflive.backend.GsonResponseParser
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.asVolleyError
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.DetailedStopPlace

class DetailedStopPlaceRequest(
    listener: VolleyRestListener<DetailedStopPlace>,
    private val stadaId: String,
    dbAuthorizationTool: DbAuthorizationTool,
    force: Boolean = false,
    private val currentPosition: Location? = null
) : PublicTrainStationRequest<DetailedStopPlace>(
    Method.GET,
    "stop-places/$stadaId",
    dbAuthorizationTool,
    listener
) {

    init {
        setShouldCache(!force)
    }

    override fun getCountKey() = "PTS/stop-places"

    override fun parseNetworkResponse(response: NetworkResponse?): Response<DetailedStopPlace> {
        super.parseNetworkResponse(response)

        return try {
            val parser = GsonResponseParser(DetailedStopPlace::class.java)
            val detailedStopPlace = parser.parseResponse(response)

            detailedStopPlace.fallbackStadaId = stadaId

            if (currentPosition != null) {
                detailedStopPlace.calculateDistance(
                    DistanceCalulator(
                        currentPosition.latitude,
                        currentPosition.longitude
                    )
                )
            }
            val forcedCacheEntryFactory =
                ForcedCacheEntryFactory(ForcedCacheEntryFactory.DAY_IN_MILLISECONDS)

            Response.success(detailedStopPlace, forcedCacheEntryFactory.createCacheEntry(response))
        } catch (e: Exception) {
            Response.error(e.asVolleyError())
        }
    }
}

