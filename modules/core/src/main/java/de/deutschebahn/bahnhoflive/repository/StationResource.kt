/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.repository

import androidx.lifecycle.Observer
import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.backend.rimap.model.StationFeatureCollection
import de.deutschebahn.bahnhoflive.repository.DetailedStopPlaceStationWrapper.Companion.of

class StationResource @JvmOverloads constructor(
    private val detailedStopPlaceResource: DetailedStopPlaceResource = DetailedStopPlaceResource(),
    private val rimapStationFeatureCollectionResource: RimapStationFeatureCollectionResource = RimapStationFeatureCollectionResource()
) : MediatorResource<MergedStation>() {
    private val rimapErrorObserver = Observer<VolleyError?> { volleyError ->
        val stationFeatureCollection = rimapStationFeatureCollectionResource.data.value
        if (volleyError == null && (stationFeatureCollection == null || stationFeatureCollection.features.isNotEmpty())) {
            clearRimapError()
        }
    }
    private val rimapDataObserver =
        Observer<StationFeatureCollection?> { stationFeatureCollection ->
            RimapStationWrapper.wrap(stationFeatureCollection).run {
                data.value = orCurrent().copy(rimapStationWrapper = this)
            }
            loadingStatus.value = LoadingStatus.IDLE
        }

    constructor(id: String?) : this() {
        detailedStopPlaceResource.initialize(id)
        rimapStationFeatureCollectionResource.initialize(id)
    }

    private fun clearStadaError() {
        data.removeSource(rimapStationFeatureCollectionResource.mutableData)
        data.removeSource(rimapStationFeatureCollectionResource.mutableError)
        clearRimapError()
    }

    private fun clearRimapError() {
        error.value = null
        loadingStatus.value = LoadingStatus.IDLE
    }

    override fun onRefresh(): Boolean {
        val loading = detailedStopPlaceResource.loadIfNecessary()
        if (loading) {
            loadingStatus.value = LoadingStatus.BUSY
        }
        return loading
    }

    fun initialize(station: Station?) {
        if (station != null) {
            data.value = station.orCurrent().copy(fallbackStation = station)
            detailedStopPlaceResource.initialize(station)
            rimapStationFeatureCollectionResource.initialize(station)
        }
    }

    private fun Station.orCurrent() = data.value ?: MergedStation(this)

    init {
        data.addSource(detailedStopPlaceResource.data) { detailedStopPlace ->
            of(detailedStopPlace)?.run {
                data.value = orCurrent().copy(detailedStopPlaceStationWrapper = this)
            }
            loadingStatus.value = LoadingStatus.IDLE

        }
        data.addSource(detailedStopPlaceResource.error) { volleyError ->
            if (volleyError == null) {
                clearStadaError()
            } else {
                rimapStationFeatureCollectionResource.loadIfNecessary()
                data.addSource(rimapStationFeatureCollectionResource.mutableData, rimapDataObserver)
                data.addSource(
                    rimapStationFeatureCollectionResource.mutableError, rimapErrorObserver
                )
            }
        }
    }
}