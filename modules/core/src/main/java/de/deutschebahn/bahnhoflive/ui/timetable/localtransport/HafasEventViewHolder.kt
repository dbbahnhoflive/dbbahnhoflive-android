/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.timetable.localtransport

import android.view.View
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.ui.ViewHolder

internal class HafasEventViewHolder(
    parent: View,
    hafasDetailsClickEvent : (View, DetailedHafasEvent)->Unit,
    private val listener : DetailedHafasEvent.HafasDetailListener
) : ViewHolder<DetailedHafasEvent>(parent) {

    private val onHafasDetailsClickEvent :  (View, DetailedHafasEvent)->Unit = hafasDetailsClickEvent

    private val overviewViewHolder: HafasEventOverviewViewHolder =
        HafasEventOverviewViewHolder(this@HafasEventViewHolder.itemView.findViewById(R.id.overview))

    override fun onBind(item: DetailedHafasEvent?) {
        super.onBind(item)
        item?.let {
            overviewViewHolder.bind(it.hafasEvent)
            it.setListener(listener)
            overviewViewHolder.itemView.setOnClickListener {itView->
                onHafasDetailsClickEvent(itView, it)
            }
        }
    }

    override fun onUnbind(item: DetailedHafasEvent) {
        super.onUnbind(item)
        item.setListener(null)
    }


}
