/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository.station

import android.location.Location
import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.RestHelper
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.ris.*
import de.deutschebahn.bahnhoflive.backend.db.ris.model.LocalServices
import de.deutschebahn.bahnhoflive.backend.db.ris.model.Platform
import de.deutschebahn.bahnhoflive.backend.db.ris.model.RISStation
import de.deutschebahn.bahnhoflive.backend.db.ris.model.StopPlace
import de.deutschebahn.bahnhoflive.repository.InternalStation
import de.deutschebahn.bahnhoflive.ui.station.railreplacement.SEV_Static
import de.deutschebahn.bahnhoflive.util.Cancellable
import de.deutschebahn.bahnhoflive.util.volley.VolleyRequestCancellable
import de.deutschebahn.bahnhoflive.util.volley.cancellable

class OfficialStationRepository(
    private val restHelper: RestHelper,
    private val dbAuthorizationTool: DbAuthorizationTool,
    private val clientIdDbAuthorizationTool: DbAuthorizationTool? = null
) : StationRepository() {

    private val trackingManager = TrackingManager()

    override fun queryStations(
        listener: VolleyRestListener<List<StopPlace>?>,
        query: String?,
        location: Location?,
        force: Boolean,
        limit: Int,
        radius: Int,
        mixedResults: Boolean,
        collapseNeighbours: Boolean,
        pullUpFirstDbStation: Boolean
    ) = restHelper
        .add(
            RISStationsStopPlacesRequest(
                object :
                    VolleyRestListener<List<StopPlace>> {
                    override fun onSuccess(payload: List<StopPlace>) {

                        if(payload.isNotEmpty()) {
                            SEV_Static.addEvaIds(payload[0].stationID, payload[0].evaIds.ids)
                        }

                        listener.onSuccess(payload)

                        track("success")
                    }

                    override fun onFail(reason: VolleyError) {
                        listener.onFail(reason)

                        track("failure")
                    }

                    private fun track(result: String) {
                        when {
                            !query.isNullOrEmpty() -> "text"
                            location != null -> "geo"
                            else -> null
                        }?.also { type ->
                            trackingManager.track(
                                TrackingManager.TYPE_ACTION, "pts_request", type, result
                            )
                        }
                    }
                },
                dbAuthorizationTool,
                query,
                location,
                force,
                limit,
                radius,
                mixedResults,
                collapseNeighbours,
                pullUpFirstDbStation
            )
        )
        .cancellable()

    override fun queryLocalServices(
        listener: VolleyRestListener<LocalServices>,
        stadaId: String,
        force: Boolean,
        currentPosition: Location?
    ): Cancellable =
        restHelper.add(
            RISStationsLocalServicesRequest(
                stadaId,
                listener,
                dbAuthorizationTool
            )
        ).cancellable()


    override fun queryStation(
        listener: VolleyRestListener<RISStation>,
        stadaId: String,
        force: Boolean,
        currentPosition: Location?
    ) =
        restHelper.add(
            RISStationsStationRequest(
                stadaId, listener, dbAuthorizationTool
            )
        ).cancellable()

    override fun queryStationByEvaId(
        listener: VolleyRestListener<InternalStation?>,
        evaId: String
    ) = restHelper
        .add(
            RISStationsStopPlacesRequestByEvaId(
                object :
                    VolleyRestListener<InternalStation?> {
                    override fun onSuccess(payload: InternalStation?) {
                        listener.onSuccess(payload)
                    }

                    override fun onFail(reason: VolleyError) {
                        listener.onFail(reason)
                    }

                },
                dbAuthorizationTool,
                evaId
            )
        )
        .cancellable()


    override fun queryAccessibilityDetails(
        listener: VolleyRestListener<List<Platform>>,
        stadaId: String,
        force: Boolean
    ): VolleyRequestCancellable<List<Platform>> = restHelper
        .add(
            RISPlatformsRequest(
                listener, dbAuthorizationTool, stadaId, force
            )
        )
        .cancellable()


}