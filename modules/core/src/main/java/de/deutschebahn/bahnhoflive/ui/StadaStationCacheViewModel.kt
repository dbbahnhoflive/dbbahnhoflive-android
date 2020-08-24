package de.deutschebahn.bahnhoflive.ui

import android.location.Location
import androidx.lifecycle.*
import de.deutschebahn.bahnhoflive.repository.StationResource
import de.deutschebahn.bahnhoflive.repository.StationResourceProvider
import de.deutschebahn.bahnhoflive.repository.stationsearch.NearbyStopPlacesResource
import java.util.*

open class StadaStationCacheViewModel : ViewModel(), StationResourceProvider {
    private val cache: MutableMap<String, StationResource> = HashMap()
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

    override fun getStationResource(id: String): StationResource {
        return cache.getOrPut(id) { StationResource(id) }
    }

}