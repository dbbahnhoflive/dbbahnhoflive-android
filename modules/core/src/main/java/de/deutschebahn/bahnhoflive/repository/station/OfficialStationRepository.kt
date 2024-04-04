/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository.station

import android.location.Location
import android.util.Log
import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.RestHelper
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.ris.RISPlatformsRequest
import de.deutschebahn.bahnhoflive.backend.db.ris.RISStationsLocalServicesRequest
import de.deutschebahn.bahnhoflive.backend.db.ris.RISStationsStationRequest
import de.deutschebahn.bahnhoflive.backend.db.ris.RISStationsStopPlacesGroupRequest
import de.deutschebahn.bahnhoflive.backend.db.ris.RISStationsStopPlacesRequest
import de.deutschebahn.bahnhoflive.backend.db.ris.RISStationsStopPlacesRequestByEvaId
import de.deutschebahn.bahnhoflive.backend.db.ris.model.LocalServices
import de.deutschebahn.bahnhoflive.backend.db.ris.model.Platform
import de.deutschebahn.bahnhoflive.backend.db.ris.model.RISStation
import de.deutschebahn.bahnhoflive.backend.db.ris.model.StopPlace
import de.deutschebahn.bahnhoflive.repository.InternalStation
import de.deutschebahn.bahnhoflive.util.Cancellable
import de.deutschebahn.bahnhoflive.util.volley.VolleyRequestCancellable
import de.deutschebahn.bahnhoflive.util.volley.cancellable

class OfficialStationRepository(
    private val restHelper: RestHelper,
    private val dbAuthorizationTool: DbAuthorizationTool,
    private val clientIdDbAuthorizationTool: DbAuthorizationTool? = null
) : StationRepository() {

    private val trackingManager = TrackingManager()

    fun queryStationGroups(
        listener: VolleyRestListener<List<StopPlace>?>,
        evaId : String,
        stopPlace: List<StopPlace>,
        force: Boolean = false
    ) = restHelper
        .add(
            RISStationsStopPlacesGroupRequest(
                object :
                    VolleyRestListener<Array<String>> {
                    override fun onSuccess(payload: Array<String>) {

                        try {
                            stopPlace[0].evaIds.ids.let {itIds->
                                payload.forEach {
                                    if(!itIds.contains(it))
                                        itIds.add(it)
                                    Log.d("cr", "evaId: ${it}")
                                }
                            }
                        }
                        catch(_:Exception) {

                        }

                        listener.onSuccess(stopPlace)
                    }

                    override fun onFail(reason: VolleyError) {
                        listener.onFail(reason)
                    }

                },
                dbAuthorizationTool,
                evaId,
                force,
            )
        )
        .cancellable()

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

                        if(payload.isNotEmpty() && payload[0].isDbStation) {
//                            SEV_Static.addEvaIds(payload[0].stationID, payload[0].evaIds.ids)

                               payload[0].evaIds.main?.let {
                                   queryStationGroups(listener,
                                       it, payload)
                        }


                        }
                        else {
                        listener.onSuccess(payload)
                        track("success")
                        }
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