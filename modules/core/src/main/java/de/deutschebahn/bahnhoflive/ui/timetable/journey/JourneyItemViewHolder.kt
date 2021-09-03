package de.deutschebahn.bahnhoflive.ui.timetable.journey

import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.databinding.ItemJourneyDetailedBinding
import de.deutschebahn.bahnhoflive.ui.Status
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import java.text.DateFormat
import java.util.concurrent.TimeUnit

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

    val normalTypeFace = Typeface.defaultFromStyle(Typeface.NORMAL)

    val highlightableTextViews = itemJourneyDetailedBinding.run {
        listOf(stopName, scheduledArrival, expectedArrival, scheduledDeparture, expectedDeparture)
    }

    override fun onBind(item: JourneyStop?) {
        super.onBind(item)

        with(itemJourneyDetailedBinding) {
            stopName.text = item?.name

            platform.text = item?.platform?.let { "Gl. $it" }

            when {
                item?.isAdditional == true -> advice.setText(R.string.journey_stop_additional)
                item?.isPlatformChange == true -> advice.setText(R.string.journey_stop_platform_change)
                else -> advice.text = null
            }

            bindTimes(scheduledArrival, expectedArrival, item?.arrival)
            bindTimes(scheduledDeparture, expectedDeparture, item?.departure)

            trackStop.isSelected = item?.current == true
            upperTrack.isVisible = item?.first == false
            lowerTrack.isVisible = item?.last == false


            (if (item?.highlight == true) Typeface.BOLD else Typeface.NORMAL).let { textStyle ->
                highlightableTextViews.forEach { textView ->
                    textView.setTypeface(
                        Typeface.create(textView.typeface, textStyle),
                        textStyle
                    )
                }
            }

            root.contentDescription = item?.run {
                listOfNotNull(
                    listOfNotNull(
                        name,
                        platform?.let { "Gleis $it " }
                    ).joinToString(", ", postfix = "."),
                    listOfNotNull(
                        when {
                            isAdditional -> "(Hinweis: \"Zusätzlicher Halt\")"
                            isPlatformChange -> "(Hinweis: \"Gleiswechsel\")"
                            else -> null
                        },
                        arrival?.formatContentDescription("Ankunft"),
                        departure?.formatContentDescription("Abfahrt")
                    ).joinToString("; ", postfix = ".")
                ).joinToString(separator = " ")
            }.also {
                Log.d(JourneyItemViewHolder::class.java.simpleName, "Content description:\n$it")
            }
        }

    }

    private fun JourneyStopEvent.formatContentDescription(prefix: String) =
        listOfNotNull(
            prefix,
            parsedScheduledTime?.run {
                "${formatTime()} Uhr"
            },
            parsedEstimatedTime?.takeUnless { it == parsedScheduledTime }?.run {
                "(heute voraussichtlich ${formatTime()} Uhr)"
            }
        ).joinToString(" ")

    private fun Long.formatTime() = dateFormat.format(this)

    private fun bindTimes(
        scheduledTimeView: TextView,
        estimatedTimeView: TextView,
        journeyStopEvent: JourneyStopEvent?
    ) {
        scheduledTimeView.text =
            journeyStopEvent?.parsedScheduledTime?.let { dateFormat.format(it) }
        estimatedTimeView.text =
            journeyStopEvent?.parsedEstimatedTime?.let { dateFormat.format(it) }
        estimatedTimeView.setTextColor(
            estimatedTimeView.context.resources.getColor(
                journeyStopEvent?.let { journeyStopEvent ->
                    journeyStopEvent.parsedScheduledTime?.let {
                        journeyStopEvent.parsedEstimatedTime?.minus(
                            it
                        )?.takeIf { it > TimeUnit.MINUTES.toMillis(5) }?.let {
                            Status.NEGATIVE.color
                        }
                    }
                } ?: Status.POSITIVE.color)
        )
    }
}