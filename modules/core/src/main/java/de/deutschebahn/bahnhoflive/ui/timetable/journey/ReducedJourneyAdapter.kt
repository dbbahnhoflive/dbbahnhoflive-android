package de.deutschebahn.bahnhoflive.ui.timetable.journey

import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.ui.timetable.RouteStop
import de.deutschebahn.bahnhoflive.view.BaseListAdapter
import de.deutschebahn.bahnhoflive.view.ListViewHolderDelegate

class ReducedJourneyAdapter : BaseListAdapter<RouteStop, ReducedJourneyItemViewHolder>(
    object : ListViewHolderDelegate<RouteStop, ReducedJourneyItemViewHolder> {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ReducedJourneyItemViewHolder = ReducedJourneyItemViewHolder(parent)

        override fun onBindViewHolder(
            holder: ReducedJourneyItemViewHolder,
            item: RouteStop,
            position: Int
        ) {
            holder.bind(item)
        }
    }) {

}
