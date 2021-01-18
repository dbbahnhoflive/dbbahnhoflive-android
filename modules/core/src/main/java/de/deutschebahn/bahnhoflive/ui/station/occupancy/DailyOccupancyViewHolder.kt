package de.deutschebahn.bahnhoflive.ui.station.occupancy

import android.view.View
import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.repository.occupancy.model.Occupancy
import de.deutschebahn.bahnhoflive.ui.ViewHolder

class DailyOccupancyViewHolder(
    parent: ViewGroup,
    timeTextWidth: Float,
    val onClickListener: View.OnClickListener?
) : ViewHolder<Pair<Occupancy, Int>>(
    parent,
    R.layout.include_graph
) {

    init {
        itemView.setOnClickListener(onClickListener)
    }

    val graphViewBinder = GraphViewBinder(itemView, timeTextWidth)

    override fun onBind(item: Pair<Occupancy, Int>?) {
        super.onBind(item)

        val occupancy = item?.first

        graphViewBinder.set(
            item?.second?.let { occupancy?.dailyOccupancies?.get(it) },
            occupancy?.getCurrentHourlyOccupancy(),
            occupancy?.max
        )

    }
}
