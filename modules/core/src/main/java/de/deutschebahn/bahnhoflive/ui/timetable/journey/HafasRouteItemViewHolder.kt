package de.deutschebahn.bahnhoflive.ui.timetable.journey

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.databinding.ItemJourneyBinding
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.ui.timetable.localtransport.RouteStopConnector

class HafasRouteItemViewHolder(private val itemJourneyBinding: ItemJourneyBinding) :
    ViewHolder<RouteStopConnector>(itemJourneyBinding.root) {

    constructor(
        parent: ViewGroup,
        inflater: LayoutInflater = LayoutInflater.from(parent.context)
    ) : this(
        ItemJourneyBinding.inflate(
            inflater,
            parent,
            false
        )
    )

    override fun onBind(item: RouteStopConnector?) {
        super.onBind(item)

        with(itemJourneyBinding) {

            item?.routeStop.let {
                stopName.text = it?.name

                val isLastOrFirst = it?.isLast == true || it?.isFirst == true

                stopName.setTypeface(null, if (isLastOrFirst) Typeface.BOLD else Typeface.NORMAL)
                upperTrack.visibility = if (it?.isFirst == true) View.GONE else View.VISIBLE
                lowerTrack.visibility = if (it?.isLast == true) View.GONE else View.VISIBLE
            trackStop.isSelected = isLastOrFirst
            }
        }

    }

}