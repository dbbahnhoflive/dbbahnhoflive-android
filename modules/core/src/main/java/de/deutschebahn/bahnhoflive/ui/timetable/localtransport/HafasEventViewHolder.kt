/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.timetable.localtransport

import android.view.ViewGroup
import android.widget.ViewSwitcher
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.ui.timetable.RouteStop
import de.deutschebahn.bahnhoflive.ui.timetable.RouteStopsAdapter
import de.deutschebahn.bahnhoflive.view.SelectableItemViewHolder
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager

internal class HafasEventViewHolder(
    parent: ViewGroup,
    singleSelectionManager: SingleSelectionManager
) : SelectableItemViewHolder<DetailedHafasEvent>(
    parent,
    R.layout.card_expandable_hafas_event,
    singleSelectionManager
), DetailedHafasEvent.Listener {

    private val overviewViewHolder: HafasEventOverviewViewHolder =
        HafasEventOverviewViewHolder(this@HafasEventViewHolder.itemView.findViewById(R.id.overview))
    private val switcher: ViewSwitcher = itemView.findViewById(R.id.details)
    private val adapter: RouteStopsAdapter = RouteStopsAdapter()
    private val recyclerView: androidx.recyclerview.widget.RecyclerView = itemView.findViewById(R.id.stops_recycler)

    init {
        recyclerView.adapter = adapter
    }

    override fun onBind(item: DetailedHafasEvent) {
        super.onBind(item)
        overviewViewHolder.bind(item.hafasEvent)
        bindStops(item)
        item.setListener(this)
    }

    override fun onUnbind(item: DetailedHafasEvent) {
        super.onUnbind(item)
        item.setListener(null)
    }

    override fun onDetailUpdated(detailedHafasEvent: DetailedHafasEvent) {
        if (detailedHafasEvent === item) {
            bindStops(detailedHafasEvent)
        }
    }

    private fun bindStops(detailedHafasEvent: DetailedHafasEvent) {
        val hafasDetail = detailedHafasEvent.hafasDetail
        if (hafasDetail == null) {
            switcher.displayedChild = 0
            adapter.setRouteStops(null)
            itemView.contentDescription = null
        } else {
            switcher.displayedChild = 1

            val routeStops = ArrayList<RouteStop>()

            for (stop in hafasDetail.stops) {
                routeStops.add(RouteStop(stop.name))
            }

            if (routeStops.isNotEmpty()) {
                routeStops.first().apply {
                    isFirst = true
                    isCurrent = true
                }
                routeStops.last().isLast = true
            }

            adapter.setRouteStops(routeStops)

            with(itemView.resources) {
                itemView.contentDescription = if (isSelected)
                    getString(R.string.sr_template_local_departure_prefix,
                        detailedHafasEvent.hafasEvent.displayName, detailedHafasEvent.hafasEvent.direction,
                        routeStops.filterNot {
                            it.isFirst || it.isCurrent || it.isLast
                        }.takeUnless { it.isEmpty() }?.run {
                            getString(R.string.sr_template_local_departure_stops, joinToString(separator = ". ") { it.name.replace("/", " ") })
                        } ?: "")
                else null
            }

        }
    }
}
