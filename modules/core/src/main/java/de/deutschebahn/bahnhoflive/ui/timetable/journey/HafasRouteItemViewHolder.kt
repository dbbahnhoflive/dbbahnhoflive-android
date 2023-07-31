package de.deutschebahn.bahnhoflive.ui.timetable.journey

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import de.deutschebahn.bahnhoflive.databinding.ItemJourneyDetailedBinding
import de.deutschebahn.bahnhoflive.ui.Status
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.ui.timetable.RouteStop
import de.deutschebahn.bahnhoflive.util.time.EpochParser
import de.deutschebahn.bahnhoflive.util.visibleElseGone
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit


class HafasRouteItemViewHolder(private val itemJourneyBinding: ItemJourneyDetailedBinding) :
    ViewHolder<RouteStop>(itemJourneyBinding.root) {

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

    override fun onBind(item: RouteStop?) {
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

                    advice.text = "Halt fällt aus"
                    advice.isGone = !itHafasStop.cancelled

                    var track = itHafasStop.arrTrack
                    if(track==null || itHafasStop.depTrack!=null)
                        track = itHafasStop.depTrack

                    platform.text = "Gl. " + track
                    platform.visibleElseGone(track!=null)
                }


            }
        }

    }

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