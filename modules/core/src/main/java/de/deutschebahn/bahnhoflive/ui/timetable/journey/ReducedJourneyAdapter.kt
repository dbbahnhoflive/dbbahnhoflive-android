package de.deutschebahn.bahnhoflive.ui.timetable.journey

import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.ui.timetable.HafasRouteStop
import de.deutschebahn.bahnhoflive.view.BaseListAdapter
import de.deutschebahn.bahnhoflive.view.ListViewHolderDelegate

class ReducedJourneyAdapter : BaseListAdapter<HafasRouteStop, ReducedJourneyItemViewHolder>(
    object : ListViewHolderDelegate<HafasRouteStop, ReducedJourneyItemViewHolder> {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ReducedJourneyItemViewHolder = ReducedJourneyItemViewHolder(parent)

        override fun onBindViewHolder(
            holder: ReducedJourneyItemViewHolder,
            item: HafasRouteStop,
            position: Int
        ) {
            holder.bind(item)
        }
    }) {

}
