package de.deutschebahn.bahnhoflive.ui.timetable.journey

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasEvent
import de.deutschebahn.bahnhoflive.databinding.ItemJourneyDetailedBinding
import de.deutschebahn.bahnhoflive.ui.Status
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.ui.timetable.HafasRouteStop
import de.deutschebahn.bahnhoflive.util.formatShortTime
import de.deutschebahn.bahnhoflive.util.time.EpochParser
import de.deutschebahn.bahnhoflive.util.visibleElseGone
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
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

    override fun onBind(item: HafasRouteStop?) {
        super.onBind(item)

        with(itemJourneyBinding) {

            item?.let {
                stopName.text = it.name

                val isLastOrFirst = it.isLast || it.isFirst

                stopName.setTypeface(null, if (isLastOrFirst) Typeface.BOLD else Typeface.NORMAL)
                upperTrack.visibleElseGone(!it.isFirst)
                lowerTrack.visibleElseGone(!it.isLast)
                trackStop.isSelected = isLastOrFirst

                item.hafasStop?.let {itHafasStop->
                    bindTimes(scheduledArrival, itHafasStop.arrTime, expectedArrival, itHafasStop.rtArrTime)
                    bindTimes(scheduledDeparture,  itHafasStop.depTime, expectedDeparture, itHafasStop.rtDepTime)

                    if(itHafasStop.cancelled) {
                    advice.text = "Halt fällt aus"
                    }
                    else
                    if(itHafasStop.additional) {
                        advice.text = "Zusätzlicher Halt"
                    }

                    advice.setCompoundDrawablesWithIntrinsicBounds(if(itHafasStop.cancelled) R.drawable.app_warndreieck else 0,0,0,0)
                    advice.visibleElseGone(itHafasStop.cancelled || itHafasStop.additional)

                    var track = itHafasStop.arrTrack
                    if(track==null || itHafasStop.depTrack!=null)
                        track = itHafasStop.depTrack

                    track?.let {
                        if (item.hafasEvent?.product?.onTrack() == true) {
                            platform.text = "Gl. $it"
                        } else {
                            platform.text = "Pl. $it"
                        }
                    }
//                    platform.text = "Gl. " + track
                    platform.visibleElseGone(track!=null)

                    // for screenreader
//                    val contentDescription = item?.run {
//                        listOfNotNull(
//                            listOfNotNull(
//                                name,
//                                platform?.let { "Gleis $it " }
//                            ).joinToString(", ", postfix = "."),
//                            listOfNotNull(
////                        when {
////                            this.additional -> "(Hinweis: \"Zusätzlicher Halt\")"
////                            else -> null
////                        },
//                                arrival?.formatContentDescription("Ankunft", progress >= 0),
//                                departure?.formatContentDescription("Abfahrt", progress > 0)
//                            ).joinToString("; ", postfix = ".")
//                        ).joinToString(separator = " ")
//                    }.also {
////                Log.d(JourneyItemViewHolder::class.java.simpleName, "Content description:\n$it")
//                    }
                }
            }
            }
        }

    private fun HafasEvent.formatContentDescription(prefix: String, past: Boolean) =
        listOfNotNull(
            prefix,
            scheduledTime?.run {
                "${this.time.formatShortTime()} Uhr"
            },
            estimatedTime?.takeUnless { it == scheduledTime }?.run {
                "(heute ${if (!past) "voraussichtlich " else ""}${this.time.formatShortTime()} Uhr)"
    }
        ).joinToString(" ")

    private fun bindTimes(
        scheduledTimeView: TextView,
        scheduledTime:String?,

        estimatedTimeView: TextView,
        estimatedTime : String?
    ) {
        var parsedScheduledTime: Long? = null
        var parsedEstimatedTime: Long? = null

        val formatter: DateFormat = SimpleDateFormat("HH:mm:ss", Locale.GERMANY)
        val cal = Calendar.getInstance()

        if(scheduledTime!=null) {
            try {
                cal.time = formatter.parse(scheduledTime)!!
                parsedScheduledTime = cal.timeInMillis
            } catch (_: ParseException) {
            }
        }

        scheduledTimeView.text =
            parsedScheduledTime?.let { dateFormat.format(it) }

        if(estimatedTime!=null) {
            try {
                cal.time = formatter.parse(estimatedTime)!!
                parsedEstimatedTime = cal.timeInMillis
            } catch (_: ParseException) {
            }
        }
        else
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