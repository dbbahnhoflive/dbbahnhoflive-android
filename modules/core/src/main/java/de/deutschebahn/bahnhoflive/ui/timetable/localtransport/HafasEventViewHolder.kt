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
import de.deutschebahn.bahnhoflive.ui.timetable.RouteStopsAdapter
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager

internal class HafasEventViewHolder(
    parent: ViewGroup,
    hafasDetailsClickEvent : (View, DetailedHafasEvent)->Unit,
    hafasDataReceivedEvent : (View, DetailedHafasEvent, Boolean)->Unit,
    singleSelectionManager: SingleSelectionManager
)
    : ViewHolder<DetailedHafasEvent>(parent,R.layout.card_expandable_hafas_event )
    ,
    DetailedHafasEvent.HafasDetailListener {

    private val onHafasDetailsClickEvent :  (View, DetailedHafasEvent)->Unit = hafasDetailsClickEvent
    private val onHafasDataReceivedEvent :  (View, DetailedHafasEvent, Boolean)->Unit = hafasDataReceivedEvent

    private val overviewViewHolder: HafasEventOverviewViewHolder =
        HafasEventOverviewViewHolder(this@HafasEventViewHolder.itemView.findViewById(R.id.overview))
    private val adapter: RouteStopsAdapter = RouteStopsAdapter()

    override fun onBind(item: DetailedHafasEvent) {
        super.onBind(item)
        overviewViewHolder.bind(item.hafasEvent)
        bindStops(item)
        item.setListener(this)
        overviewViewHolder.itemView.setOnClickListener {
            onHafasDetailsClickEvent(it, item)
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

    private fun bindStops(detailedHafasEvent: DetailedHafasEvent) {
//        val hafasDetail = detailedHafasEvent.hafasDetail
//        if (hafasDetail == null) {
//            adapter.setRouteStops(null)
//            itemView.contentDescription = null
//        } else {
//
//            val routeStops = ArrayList<RouteStop>()
//
//            for (stop in hafasDetail.stops) {
//                routeStops.add(RouteStop(stop))
//            }
//
//            if (routeStops.isNotEmpty()) {
//                routeStops.first().apply {
//                    isFirst = true
//                    isCurrent = true
//                }
//                routeStops.last().isLast = true
//            }
//
//            adapter.setRouteStops(routeStops)
//
//            onHafasDataReceivedEvent(itemView,detailedHafasEvent, true)
//
//        }
    }
}
