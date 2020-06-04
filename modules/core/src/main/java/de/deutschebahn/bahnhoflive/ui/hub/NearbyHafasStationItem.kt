package de.deutschebahn.bahnhoflive.ui.hub

import de.deutschebahn.bahnhoflive.ui.search.HafasStationSearchResult

class NearbyHafasStationItem(val hafasStationSearchResult: HafasStationSearchResult) : NearbyStationItem {
    override fun onLoadDetails() {
        hafasStationSearchResult.timetable.requestTimetable(true, HubFragment.ORIGIN_HUB)
    }

    override val type: Int
        get() = 1

    override val distance: Float
        get() = hafasStationSearchResult.timetable.station.dist / 1000f

    override fun bindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder) {
        (holder as NearbyDeparturesViewHolder).bind(hafasStationSearchResult)
    }
}