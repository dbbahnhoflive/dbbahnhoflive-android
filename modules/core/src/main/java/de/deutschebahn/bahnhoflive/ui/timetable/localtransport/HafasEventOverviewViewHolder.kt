/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.timetable.localtransport

import android.view.View
import android.widget.ImageView
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasEvent
import de.deutschebahn.bahnhoflive.ui.TimetableItemOverviewViewHolder
import de.deutschebahn.bahnhoflive.util.visibleElseGone
import java.text.SimpleDateFormat
import java.util.*

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

            val time = item.time?.let { DATE_FORMAT.format(it) } ?: "?"
            timeView?.text = time

            val estimatedTime: CharSequence = item.actualTime?.let { DATE_FORMAT.format(it) }
                ?: time
            bindDelay(item.delay.toLong(), estimatedTime)

            val resources = itemView.resources

            platformView?.text = item.track?.let { "Gl. $it" }


            itemView.contentDescription = resources.getString(
                R.string.sr_template_local_departure_overview,
                item.displayName, item.direction, time,
                if (item.delay > 0) resources.getString(R.string.sr_template_estimated, estimatedTime) else ""
            )

            issueIndicator?.visibleElseGone(item.partCancelled)
        }
    }

    companion object {

        val DATE_FORMAT = SimpleDateFormat("HH:mm", Locale.GERMANY)
    }
}
