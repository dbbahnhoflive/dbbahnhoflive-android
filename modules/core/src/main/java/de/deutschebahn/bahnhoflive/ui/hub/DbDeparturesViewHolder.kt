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
import de.deutschebahn.bahnhoflive.repository.InternalStation
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableCollector
import de.deutschebahn.bahnhoflive.ui.search.SearchItemPickedListener
import de.deutschebahn.bahnhoflive.ui.search.StationSearchResult
import de.deutschebahn.bahnhoflive.ui.search.StationSearchViewHolder
import de.deutschebahn.bahnhoflive.ui.timetable.localtransport.ReducedDbDeparturesViewHolder
import de.deutschebahn.bahnhoflive.view.LongClickSelectableItemViewHolder
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager

open class DbDeparturesViewHolder internal constructor(
    view: View,
    owner: LifecycleOwner?,
    singleSelectionManager: SingleSelectionManager?,
    private val trackingManager: TrackingManager,
    private val searchItemPickedListener: SearchItemPickedListener?,
    private val itemTag: String?
) : LongClickSelectableItemViewHolder<StationSearchResult<InternalStation?, TimetableCollector?>?>(view, singleSelectionManager) {

    private val reducedDbDeparturesViewHolder: ReducedDbDeparturesViewHolder
    private val stationSearchViewHolder: StationSearchViewHolder = StationSearchViewHolder(itemView)

    private val onClickListener = View.OnClickListener {
        trackingManager.track(
            TrackingManager.TYPE_ACTION,
            TrackingManager.Screen.H0,
            TrackingManager.Action.TAP,
        itemTag
    )
        searchItemPickedListener?.onSearchItemPicked()
        item?.onClick(it.context, it !== itemView)
    }

    init {
        itemView.setOnClickListener(onClickListener)
        itemView.findViewById<View>(R.id.details)?.setOnClickListener(onClickListener)
        reducedDbDeparturesViewHolder =
            ReducedDbDeparturesViewHolder(itemView, R.id.view_flipper, owner)
    }

    override fun onBind(item: StationSearchResult<InternalStation?, TimetableCollector?>?) {
        super.onBind(item)
        stationSearchViewHolder.bind(item)
        item?.let {
            reducedDbDeparturesViewHolder.bind(item.timetable?.timetableStateFlow?.value)
        }
    }

    companion object {
        val TAG: String = DbDeparturesViewHolder::class.java.simpleName
    }
}
