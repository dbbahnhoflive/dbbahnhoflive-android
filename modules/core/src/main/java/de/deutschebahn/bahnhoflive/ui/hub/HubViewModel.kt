/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.hub

import android.app.Application
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasTimetable
import de.deutschebahn.bahnhoflive.repository.InternalStation
import de.deutschebahn.bahnhoflive.ui.StadaStationCacheViewModel

class HubViewModel(application: Application) : StadaStationCacheViewModel(application) {

    val hafasData: ArrayList<HafasTimetable> = ArrayList()

    fun buildhafasData(hafasStations: List<HafasStation>) {
        wrapTimetables(hafasStations)
    }

    private fun wrapTimetables(stations: List<HafasStation>) {
        hafasData.clear()
        hafasData.ensureCapacity(stations.size)

        for (station in stations) {
            hafasData.add(HafasTimetable(station))
        }

    }

    val updatedStationRepository get() = BaseApplication.INSTANCE.applicationServices.updatedStationRepository

    fun getUpdatedStationLiveData(station: InternalStation) =
        liveData(viewModelScope.coroutineContext) {
            emit(updatedStationRepository.getUpdatedStation(station))
        }
}
