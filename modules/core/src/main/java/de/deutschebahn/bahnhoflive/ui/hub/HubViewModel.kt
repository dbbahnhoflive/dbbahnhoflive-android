/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.hub

import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasTimetable
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


}
