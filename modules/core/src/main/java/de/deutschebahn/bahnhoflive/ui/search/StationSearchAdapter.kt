/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import de.deutschebahn.bahnhoflive.BaseApplication.Companion.get
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.db.ris.model.StopPlace
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation
import de.deutschebahn.bahnhoflive.backend.toHafasStation
import de.deutschebahn.bahnhoflive.persistence.FavoriteStationsStore
import de.deutschebahn.bahnhoflive.persistence.RecentSearchesStore
import de.deutschebahn.bahnhoflive.repository.InternalStation
import de.deutschebahn.bahnhoflive.repository.timetable.OnStartStopCyclicLoadingOfTimetableListener
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableCollector
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableRepository
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.ui.hub.DbDeparturesViewHolder
import de.deutschebahn.bahnhoflive.ui.hub.DeparturesViewHolder
import de.deutschebahn.bahnhoflive.ui.hub.HubViewModel
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager
import kotlinx.coroutines.CoroutineScope

internal class StationSearchAdapter(
    context: FragmentActivity,
    private val recentSearchesStore: RecentSearchesStore,
    private val searchItemPickedListener: SearchItemPickedListener,
    private val owner: LifecycleOwner,
    private val trackingManager: TrackingManager,
    private val coroutineScope: CoroutineScope,
    private val timetableRepository: TimetableRepository,
    onStartOrStopCyclicLoadingOfTimetableListener: OnStartStopCyclicLoadingOfTimetableListener
) : RecyclerView.Adapter<ViewHolder<*>>() {
    private val singleSelectionManager = SingleSelectionManager(this)
    private val favoriteDbStationsStore: FavoriteStationsStore<InternalStation>
    private val favoriteHafasStationsStore: FavoriteStationsStore<HafasStation>
    private val hubViewModel: HubViewModel

    private var dbStations: List<StopPlace>? = null
//    private var hafasStations: List<HafasStation>? = null

    private var searchResults: MutableList<SearchResult> = mutableListOf()

    private var hafasError = false
    private var dbError = false

    init {
        hubViewModel = ViewModelProvider(context)[HubViewModel::class.java]
        favoriteDbStationsStore = get().applicationServices.favoriteDbStationStore
        favoriteHafasStationsStore = get().applicationServices.favoriteHafasStationsStore
        singleSelectionManager.addListener { selectionManager: SingleSelectionManager ->
            val selectedItem = selectionManager.getSelectedItem(searchResults)

            // long click
            val selection = selectionManager.selection
            if (selection == SingleSelectionManager.INVALID_SELECTION) {
                // selection wurde eingeklappt
                onStartOrStopCyclicLoadingOfTimetableListener.onStartStopCyclicLoading(
                    null, null, selection
                ) // permanentes laden abbrechen
            } else if (selectedItem is StoredStationSearchResult) {
                // long click on stored item
                onStartOrStopCyclicLoadingOfTimetableListener.onStartStopCyclicLoading(
                    selectedItem.timetable,
                    null,
                    selection
                )
            } else if (selectedItem is StopPlaceSearchResult) {
                // long click on typed searchresult
                onStartOrStopCyclicLoadingOfTimetableListener.onStartStopCyclicLoading(
                    selectedItem.timetable,
                    null,
                    selection
                )
            } else if (selectedItem is HafasStationSearchResult) {
                selectedItem.timetable.requestTimetable(true, "search", true)
                onStartOrStopCyclicLoadingOfTimetableListener.onStartStopCyclicLoading(
                    null,
                    selectedItem.timetable,
                    selection
                )
            }
        }
        showRecents(coroutineScope, timetableRepository)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<*> {
        return when (viewType) {
            0, 2 -> DbDeparturesViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_departures, parent, false),
                owner,
                singleSelectionManager,
                trackingManager,
                searchItemPickedListener,
                TrackingManager.UiElement.ABFAHRT_SUCHE_BHF
            )

            1 -> DeparturesViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_departures, parent, false),
                owner, singleSelectionManager, trackingManager,
                searchItemPickedListener, TrackingManager.UiElement.ABFAHRT_SUCHE_OPNV
            )

            else -> StationSearchViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_station_suggestion, parent, false)
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        val searchResult = searchResults[position]
        if (searchResult is StoredStationSearchResult) {
            return 0
        }
        if (searchResult is HafasStationSearchResult) {
            return 1
        }
        return if (searchResult is StopPlaceSearchResult) {
            2
        } else 3
    }

    override fun onBindViewHolder(holder: ViewHolder<*>, position: Int) {
        val searchResult = searchResults[position]

        when{
            (searchResult is StoredStationSearchResult)->
                (holder as DbDeparturesViewHolder).bind(searchResult)

            (searchResult is HafasStationSearchResult)->
                (holder as DeparturesViewHolder).bind(searchResult)

            (searchResult is StopPlaceSearchResult)-> {
                @Suppress("Unchecked_Cast")
                (holder as DbDeparturesViewHolder).bind(searchResult as StationSearchResult<InternalStation?, TimetableCollector?>)
            }
            else ->
              (holder as StationSearchViewHolder).bind(searchResult)
        }


    }

    override fun getItemCount(): Int {
        return searchResults.size
    }



    private fun updateItems() {
        singleSelectionManager.clearSelection()
        searchResults.clear()

        dbStations?.let {
            for (dbStation in it) {
                val searchResult: SearchResult = if (dbStation.isDbStation) {
                    StopPlaceSearchResult(
                        coroutineScope, dbStation,
                        recentSearchesStore, favoriteDbStationsStore, timetableRepository
                    )
                } else {
                    val hafasStation = dbStation.toHafasStation()
                    HafasStationSearchResult(
                        hafasStation,
                        recentSearchesStore,
                        favoriteHafasStationsStore
                    )
                }
                searchResults.add(searchResult)
            }
        }

//        hafasStations?.let {
//            for (hafasStation in it) {
//                searchResults.add(
//                    HafasStationSearchResult(
//                        hafasStation,
//                        recentSearchesStore,
//                        favoriteHafasStationsStore
//                    )
//                )
//            }
//        }
        notifyDataSetChanged()
    }

    fun showRecents(coroutineScope: CoroutineScope?, timetableRepository: TimetableRepository?) {
        singleSelectionManager.clearSelection()
        searchResults.clear()
        searchResults.addAll(recentSearchesStore.loadRecentStations(coroutineScope, timetableRepository!!))

//            recentSearchesStore.loadRecentStations(coroutineScope, timetableRepository!!)
        notifyDataSetChanged()
    }


//    fun setHafasStations(stations: List<HafasStation>?) {
//        hafasStations = stations
//        hafasError = false
//        updateItems()
//    }
//
//    fun setHafasError() {
//        hafasError = true
//        updateItems()
//    }

    fun setDBStations(stations: List<StopPlace>?) {
        dbError = false
        dbStations = stations
        updateItems()
    }

    fun setDBError() {
        dbError = true
        updateItems()
    }

    fun hasErrors(): Boolean {
        return dbError || hafasError
    }

    fun clearSelection() {
        singleSelectionManager.clearSelection()
    }
}
