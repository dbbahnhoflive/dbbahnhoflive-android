/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.hub

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.ui.search.HafasStationSearchResult
import de.deutschebahn.bahnhoflive.ui.search.SearchResult
import de.deutschebahn.bahnhoflive.ui.search.StoredStationSearchResult
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager

class FavoritesAdapter(val owner: LifecycleOwner, private val trackingManager: TrackingManager) : androidx.recyclerview.widget.RecyclerView.Adapter<ViewHolder<out Any>>() {

    val singleSelectionManager = SingleSelectionManager(this).apply {
        addListener {
            if (it.selection == SingleSelectionManager.INVALID_SELECTION) {
                return@addListener
            }

            val searchResult = favorites?.get(it.selection)
            when (searchResult) {
                is StoredStationSearchResult -> searchResult.timetable.loadIfNecessary()
                is HafasStationSearchResult -> searchResult.timetable.requestTimetable(true, "hub")
            }
        }
    }

    var favorites: List<SearchResult>? = null
        set(value) {
            singleSelectionManager.clearSelection()
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<out Any> = when (viewType) {
        0 -> DbDeparturesViewHolder(
            parent,
            singleSelectionManager,
            owner,
            trackingManager,
            null,
            TrackingManager.UiElement.ABFAHRT_FAVORITEN_BHF
        )
        else -> DeparturesViewHolder(
            parent,
            owner,
            singleSelectionManager,
            trackingManager,
            null,
            TrackingManager.UiElement.ABFAHRT_FAVORITEN_OPNV
        )
    }

    override fun getItemCount() = favorites?.size ?: 0

    override fun getItemViewType(position: Int): Int =
        if (favorites!![position].isLocal) 1 else 0

    override fun onBindViewHolder(holder: ViewHolder<out Any>, position: Int) {
        val searchResult = favorites?.get(position)
        when (getItemViewType(position)) {
            0 -> (holder as DbDeparturesViewHolder).bind(searchResult as StoredStationSearchResult?)
            1 -> (holder as DeparturesViewHolder).bind(searchResult as HafasStationSearchResult?)
        }
    }

    fun clearSelection() {
        singleSelectionManager.clearSelection()
    }
}

