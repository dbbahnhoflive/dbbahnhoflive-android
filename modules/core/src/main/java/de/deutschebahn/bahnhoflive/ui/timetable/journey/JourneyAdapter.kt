package de.deutschebahn.bahnhoflive.ui.timetable.journey

import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.view.BaseListAdapter
import de.deutschebahn.bahnhoflive.view.ListViewHolderDelegate

class JourneyAdapter : BaseListAdapter<JourneyStop, JourneyItemViewHolder>(
    object : ListViewHolderDelegate<JourneyStop, JourneyItemViewHolder> {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): JourneyItemViewHolder = JourneyItemViewHolder(parent)

        override fun onBindViewHolder(
            holder: JourneyItemViewHolder,
            item: JourneyStop,
            position: Int
        ) {
            holder.bind(item)
        }
    }) {

}
