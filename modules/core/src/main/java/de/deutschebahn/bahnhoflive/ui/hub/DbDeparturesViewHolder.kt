/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.hub

import android.view.View
import android.view.ViewGroup
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
    parent: ViewGroup?,
    layout: Int,
    singleSelectionManager: SingleSelectionManager?,
    owner: LifecycleOwner?,
    private val trackingManager: TrackingManager,
    private val searchItemPickedListener: SearchItemPickedListener?,
    private val itemTag: String? = null
) : LongClickSelectableItemViewHolder<StationSearchResult<InternalStation?, TimetableCollector?>?>(
    parent,
    layout,
    singleSelectionManager
), View.OnClickListener {
    private val reducedDbDeparturesViewHolder: ReducedDbDeparturesViewHolder
    private val stationSearchViewHolder: StationSearchViewHolder = StationSearchViewHolder(itemView)
//    private val itemTag: String?

    constructor(
        parent: ViewGroup?,
        singleSelectionManager: SingleSelectionManager?,
        owner: LifecycleOwner?,
        trackingManager: TrackingManager,
        searchItemPickedListener: SearchItemPickedListener?,
        itemTag: String?
    ) : this(
        parent,
        R.layout.card_departures,
        singleSelectionManager,
        owner,
        trackingManager,
        searchItemPickedListener,
        itemTag
    )

    init {
//        itemView.setOnClickListener(this)
//        itemView.findViewById<View>(R.id.details).setOnClickListener(this)
        reducedDbDeparturesViewHolder =
            ReducedDbDeparturesViewHolder(itemView, R.id.view_flipper, owner)
//        this.itemTag = itemTag
    }

    override fun onBind(item: StationSearchResult<InternalStation?, TimetableCollector?>?) {
        super.onBind(item)
        item?.let {
            stationSearchViewHolder.bind(it)
            reducedDbDeparturesViewHolder.bind(it.timetable?.timetableStateFlow?.value)
        }
        itemView.setOnClickListener(this)
        itemView.findViewById<View>(R.id.details).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        trackingManager.track(
            TrackingManager.TYPE_ACTION,
            TrackingManager.Screen.H0,
            TrackingManager.Action.TAP,
            itemTag
        )
        searchItemPickedListener?.onSearchItemPicked()
        val context = v.context
        val item: StationSearchResult<InternalStation?, TimetableCollector?>? = item
        item?.onClick(context, v !== itemView)
    }

    companion object {
        val TAG: String = DbDeparturesViewHolder::class.java.simpleName
    }
}
