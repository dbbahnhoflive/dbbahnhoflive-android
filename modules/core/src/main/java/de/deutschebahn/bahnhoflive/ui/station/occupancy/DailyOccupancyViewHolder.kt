package de.deutschebahn.bahnhoflive.ui.station.occupancy

import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.repository.occupancy.model.Occupancy
import de.deutschebahn.bahnhoflive.ui.ViewHolder

class DailyOccupancyViewHolder(
    parent: ViewGroup,
    timeTextWidth: Float
) : ViewHolder<Pair<Occupancy, Int>>(
    parent,
    R.layout.include_graph
) {

    val graphViewBinder = GraphViewBinder(itemView, timeTextWidth)

    override fun onBind(item: Pair<Occupancy, Int>?) {
        super.onBind(item)

        val occupancy = item?.first

        graphViewBinder.set(
            item?.second?.let { occupancy?.dailyOccupancies?.get(it) },
            occupancy?.mostRecent,
            occupancy?.max
        )

    }
}
