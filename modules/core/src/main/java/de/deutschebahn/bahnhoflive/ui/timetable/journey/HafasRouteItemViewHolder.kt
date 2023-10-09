package de.deutschebahn.bahnhoflive.ui.timetable.journey

import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.databinding.ItemJourneyDetailedBinding
import de.deutschebahn.bahnhoflive.ui.Status
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.ui.timetable.HafasRouteStop
import de.deutschebahn.bahnhoflive.util.formatShortTime
import de.deutschebahn.bahnhoflive.util.time.EpochParser
import de.deutschebahn.bahnhoflive.util.visibleElseGone
import java.text.DateFormat
import java.util.Date
import java.util.concurrent.TimeUnit


class HafasRouteItemViewHolder(private val itemJourneyBinding: ItemJourneyDetailedBinding) :
    ViewHolder<HafasRouteStop>(itemJourneyBinding.root) {

    companion object {
        val TIME_PARSER = EpochParser.getInstance()
    }

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

    private val highlightableTextViews = itemJourneyBinding.run {
        listOf(stopName, scheduledArrival, expectedArrival, scheduledDeparture, expectedDeparture)
    }

    override fun onBind(item: HafasRouteStop?) {
        super.onBind(item)

        with(itemJourneyBinding) {

            item?.let { it ->
                stopName.text = it.name

                val isLastOrFirst = it.isLast || it.isFirst

                (if (it.highlight) Typeface.BOLD else Typeface.NORMAL).let { textStyle ->
                    highlightableTextViews.forEach { textView ->
                        textView.setTypeface(
                            Typeface.create(textView.typeface, textStyle),
                            textStyle
                        )
                    }
                }

                upperTrack.visibleElseGone(!it.isFirst)
                lowerTrack.visibleElseGone(!it.isLast)
                trackStop.isSelected = isLastOrFirst

                item.hafasStop?.let { itHafasStop ->
                    bindTimes(scheduledArrival,  expectedArrival, itHafasStop.arrivalTime())
                    bindTimes(scheduledDeparture,  expectedDeparture, itHafasStop.departureTime())


                    advice.text =
                        when {
                            itHafasStop.cancelled -> "Halt fällt aus"
                            itHafasStop.additional -> "Zusätzlicher Halt"
                            itHafasStop.departureTrackChanged -> "Gleiswechsel"
                            else -> ""
                    }


                    advice.isVisible = advice.text.isNotEmpty()
                    advice.setCompoundDrawablesWithIntrinsicBounds(
                        if (advice.isVisible) R.drawable.app_warndreieck else 0,
                        0,
                        0,
                        0
                    )

                    var platformText = ""
                    var sr_platformText: String? = null

                    var track = itHafasStop.arrTrack
                    if (track == null || itHafasStop.rtDepTrack != null)
                        track = itHafasStop.rtDepTrack


                    track?.let {

//                        platformText = item.hafasEvent?.shortcutTrackName?:""
//                        sr_platformText = item.hafasEvent?.prettyTrackName?:""

                        if (itHafasStop.departureTrackChanged)
                            sr_platformText = "heute abweichend von "
                        else
                            sr_platformText = ""

                        if (item.hafasEvent?.product?.onTrack() == true) {
                            platformText = "Gl. $it"
                            sr_platformText += "Gleis $it"
                        } else {
                            platformText = "Pl. $it"
                            sr_platformText += "Plattform $it"
                        }
                        platform.text = platformText
                    }

                    platform.visibleElseGone(track != null)

                    // for screenreader
                    root.contentDescription = item.run {
                        listOfNotNull(
                            listOfNotNull(
                                name,
                                sr_platformText
                            ).joinToString(", ", postfix = "."),
                            listOfNotNull(
                                when {
                                    this.hafasStop?.cancelled == true -> "(Hinweis: \"Halt fällt aus\")"
                                    this.hafasStop?.additional == true -> "(Hinweis: \"Zusätzlicher Halt\")"
//                                    this.hafasStop?.departureTrackChanged == true -> "(Hinweis: \"Gleisänderung\")"
                                    else -> null
                                },
                                formatContentDescription(
                                    hafasStop?.arrivalTime(),
                                    "Ankunft",
                                    hafasStop?.let { it.progress >= 0.0 }),
                                formatContentDescription(
                                    hafasStop?.departureTime(),
                                    "Abfahrt",
                                    hafasStop?.let { it.progress > 0.0 })
                            ).joinToString("; ", postfix = ".")
                        ).joinToString(separator = " ")
                    }.also {
                    // Log.d(JourneyItemViewHolder::class.java.simpleName, "Content description:\n$it")
                }
            }
            }
        }
    }


    /**
     * Formatiert eine Zeitangabe.
     *
     * @param time   time.first : scheduled geplante Zeit,  time.second : estimated Zeit mit Verspätung
     * @param prefix Startstring
     * @param past   true = hat Verspätung -> vorraussichtlich
     * @return       Formatted string.
     */

    private fun formatContentDescription(time : Pair<Date?, Date?>?, prefix: String, past: Boolean?) =
        listOfNotNull(
            if(time?.first != null) prefix else null,
            time?.first?.run {
                "${this.time.formatShortTime()}"
            },
            time?.second?.takeUnless { it == time.first }?.run {
                "(heute ${if (past!=null && !past) "voraussichtlich " else ""}${this.time.formatShortTime()})"
    }
        ).joinToString(" ")

    private fun bindTimes(
        scheduledTimeView: TextView,
        estimatedTimeView: TextView,
        time: Pair<Date?, Date?>
    ) {
        val parsedScheduledTime: Long? = time.first?.time
        var parsedEstimatedTime: Long? = time.second?.time

        scheduledTimeView.text =
            parsedScheduledTime?.let { dateFormat.format(it) }

        if (parsedEstimatedTime == null)
            parsedEstimatedTime = parsedScheduledTime

        estimatedTimeView.text =
            parsedEstimatedTime?.let { dateFormat.format(it) }

        val colorId =
                parsedScheduledTime?.let {
                    parsedEstimatedTime?.minus(
                        it
                    )?.takeIf { it > TimeUnit.MINUTES.toMillis(5) }?.let {
                        Status.NEGATIVE.color
                    }
            } ?: Status.POSITIVE.color

        estimatedTimeView.setTextColor(ContextCompat.getColor(estimatedTimeView.context, colorId))

        val viewsVisible = parsedScheduledTime != null
        scheduledTimeView.visibleElseGone(viewsVisible)
        estimatedTimeView.visibleElseGone(viewsVisible)
    }
}