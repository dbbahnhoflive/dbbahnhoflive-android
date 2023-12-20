/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.hub

import de.deutschebahn.bahnhoflive.ui.search.HafasStationSearchResult

class NearbyHafasStationItem(val hafasStationSearchResult: HafasStationSearchResult) :
    NearbyStationItem {
    override fun onLoadDetails() {
        hafasStationSearchResult.timetable.requestTimetable(true, HubFragment.ORIGIN_HUB, true)
    }

    override val type: Int
        get() = 1

    override val distance: Float
        get() = hafasStationSearchResult.timetable.station.dist / 1000f

    override fun bindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder) {
        (holder as NearbyDeparturesViewHolder).bind(hafasStationSearchResult)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NearbyHafasStationItem) return false

        if (hafasStationSearchResult.timetable.station.extId != other.hafasStationSearchResult.timetable.station.extId) return false

        return true
    }

    override fun hashCode(): Int {
        return hafasStationSearchResult.hashCode()
    }

    fun refresh() {
        hafasStationSearchResult.timetable.refreshTimetable()
    }

}