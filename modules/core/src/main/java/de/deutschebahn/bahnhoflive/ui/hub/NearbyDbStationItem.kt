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
        dbStationSearchResult.timetable.loadIfNecessary()
    }

    override val type: Int
        get() = 0

    override val distance: Float
        get() = dbStationSearchResult.distance

    override fun bindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder) {
        (holder as NearbyDbDeparturesViewHolder).bind(dbStationSearchResult)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NearbyDbStationItem) return false

        return station.id == other.station.id
    }

    override fun hashCode(): Int {
        return station.hashCode()
    }

    val station: Station
        get() = dbStationSearchResult.getStation()


}