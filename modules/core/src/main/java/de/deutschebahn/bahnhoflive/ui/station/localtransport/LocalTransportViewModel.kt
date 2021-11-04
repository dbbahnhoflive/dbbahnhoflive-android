/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.localtransport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation
import de.deutschebahn.bahnhoflive.repository.HafasStationsResource
import de.deutschebahn.bahnhoflive.repository.MediatorResource
import de.deutschebahn.bahnhoflive.repository.MergedStation
import de.deutschebahn.bahnhoflive.repository.StationResource
import de.deutschebahn.bahnhoflive.util.ManagedObserver
import de.deutschebahn.bahnhoflive.util.Token

class LocalTransportViewModel : ViewModel() {

    val MAX_NEARBY_DEPARTURES_DISTANCE = 250 // BAHNHOFLIVE-1311: radius 250m

    val hafasStationsResource = HafasStationsResource(MAX_NEARBY_DEPARTURES_DISTANCE)

    val hafasStationsAvailableLiveData = hafasStationsResource.data.map { hafasStations ->
        hafasStations?.isNotEmpty()
    }

    private val mediatorResource = object : MediatorResource<List<HafasStation>>() {
        override fun onRefresh(): Boolean {
            return hafasStationsResource.refresh() || super.onRefresh()
        }
    }

    private var hafasStationsObserver: ManagedObserver<List<HafasStation>>? = null
    private var stationObserver: ManagedObserver<MergedStation>? = null
    private var stationResource: StationResource? = null

    private val initializationPending = Token()


    /**
     * Initialization from [de.deutschebahn.bahnhoflive.ui.station.StationActivity]
     */
    fun initialize(stationResource: StationResource) {
        if (initializationPending.take()) {
            this.stationResource = stationResource

            mediatorResource.addErrorSource(hafasStationsResource)
            mediatorResource.addLoadingStatusSource(hafasStationsResource)

            mediatorResource.addErrorSource(stationResource)
            mediatorResource.addLoadingStatusSource(stationResource)

            stationObserver = object : ManagedObserver<MergedStation>(stationResource.data) {
                override fun onChanged(station: MergedStation?) {
                    if (station != null) {
                        hafasStationsResource.initialize(station, ORIGIN_STATION)

                        if (hafasStationsResource.isLoadingPreconditionsMet) {
                            mediatorResource.removeSource(stationResource)
                            destroy()
                            stationObserver = null
                            hafasStationsResource.loadIfNecessary()
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        if (stationObserver != null) {
            stationObserver!!.destroy()
            stationObserver = null
        }

        if (hafasStationsObserver != null) {
            hafasStationsObserver!!.destroy()
            hafasStationsObserver = null
        }
        mediatorResource.removeSource(stationResource)
        mediatorResource.removeSource(hafasStationsResource)
        stationResource = null
    }

    companion object {

        private val ORIGIN_STATION = "station"
    }

}
