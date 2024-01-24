/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.timetable.localtransport

import android.view.View
import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.ui.ViewHolder

internal class HafasEventViewHolder(
    parent: View,
    hafasDetailsClickEvent : (View, DetailedHafasEvent)->Unit,
    hafasDataReceivedEvent : (View, DetailedHafasEvent, Boolean)->Unit
) : ViewHolder<DetailedHafasEvent>(parent)
    , DetailedHafasEvent.HafasDetailListener {

    private val onHafasDetailsClickEvent :  (View, DetailedHafasEvent)->Unit = hafasDetailsClickEvent
    private val onHafasDataReceivedEvent :  (View, DetailedHafasEvent, Boolean)->Unit = hafasDataReceivedEvent

    private val overviewViewHolder: HafasEventOverviewViewHolder =
        HafasEventOverviewViewHolder(this@HafasEventViewHolder.itemView.findViewById(R.id.overview))

    override fun onBind(item: DetailedHafasEvent?) {
        super.onBind(item)
        item?.let {
            overviewViewHolder.bind(it.hafasEvent)
            it.setListener(this)
            overviewViewHolder.itemView.setOnClickListener {itView->
                onHafasDetailsClickEvent(itView, it)
            }
        }
    }

    fun performClick() {
        overviewViewHolder.itemView.performClick()
    }
    override fun onUnbind(item: DetailedHafasEvent) {
        super.onUnbind(item)
        item.setListener(null)
    }

    override fun onDetailUpdated(detailedHafasEvent: DetailedHafasEvent, success : Boolean ) {
        if (detailedHafasEvent === item) {
            onHafasDataReceivedEvent(itemView,detailedHafasEvent,success)
        }
    }

}
