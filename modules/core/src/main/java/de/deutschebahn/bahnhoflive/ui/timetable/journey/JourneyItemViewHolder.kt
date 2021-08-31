package de.deutschebahn.bahnhoflive.ui.timetable.journey

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import de.deutschebahn.bahnhoflive.databinding.ItemJourneyDetailedBinding
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import java.text.DateFormat

class JourneyItemViewHolder(val itemJourneyDetailedBinding: ItemJourneyDetailedBinding) :
    ViewHolder<JourneyStop>(itemJourneyDetailedBinding.root) {

    private val dateFormat = java.text.SimpleDateFormat.getTimeInstance(DateFormat.SHORT)

    constructor(
        parent: ViewGroup,
        inflater: LayoutInflater = LayoutInflater.from(parent.context)
    ) : this(
        ItemJourneyDetailedBinding.inflate(
            inflater,
            parent,
            false
        )
    )

    override fun onBind(item: JourneyStop?) {
        super.onBind(item)

        with(itemJourneyDetailedBinding) {
            stopName.text = item?.name

            platform.text = item?.platform?.let { "Gl. $it" }

            bindTimes(scheduledArrival, expectedArrival, item?.arrival)
            bindTimes(scheduledDeparture, expectedDeparture, item?.departure)
        }

    }

    private fun bindTimes(
        scheduledTimeView: TextView,
        estimatedTimeView: TextView,
        journeyStopEvent: JourneyStopEvent?
    ) {
        scheduledTimeView.text =
            journeyStopEvent?.parsedScheduledTime?.let { dateFormat.format(it) }
        estimatedTimeView.text =
            journeyStopEvent?.parsedEstimatedTime?.let { dateFormat.format(it) }
    }
}