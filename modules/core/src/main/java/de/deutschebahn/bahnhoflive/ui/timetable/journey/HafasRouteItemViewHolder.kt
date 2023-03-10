package de.deutschebahn.bahnhoflive.ui.timetable.journey

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isGone
import de.deutschebahn.bahnhoflive.databinding.ItemJourneyBinding
import de.deutschebahn.bahnhoflive.databinding.ItemJourneyDetailedBinding
import de.deutschebahn.bahnhoflive.ui.Status
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.ui.timetable.RouteStop
import java.text.DateFormat
import java.util.concurrent.TimeUnit

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