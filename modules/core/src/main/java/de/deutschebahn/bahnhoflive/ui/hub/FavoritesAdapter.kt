/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.hub

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasTimetable
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableCollector
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.ui.search.HafasStationSearchResult
import de.deutschebahn.bahnhoflive.ui.search.SearchResult
import de.deutschebahn.bahnhoflive.ui.search.StoredStationSearchResult
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager

class FavoritesAdapter(val owner: LifecycleOwner,
                       private val trackingManager: TrackingManager,
                       private val startOrStopCyclicLoadingOfTimetable :
                           (selectedDbTimetableCollector : TimetableCollector?,
                            selectedHafasTimetable : HafasTimetable?,
                            selection:Int) -> Unit) :
    RecyclerView.Adapter<ViewHolder<out Any>>() {

    val singleSelectionManager = SingleSelectionManager(this).apply {
        addListener {
            // long click
            if (it.selection == SingleSelectionManager.INVALID_SELECTION) {
                // selection wurde eingeklappt
                startOrStopCyclicLoadingOfTimetable(null, null, selection) // permanentes laden abbrechen
                return@addListener
            }

            val selected = favorites?.get(it.selection)

            when (selected) {
                is StoredStationSearchResult ->
                    startOrStopCyclicLoadingOfTimetable(selected.timetable, null, selection)
                is HafasStationSearchResult -> {
                    selected.timetable.requestTimetable(true, "hub", true) // init

                    startOrStopCyclicLoadingOfTimetable(null,
                        selected.timetable,
                        selection) // permanentes laden (wieder) starten

                }
            }
        }
    }

    var favorites: List<SearchResult>? = null
        set(value) {
            singleSelectionManager.clearSelection()
            field = value?.sortedBy { it.title.toString() }
            notifyDataSetChanged()
        }

@Suppress("Unchecked_Cast")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<out Any> {

        return when (viewType) {
            0 -> DbDeparturesViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.card_departures, parent, false),
                owner,
                singleSelectionManager,
                trackingManager,
                null, //searchItemPickedListener,
                TrackingManager.UiElement.ABFAHRT_FAVORITEN_BHF
            ) as ViewHolder<Any>
             else -> DeparturesViewHolder(
                 LayoutInflater.from(parent.context).inflate(R.layout.card_departures, parent, false),
                owner,
                singleSelectionManager,
                trackingManager,
                null,
                TrackingManager.UiElement.ABFAHRT_FAVORITEN_OPNV
            ) as ViewHolder<Any>
        }

    }

    override fun getItemCount() = favorites?.size ?: 0

    override fun getItemViewType(position: Int): Int =
        if (favorites!![position].isLocal) 1 else 0

    override fun onBindViewHolder(holder: ViewHolder<out Any>, position: Int) {
        val searchResult = favorites?.get(position)
        when (getItemViewType(position)) {
            0 -> {
                (holder as DbDeparturesViewHolder).bind(searchResult as StoredStationSearchResult?)
            }
            1 -> (holder as DeparturesViewHolder).bind(searchResult as HafasStationSearchResult?)
        }
    }

    fun clearSelection() {
        singleSelectionManager.clearSelection()
    }
}

