/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui

import android.app.Application
import android.location.Location
import androidx.lifecycle.*
import de.deutschebahn.bahnhoflive.repository.StationResource
import de.deutschebahn.bahnhoflive.repository.StationResourceProvider
import de.deutschebahn.bahnhoflive.repository.stationsearch.NearbyStopPlacesResource
import de.deutschebahn.bahnhoflive.util.openhours.OpenHoursParser

open class StadaStationCacheViewModel(application: Application) : AndroidViewModel(application),
    StationResourceProvider {
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
        it.data
    }

    val openHoursParser by lazy {
        OpenHoursParser(application, viewModelScope)
    }

    override fun getStationResource(id: String): StationResource {
        return cache.getOrPut(id) { StationResource(openHoursParser, id) }
    }

}