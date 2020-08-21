package de.deutschebahn.bahnhoflive.ui.hub

import android.location.Location
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasTimetable
import de.deutschebahn.bahnhoflive.repository.DbTimetableResource
import de.deutschebahn.bahnhoflive.repository.stationsearch.NearbyStopPlacesResource
import de.deutschebahn.bahnhoflive.ui.StadaStationCacheViewModel

class HubViewModel : StadaStationCacheViewModel() {

    val hafasData: ArrayList<HafasTimetable> = ArrayList()

    fun buildhafasData(hafasStations : List<HafasStation>) {
       wrapTimetables(hafasStations)
    }

    private fun wrapTimetables(stations: List<HafasStation>) {
        hafasData.clear()
        hafasData.ensureCapacity(stations.size)

        for (station in stations) {
            hafasData.add(HafasTimetable(station))
        }

    }

    val locationLiveData = MutableLiveData<Location>()

    val smoothedLocationLiveData = MediatorLiveData<Location>().apply {
        addSource(locationLiveData) { location ->
            if (location == null) {
                return@addSource
            }

            val currentValue = value

            if (currentValue == null || currentValue.distanceTo(location) > 500) {
                value = location
            }
        }
    }

    val nearbyStopPlacesResourceLiveData =
        NearbyStopPlacesResource(true).let { nearbyStopPlacesResource ->
            smoothedLocationLiveData.map {
                nearbyStopPlacesResource.apply {
                    location = it
                }
            }
        }

    val nearbyStopPlacesLiveData = nearbyStopPlacesResourceLiveData.switchMap {
        it?.data
    }

    val dbTimeTableResourcesLiveData = mutableMapOf<String, DbTimetableResource>().let { cache ->
        nearbyStopPlacesLiveData.map { stopPlaces ->
            stopPlaces.mapNotNull { stopPlace ->
                stopPlace.stadaId?.let { id ->
                    cache.getOrPut(id) {
                        DbTimetableResource(stopPlace.asInternalStation)
                            .apply {
                                refresh()
                            }
                    }
                }
            }
        }
    }


    val nearbyStopPlacesWithTimetableResourcesLiveData =
        dbTimeTableResourcesLiveData.switchMap { dbTimeTableResources ->
            nearbyStopPlacesLiveData.map { stopPlaces ->
                stopPlaces to dbTimeTableResources
            }
        }
}
