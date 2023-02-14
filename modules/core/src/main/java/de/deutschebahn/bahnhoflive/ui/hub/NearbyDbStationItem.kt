/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.hub

import de.deutschebahn.bahnhoflive.repository.Station
import de.deutschebahn.bahnhoflive.ui.search.StopPlaceSearchResult


class NearbyDbStationItem(val dbStationSearchResult: StopPlaceSearchResult) :
    NearbyStationItem {

    override fun onLoadDetails() {
        dbStationSearchResult.timetable.first.loadIfNecessary()
    }

    override val type: Int
        get() = 0

    override val distance: Float
        get() = dbStationSearchResult.distance
              //  get() = dbStationSearchResult.timetable.second

    override fun bindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder) {
        (holder as NearbyDbDeparturesViewHolder).bind(dbStationSearchResult)
    }

    val station : Station
        get() = dbStationSearchResult.getStation()

 //   val timetableCollectorConnector : TimetableCollectorConnector?
   //     get() = dbStationSearchResult.timetableCollectorConnector

//    var timetable : Timetable?
//        get() = dbStationSearchResult.timetable
//        set(value) {dbStationSearchResult.timetable=value}

}