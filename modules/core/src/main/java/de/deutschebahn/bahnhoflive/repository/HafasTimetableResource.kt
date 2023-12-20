/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository

import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.backend.hafas.HafasDepartures
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation
import de.deutschebahn.bahnhoflive.repository.timetable.Constants
import de.deutschebahn.bahnhoflive.util.isNotZero

class HafasTimetableResource : RemoteResource<HafasDepartures>() {

    var hafasStation: HafasStation? = null
        private set

    private var filterStrictly = true
    private var showAllDepartures = true
    private var origin = HafasStationResource.ORIGIN_TIMETABLE
    private var hours = Constants.PRELOAD_HOURS

    fun initialize(hafasStation: HafasStation?,
                   departures: HafasDepartures?,
                   filterStrictly: Boolean,
                   origin: String,
                   showAllDepartures:Boolean
                   ) {

        this.showAllDepartures = showAllDepartures
        this.filterStrictly = filterStrictly
        this.origin = origin

        setData(departures)

        if (isLoadingPreconditionsMet) {
            return
        }

        this.hafasStation = hafasStation
        loadData(false)
    }

    fun refresh(hafasStation: HafasStation?) {
        this.hafasStation = hafasStation
        loadData(true)
    }

    override fun onStartLoading(force: Boolean) {
        BaseApplication.get().repositories.localTransportRepository.queryTimetable(
            hafasStation!!,
            origin,
            Listener(),
            hours,
            filterStrictly,
            force,
            showAllDepartures // x Bushaltestellen haben gleichen Namen, auf der map sieht man, welche es sind...
        )
    }

    fun addHour(): Int {
        return ++hours
    }

    override val isLoadingPreconditionsMet: Boolean get() = hafasStation != null

    fun setData(hafasDepartures: HafasDepartures?) {
        if (data.value == null && hafasDepartures != null) {
            setResult(hafasDepartures)
        }
    }


    public override fun setError(reason: VolleyError?) {
        super.setError(reason)
    }

    fun hasStationWithLocation() : Boolean {
        return if(hafasStation!=null) (hafasStation?.latitude?.isNotZero() == true && hafasStation?.longitude?.isNotZero() == true) else false
    }

}
