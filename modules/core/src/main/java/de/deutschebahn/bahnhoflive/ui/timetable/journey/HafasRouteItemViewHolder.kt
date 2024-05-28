package de.deutschebahn.bahnhoflive.ui.timetable.journey

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.databinding.ItemJourneyDetailedBinding
import de.deutschebahn.bahnhoflive.ui.Status
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.ui.timetable.HafasRouteStop
import de.deutschebahn.bahnhoflive.util.accessibility.AccessibilityUtilities
import de.deutschebahn.bahnhoflive.util.formatShortTime
import de.deutschebahn.bahnhoflive.util.visibleElseGone
import java.util.Date
import java.util.concurrent.TimeUnit


class HafasRouteItemViewHolder(private val itemJourneyBinding: ItemJourneyDetailedBinding) :
    ViewHolder<HafasRouteStop>(itemJourneyBinding.root) {

//    companion object {
//        val TIME_PARSER = EpochParser.getInstance()
//    }

//    private val dateFormat = java.text.SimpleDateFormat.getTimeInstance(DateFormat.SHORT)

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

        if(item==null)
            itemJourneyBinding.root.visibility=View.INVISIBLE
        else
        with(itemJourneyBinding) {

            item.let { itHafasRouteStop ->
                stopName.text = itHafasRouteStop.name

                val isLastOrFirst = itHafasRouteStop.isLast || itHafasRouteStop.isFirst

                (if (itHafasRouteStop.highlight) Typeface.BOLD else Typeface.NORMAL).let { textStyle ->
                    highlightableTextViews.forEach { textView ->
                        textView.setTypeface(
                            Typeface.create(textView.typeface, textStyle),
                            textStyle
                        )
                    }
                }

                upperTrack.visibleElseGone(!itHafasRouteStop.isFirst)
                lowerTrack.visibleElseGone(!itHafasRouteStop.isLast)
                trackStop.isSelected = isLastOrFirst

                itHafasRouteStop.hafasStop?.let { itHafasStop ->

                    advice.isGone = false

                    val adviceText =
                        when {
                            itHafasStop.cancelled -> "Halt fällt aus"
                            itHafasStop.additional -> "Zusätzlicher Halt"
                            itHafasStop.departureTrackChanged -> "Gleiswechsel"
                            else -> ""
                    }

                    val hasAdviceText = adviceText.isNotEmpty()

                    advice.setCompoundDrawablesWithIntrinsicBounds(
                        if (hasAdviceText && !itHafasStop.additional) R.drawable.app_warndreieck else 0,
                        0,
                        0,
                        0
                    )

                    advice.text = adviceText
                    advice.isSelected = hasAdviceText && !itHafasStop.additional

                    val arrivalTime = itHafasStop.arrivalTime()
                    val departureTime = itHafasStop.departureTime()



                    val hasArrivalTime: Boolean = arrivalTime.first!=null
                    val hasDepartureTime : Boolean = departureTime.first!=null


                    // layout is so designt, das elemente sich automatisch vertikal zentrieren
                    // ab 16.4.2024 nicht mehr gewünscht -> Anpassung an IOS design

                    // Normalfall: ankunft+abfahrt vorhanden, mit od. ohne advice -> 2 Zeilen
                    // Sonderfall 1 : keine ankunft, kein advice  -> advice GONE, arrival GONE
                    // Sonderfall 2 : keine abfahrt, kein advice  -> advice GONE, departure GONE
                    // Sonderfall 3 : keine ankunft, advice  -> departure INVISIBLE, departure an arrival-position
                    // Sonderfall 4 : keine abfahrt, advice  -> departure INVISIBLE

                    var arrivalViewMode = View.VISIBLE
                    var departureViewMode = View.VISIBLE

                    if (!hasArrivalTime && !hasAdviceText) {
                        advice.isGone = true
                        arrivalViewMode = View.GONE
                    } else
                        if (!hasDepartureTime && !hasAdviceText) {
                            advice.isGone = true
                            departureViewMode = View.GONE
                        } else
                            if (!hasArrivalTime && hasAdviceText) {
                                departureViewMode = View.INVISIBLE
                            } else
                                if (!hasDepartureTime && hasAdviceText) {
                                    departureViewMode = View.INVISIBLE

                                }

                    if (!hasArrivalTime && hasAdviceText)
                        bindTimes(
                            scheduledArrival,
                            expectedArrival,
                            departureTime,
                            arrivalViewMode
                        )
                    else
                        bindTimes(
                            scheduledArrival,
                            expectedArrival,
                            arrivalTime,
                            arrivalViewMode
                        )

                    bindTimes(
                        scheduledDeparture,
                        expectedDeparture,
                        departureTime,
                        departureViewMode
                    )

//                    bindTimes(scheduledArrival,  expectedArrival, arrivalTime, arrivalViewMode)
//                    bindTimes(scheduledDeparture,  expectedDeparture, departureTime, departureViewMode)


                    var platformText: String
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

                        if (itHafasRouteStop.hafasEvent?.product?.onTrack() == true) {
                            platformText = "Gl. $it"
                            sr_platformText += "Gleis $it"
                        } else {
                            platformText = "Pl. $it"
                            sr_platformText += "Plattform $it"
                        }
                        platform.text = platformText
                    }

                    platform.isVisible = track != null

                    // for screenreader
                    root.contentDescription = itHafasRouteStop.run {
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
                "${ AccessibilityUtilities.getSpokenTime(this.time.formatShortTime())}"
            },
            time?.second?.takeUnless { it == time.first }?.run {
                "(heute ${if (past!=null && !past) "voraussichtlich " else ""}${AccessibilityUtilities.getSpokenTime(this.time.formatShortTime())})"
    }
        ).joinToString(" ")

    private fun bindTimes(
        scheduledTimeView: TextView,
        estimatedTimeView: TextView,
        time: Pair<Date?, Date?>,
        viewMode : Int
    ) {
        val parsedScheduledTime: Long? = time.first?.time
        var parsedEstimatedTime: Long? = time.second?.time

        scheduledTimeView.text =
            parsedScheduledTime?.formatShortTime()

        if (parsedEstimatedTime == null)
            parsedEstimatedTime = parsedScheduledTime

        estimatedTimeView.text =
            parsedEstimatedTime?.formatShortTime()

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
        scheduledTimeView.visibility = viewMode
        estimatedTimeView.visibility = viewMode
    }
}