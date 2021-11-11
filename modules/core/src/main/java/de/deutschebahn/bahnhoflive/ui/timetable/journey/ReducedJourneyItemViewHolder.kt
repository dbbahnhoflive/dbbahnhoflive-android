package de.deutschebahn.bahnhoflive.ui.timetable.journey

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import de.deutschebahn.bahnhoflive.databinding.ItemJourneyBinding
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.ui.timetable.RouteStop

class ReducedJourneyItemViewHolder(val itemJourneyBinding: ItemJourneyBinding) :
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

        itemJourneyBinding.stopName.text = item?.name

        itemJourneyBinding.trackStop.isSelected = item?.isCurrent == true
        itemJourneyBinding.upperTrack.isVisible = item?.isFirst == false
        itemJourneyBinding.lowerTrack.isVisible = item?.isLast == false
    }
}