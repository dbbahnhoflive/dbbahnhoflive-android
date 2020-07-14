package de.deutschebahn.bahnhoflive.ui

import android.location.Location
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.backend.BaseRestListener
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.StopPlace
import de.deutschebahn.bahnhoflive.backend.hafas.LocalTransportFilter
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation
import de.deutschebahn.bahnhoflive.backend.hafas.model.ProductCategory
import de.deutschebahn.bahnhoflive.repository.DbTimetableResource
import de.deutschebahn.bahnhoflive.repository.LifecycleResourceClient
import de.deutschebahn.bahnhoflive.repository.Station
import de.deutschebahn.bahnhoflive.repository.StationResourceProvider

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
                    payload.firstOrNull()?.asInternalStation?.let { internalStation ->
                        val stationResource = stationResourceProvider.getStationResource(internalStation.id)

                        stationResource.refresh()

                        if (resourceClient != null) {
                            resourceClient!!.releaseResource()
                        }

                        resourceClient = LifecycleResourceClient(owner,
                                Observer { station ->
                                    val evaIds = station!!.evaIds
                                    requestHafasStations(location, evaIds.ids, origin, resultListener)

                                    resultListener.onDbTimeTableResourceAvailable(DbTimetableResource(internalStation))
                                }, null,
                                Observer { volleyError ->
                                    if (volleyError != null) {
                                        requestHafasStations(location, null, origin, resultListener)
                                        resultListener.onDbTimeTableResourceAvailable(
                                            DbTimetableResource(internalStation)
                                        )
                                    }
                                })
                        resourceClient!!.observe(stationResource)
                    } ?: requestHafasStations(location, null, origin, resultListener)
                }

                override fun onFail(reason: VolleyError) {
                    requestHafasStations(location, null, origin, resultListener)
                }
            }, location = location, mixedResults = false)

    private fun requestHafasStations(
        location: Location,
        evaIds: List<String>?,
        origin: String,
        resultListener: StationLookupResultListener
    ) {
        BaseApplication.get().repositories
            .localTransportRepository.queryNearbyStations(
            location.latitude,
            location.longitude,
            LocalTransportFilter(20, evaIds, ProductCategory.BITMASK_LOCAL_TRANSPORT),
            object : BaseRestListener<List<HafasStation>>() {
                override fun onSuccess(payload: List<HafasStation>) {
                    resultListener.onHafasStationsAvailable(payload)
                }

                override fun onFail(reason: VolleyError) {
                    super.onFail(reason)
                    resultListener.onFail(reason)
                }

            }, origin, 2000
        )
    }

}
