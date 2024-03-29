/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.timetable.localtransport

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasEvent
import de.deutschebahn.bahnhoflive.ui.TimetableItemOverviewViewHolder
import de.deutschebahn.bahnhoflive.util.accessibility.AccessibilityUtilities
import de.deutschebahn.bahnhoflive.util.formatShortTime

class HafasEventOverviewViewHolder(view: View) : TimetableItemOverviewViewHolder<HafasEvent>(view) {

    private val issueIndicator : ImageView? = itemView.findViewById<ImageView>(R.id.issue_indicator)

    override fun onBind(item: HafasEvent?) {
        super.onBind(item)

        if (item == null) {
            itemView.visibility = View.INVISIBLE
        } else {
            itemView.visibility = View.VISIBLE

            transportationNameView?.text = item.displayName
            directionView?.text = item.direction

            val scheduledTimeString = item.scheduledTime?.let { (it.time).formatShortTime() } ?: "?"
            timeView?.text = scheduledTimeString

            val estimatedTimeString: CharSequence = item.estimatedTime?.let { (it.time).formatShortTime() }
                ?: scheduledTimeString
            bindDelay(item.delay.toLong(), estimatedTimeString)

            val resources = itemView.resources

            val platform: String? = item.shortcutTrackName

            platformView?.let {
                it.text = platform
                it.isVisible = !platform.isNullOrEmpty()
                it.setTextColor( if(item.hasIssue) Color.RED else Color.BLACK)

                }

            issueIndicator?.isVisible = item.hasIssue


            itemView.contentDescription = resources.getString(
                R.string.sr_template_local_departure_overview,
                AccessibilityUtilities.fixScreenReaderText(item.displayName),
                item.direction,
                AccessibilityUtilities.getSpokenTime(scheduledTimeString),
                when {
                    item.cancelled -> "Verbindung f�llt aus"
                    item.trackChanged -> "heute abweichend ${AccessibilityUtilities.convertTrackSpan(item.prettyTrackName)}"
                    else -> AccessibilityUtilities.convertTrackSpan(item.prettyTrackName)
                },
                if (item.delay > 0)
                    resources.getString(
                        R.string.sr_template_estimated_departure,
                        AccessibilityUtilities.getSpokenTime(estimatedTimeString)
                    )
                else ""
            )

        }
    }



//    companion object {
//        val DATE_FORMAT = SimpleDateFormat("HH:mm", Locale.GERMANY)
//    }
}
