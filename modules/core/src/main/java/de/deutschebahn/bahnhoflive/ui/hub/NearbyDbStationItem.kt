/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.hub

import de.deutschebahn.bahnhoflive.repository.DbTimetableResource
import de.deutschebahn.bahnhoflive.repository.InternalStation
import de.deutschebahn.bahnhoflive.ui.search.StationSearchResult

class NearbyDbStationItem(val dbStationSearchResult: StationSearchResult<InternalStation, DbTimetableResource>) :
    NearbyStationItem {

    override fun onLoadDetails() {
        dbStationSearchResult.timetable.loadIfNecessary()
    }

    override val type: Int
        get() = 0

    override val distance: Float
        get() = dbStationSearchResult.timetable.distanceInKm

    override fun bindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder) {
        (holder as NearbyDbDeparturesViewHolder).bind(dbStationSearchResult)
    }

}