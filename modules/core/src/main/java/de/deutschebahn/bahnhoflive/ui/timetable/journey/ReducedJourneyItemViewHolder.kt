package de.deutschebahn.bahnhoflive.ui.timetable.journey

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import de.deutschebahn.bahnhoflive.databinding.ItemJourneyBinding
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.ui.timetable.HafasRouteStop

class ReducedJourneyItemViewHolder(val itemJourneyBinding: ItemJourneyBinding) :
    ViewHolder<HafasRouteStop>(itemJourneyBinding.root) {

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

    override fun onBind(item: HafasRouteStop?) {
        super.onBind(item)

        itemJourneyBinding.stopName.text = item?.name

        itemJourneyBinding.trackStop.isSelected = item?.isCurrent == true
        itemJourneyBinding.upperTrack.isVisible = item?.isFirst == false
        itemJourneyBinding.lowerTrack.isVisible = item?.isLast == false
    }
}