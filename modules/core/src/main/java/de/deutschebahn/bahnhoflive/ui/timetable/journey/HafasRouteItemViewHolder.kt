package de.deutschebahn.bahnhoflive.ui.timetable.journey

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.databinding.ItemJourneyBinding
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.ui.timetable.RouteStop

class HafasRouteItemViewHolder(val itemJourneyBinding: ItemJourneyBinding) :
    ViewHolder<RouteStop>(itemJourneyBinding.root) {

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

    override fun onBind(item: RouteStop?) {
        super.onBind(item)

        with(itemJourneyBinding) {
            stopName.text = item?.name

            val isLastOrFirst = item?.isLast == true || item?.isFirst == true

            stopName.setTypeface(null, if(isLastOrFirst)Typeface.BOLD else Typeface.NORMAL)
            upperTrack.visibility= if(item?.isFirst == true) View.GONE else View.VISIBLE
            lowerTrack.visibility= if(item?.isLast == true) View.GONE else View.VISIBLE
            trackStop.isSelected = isLastOrFirst

        }

    }

}