/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.repository

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.backend.rimap.model.RimapPOI
import de.deutschebahn.bahnhoflive.ui.station.shop.CategorizedShops

class ShopsResource constructor(
    override val data: MediatorLiveData<CategorizedShops?> = MediatorLiveData<CategorizedShops?>(),
    override val loadingStatus: MediatorLiveData<LoadingStatus> = MediatorLiveData<LoadingStatus>(),
    override val error: MediatorLiveData<VolleyError?> = MediatorLiveData<VolleyError?>()
) : Resource<CategorizedShops?, VolleyError?>(
    data, loadingStatus, error
) {
    private val loadingStatusForwarder: Observer<LoadingStatus> =
        Observer { loadingStatus -> this@ShopsResource.loadingStatus.value = loadingStatus }
    private val errorForwarder = Observer<VolleyError?> { throwable -> error.value = throwable }
    val rimapPOIListResource = RimapPOIListResource()
    private val einkaufsbahnhofStationResponseResource = EinkaufsbahnhofStationResponseResource()
    private var skipRimap = false
    private var skipEinkaufsbahnhof = false
    fun initialize(station: Station) {
        rimapPOIListResource.initialize(station)
        einkaufsbahnhofStationResponseResource.initialize(station.id)
        rimapPOIListResource.load()
    }

    override fun onRefresh(): Boolean {
        if (skipEinkaufsbahnhof) {
            super.onRefresh()
        } else if (skipRimap) {
            einkaufsbahnhofStationResponseResource.refresh()
        } else {
            rimapPOIListResource.refresh()
        }
        return false
    }

    init {
        loadingStatus.addSource(rimapPOIListResource.loadingStatus, loadingStatusForwarder)
        loadingStatus.addSource(
            einkaufsbahnhofStationResponseResource.loadingStatus,
            loadingStatusForwarder
        )
        error.addSource(einkaufsbahnhofStationResponseResource.error, errorForwarder)
        error.addSource(rimapPOIListResource.error) { volleyError ->
            if (volleyError == null) {
                error.value = volleyError
            }
        }
        data.addSource(rimapPOIListResource.error) { volleyError ->
            if (volleyError != null) {
                einkaufsbahnhofStationResponseResource.refresh()
            }
        }
        data.addSource(rimapPOIListResource.data) { rimapPOIs: List<RimapPOI?>? ->
            if (rimapPOIs == null || rimapPOIs.isEmpty()) {
                skipRimap = true
                einkaufsbahnhofStationResponseResource.load()
                return@addSource
            }
            val categorizedShops = CategorizedShops(rimapPOIs)
            data.setValue(categorizedShops)
        }
        data.addSource(einkaufsbahnhofStationResponseResource.data) { stationResponse ->
            if (stationResponse?.stores == null || stationResponse.stores.isEmpty()) {
                skipEinkaufsbahnhof = true
                if (data.value == null) {
                    data.value = null // trigger value update without overwriting existing data
                }
                return@addSource
            }
            val categorizedShops = CategorizedShops(stationResponse)
            data.setValue(categorizedShops)
        }
    }
}