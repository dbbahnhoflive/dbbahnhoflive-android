/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.timetable.localtransport

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.backend.hafas.HafasDepartures
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStationProduct
import de.deutschebahn.bahnhoflive.backend.local.model.RrtPoint
import de.deutschebahn.bahnhoflive.repository.HafasStationResource
import de.deutschebahn.bahnhoflive.repository.HafasTimetableResource
import de.deutschebahn.bahnhoflive.repository.MediatorResource
import de.deutschebahn.bahnhoflive.repository.MergedStation
import de.deutschebahn.bahnhoflive.repository.Resource
import de.deutschebahn.bahnhoflive.repository.Station
import de.deutschebahn.bahnhoflive.repository.StationResource
import de.deutschebahn.bahnhoflive.ui.accessibility.SpokenFeedbackAccessibilityLiveData
import de.deutschebahn.bahnhoflive.util.ManagedObserver
import de.deutschebahn.bahnhoflive.util.Token

open class HafasTimetableViewModel(val hafasapplication: Application) : AndroidViewModel(hafasapplication) {

    @JvmField
    val hafasTimetableResource = HafasTimetableResource()
    private val mediatorResource: MediatorResource<HafasDepartures> =
        object : MediatorResource<HafasDepartures>() {
            override fun onRefresh(): Boolean {
                return hafasTimetableResource.refresh() ||
                        hafasStationResource.refresh() || stationResource != null && stationResource!!.refresh() ||
                        super.onRefresh()
            }
        }
    @JvmField
    val hafasStationResource = HafasStationResource()
    @JvmField
    val pendingRailReplacementPointLiveData: MutableLiveData<RrtPoint?> =
        MutableLiveData<RrtPoint?>(null)
    private var hafasStationObserver: ManagedObserver<HafasStation>? = null
    private var hafasStationErrorObserver: ManagedObserver<VolleyError>? = null
    private var stationObserver: ManagedObserver<MergedStation>? = null
    private var stationResource: StationResource? = null
    private val initializationPending = Token()

    var station: Station? = null
        protected set
    protected var last_station: Station? = null
    var hafasStations: List<HafasStation>? = null
        private set
    var filterName: String? = null
    fun getHafasTimetableResource(): Resource<HafasDepartures, VolleyError> {
        return mediatorResource
    }

    val mapAvailableLiveData =
        SpokenFeedbackAccessibilityLiveData(hafasapplication).switchMap { spokenFeedbackAccessibilityEnabled ->
            if (stationResource != null) {
            stationResource?.data?.map { mergedStation ->
                !(spokenFeedbackAccessibilityEnabled || mergedStation.location == null)
                }
            } else {

                hafasTimetableResource.data.map {
                    !spokenFeedbackAccessibilityEnabled && it != null
                }

            }
        }
    /**
     * Initialization from [DeparturesActivity]
     */
    fun initialize(
        hafasStation: HafasStation?,
        departures: HafasDepartures?,
        filterStricly: Boolean,
        station: Station?,
        hafasStations: List<HafasStation>?,
        showAllDepartures : Boolean
    ) {
        if (initializationPending.take()) {
            hafasTimetableResource.initialize(
                hafasStation,
                departures,
                filterStricly,
                HafasStationResource.ORIGIN_TIMETABLE,
                showAllDepartures
            )
            hafasStationResource.initialize(hafasStation)
            this.station = station
            this.hafasStations = hafasStations
        }
    }

    /**
     * Initialization from [de.deutschebahn.bahnhoflive.ui.station.StationActivity]
     */
    fun initialize(stationResource: StationResource) {
        if (initializationPending.take()) {
            this.stationResource = stationResource
            mediatorResource.addErrorSource(hafasStationResource)
            mediatorResource.addLoadingStatusSource(hafasStationResource)
            hafasStationObserver =
                object : ManagedObserver<HafasStation>(hafasStationResource.data) {
                    override fun onChanged(value: HafasStation?) {
                        hafasTimetableResource.initialize(value, null, false, ORIGIN_STATION, true)
                        mediatorResource.removeSource(hafasStationResource)
                        destroy()
                        hafasStationObserver = null
                    }
                }
            hafasStationErrorObserver =
                object : ManagedObserver<VolleyError>(hafasStationResource.error) {
                    override fun onChanged(value: VolleyError?) {
                        if (value != null) {
                            hafasTimetableResource.setError(value)
                        }
                    }
                }
            mediatorResource.addErrorSource(stationResource)
            mediatorResource.addLoadingStatusSource(stationResource)
            stationObserver = object : ManagedObserver<MergedStation>(stationResource.data) {
                override fun onChanged(value: MergedStation?) {
                    hafasStationResource.initialize(value)
                    if (hafasStationResource.isLoadingPreconditionsMet) {
                        mediatorResource.removeSource(stationResource)
                        destroy()
                        stationObserver = null
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
        if (hafasStationObserver != null) {
            hafasStationObserver!!.destroy()
            hafasStationObserver = null
        }
        if (hafasStationErrorObserver != null) {
            hafasStationErrorObserver!!.destroy()
            hafasStationErrorObserver = null
        }
    }

    fun loadMore() {
        hafasTimetableResource.addHour()
        hafasTimetableResource.refresh()
    }

    val selectedHafasStationProduct = MutableLiveData<HafasStationProduct>()
    val selectedHafasJourney = MutableLiveData<DetailedHafasEvent>()

    init {
        mediatorResource.addSource(hafasTimetableResource)
    }

    companion object {
        const val ORIGIN_STATION = "station"
    }
}