/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.timetable

import android.content.Context
import android.text.format.DateUtils
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent
import de.deutschebahn.bahnhoflive.databinding.ItemMessageBinding
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.view.inflater
import java.text.DateFormat
import java.text.SimpleDateFormat

class TimetableTrailingItemViewHolder(
    private val itemMessageBinding: ItemMessageBinding,
    loadMoreCallback: View.OnClickListener? = null
) : ViewHolder<FilterSummary>(itemMessageBinding.root) {

    constructor(parent: ViewGroup, loadMoreCallback: View.OnClickListener? = null) : this(
        ItemMessageBinding.inflate(
            parent.inflater,
            parent,
            false
        ),
        loadMoreCallback
    )

    companion object {
        val timeFormat: DateFormat = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)
    }

    init {
        loadMoreCallback?.run {
            button.setOnClickListener(this)
        }
    }

    private val textView: TextView
        get() = itemMessageBinding.text

    private val button: View
        get() = itemMessageBinding.buttonLoadMore

    private val context
        get() = itemView.context

    override fun onBind(item: FilterSummary?) {
        super.onBind(item)

        item?.let {
            textView.text = context.composeMessage(it)
            button.visibility = if (it.isMayLoadMore) View.VISIBLE else View.GONE
        }
    }

    private fun Context.composeMessage(filterSummary: FilterSummary) = getString(R.string.template_empty_departures,
        if (filterSummary.matchCount > 0) getString(R.string.timetable_trailer_optional_additional) else "",
        filterSummary.trainCategory?.let { getString(R.string.template_timetable_trailer_optional_train_type, it) }
            ?: "",
        filterSummary.track?.let { getString(R.string.template_timetable_trailer_optional_platform, it) }
            ?: "",
        when (filterSummary.trainEvent) {
            TrainEvent.DEPARTURE -> getString(R.string.timetable_trailer_departure)
            TrainEvent.ARRIVAL -> getString(R.string.timetable_trailer_arrival)
        },
        timeFormat.format(filterSummary.endTime),
        if (DateUtils.isToday(filterSummary.endTime)) "" else getString(R.string.timetable_trailer_tomorrow)
    )
}