/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.hub

import android.view.View
import androidx.lifecycle.LifecycleOwner
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.ui.search.HafasStationSearchResult
import de.deutschebahn.bahnhoflive.ui.search.SearchItemPickedListener
import de.deutschebahn.bahnhoflive.ui.search.StationSearchViewHolder
import de.deutschebahn.bahnhoflive.ui.timetable.localtransport.ReducedHafasDeparturesViewHolder
import de.deutschebahn.bahnhoflive.view.LongClickSelectableItemViewHolder
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager

open class DeparturesViewHolder(
    view: View,
    owner: LifecycleOwner?,
    singleSelectionManager: SingleSelectionManager?,
    private val trackingManager: TrackingManager,
    private val searchItemPickedListener: SearchItemPickedListener?,
    private val itemTag: String?
) : LongClickSelectableItemViewHolder<HafasStationSearchResult?>(
    view,
    singleSelectionManager
) {

    private val reducedHafasDeparturesViewHolder: ReducedHafasDeparturesViewHolder
    private val stationSearchViewHolder: StationSearchViewHolder = StationSearchViewHolder(itemView)

    val  onClickListener = View.OnClickListener   {itView->
        trackingManager.track(
            TrackingManager.TYPE_ACTION,
            TrackingManager.Screen.H0,
            TrackingManager.Action.TAP,
            itemTag
        )
        searchItemPickedListener?.onSearchItemPicked()
        item?.onClick(itView.context, itView !== itemView)
    }

    init {
        itemView.setOnClickListener(onClickListener)
        itemView.findViewById<View>(R.id.details).setOnClickListener(onClickListener)
        reducedHafasDeparturesViewHolder = ReducedHafasDeparturesViewHolder(itemView, owner)
    }

    override fun onBind(item: HafasStationSearchResult?) {
        super.onBind(item)
        item?.let {
            stationSearchViewHolder.bind(it)
            reducedHafasDeparturesViewHolder.bind(it.timetable.resource)
        }
    }

    companion object {
        val TAG: String = DeparturesViewHolder::class.java.simpleName
    }
}
