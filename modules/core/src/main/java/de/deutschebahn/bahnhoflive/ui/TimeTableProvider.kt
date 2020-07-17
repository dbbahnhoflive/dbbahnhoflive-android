package de.deutschebahn.bahnhoflive.ui

import android.location.Location
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.StopPlace
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation
import de.deutschebahn.bahnhoflive.repository.DbTimetableResource
import de.deutschebahn.bahnhoflive.repository.LifecycleResourceClient
import de.deutschebahn.bahnhoflive.repository.Station
import de.deutschebahn.bahnhoflive.repository.StationResourceProvider
import kotlin.math.roundToInt

class TimeTableProvider {

    interface StationLookupResultListener {
        fun onDbTimeTableResourceAvailable(timeTableResoures: DbTimetableResource)
        fun onHafasStationsAvailable(hafasStations: List<HafasStation>)
        fun onFail(error: Exception)
    }

    fun stationLookupRequest(owner: LifecycleOwner, location: Location, stationResourceProvider: StationResourceProvider, origin: String, resultListener: StationLookupResultListener) =
        BaseApplication
                    .get()
            .repositories.stationRepository.queryStations(object :
            VolleyRestListener<List<StopPlace>> {

                private var resourceClient: LifecycleResourceClient<Station, VolleyError>? = null

                override fun onSuccess(payload: List<StopPlace>) {
                    payload.firstOrNull { it.isDbStation }?.asInternalStation?.let { internalStation ->
                        val stationResource =
                            stationResourceProvider.getStationResource(internalStation.id)

                        stationResource.refresh()

                        resourceClient?.releaseResource()

                        resourceClient = LifecycleResourceClient<Station, VolleyError>(owner,
                            Observer { station ->
                                val evaIds = station!!.evaIds
                                requestHafasStations(
                                    location,
                                    evaIds.ids,
                                    origin,
                                    resultListener,
                                    payload
                                )

                                resultListener.onDbTimeTableResourceAvailable(
                                    DbTimetableResource(
                                        internalStation
                                    )
                                )
                            }, null,
                            Observer { volleyError ->
                                if (volleyError != null) {
                                    requestHafasStations(
                                        location,
                                        null,
                                        origin,
                                        resultListener,
                                        payload
                                    )

                                    resultListener.onDbTimeTableResourceAvailable(
                                        DbTimetableResource(internalStation)
                                    )
                                }
                            }).apply {
                            observe(stationResource)
                        }

                    } ?: requestHafasStations(location, null, origin, resultListener, payload)

                }

                override fun onFail(reason: VolleyError) {
                    resultListener.onFail(reason)
                }
            }, location = location, mixedResults = true, limit = 100)

    private fun requestHafasStations(
        location: Location,
        evaIds: List<String>?,
        origin: String,
        resultListener: StationLookupResultListener,
        stopPlaces: List<StopPlace>
    ) {
        resultListener.onHafasStationsAvailable(
            stopPlaces.mapNotNull {
                it.takeIf { it.isLocalTransportStation }?.let { stopPlace ->
                    HafasStation().also { hafasStation ->
                        hafasStation.name = stopPlace.name
                        hafasStation.extId = stopPlace.evaId
                        stopPlace.location?.let {
                            hafasStation.longitude = it.longitude
                            hafasStation.latitude = it.latitude
                        }
                        hafasStation.dist = (stopPlace.distanceInKm * 1000).roundToInt()
                    }

                }
            }
        )
    }

}
