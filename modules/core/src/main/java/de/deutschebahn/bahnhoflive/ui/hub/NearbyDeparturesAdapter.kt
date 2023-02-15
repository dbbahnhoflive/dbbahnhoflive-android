/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.hub

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.db.ris.model.StopPlace
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation
import de.deutschebahn.bahnhoflive.backend.toHafasStation
import de.deutschebahn.bahnhoflive.persistence.FavoriteStationsStore
import de.deutschebahn.bahnhoflive.persistence.RecentSearchesStore
import de.deutschebahn.bahnhoflive.repository.InternalStation
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableCollector
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableRepository
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.ui.search.HafasStationSearchResult
import de.deutschebahn.bahnhoflive.ui.search.StopPlaceSearchResult
import de.deutschebahn.bahnhoflive.view.BaseItemCallback
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager
import kotlinx.coroutines.CoroutineScope

internal class NearbyDeparturesAdapter(
    private val coroutineScope: CoroutineScope,
    private val owner: LifecycleOwner,
    private val recentSearchesStore: RecentSearchesStore,
    private val favoriteHafasStationsStore: FavoriteStationsStore<HafasStation>,
    private val favoriteStationsStore: FavoriteStationsStore<InternalStation>,
    private val timetableRepository: TimetableRepository,
    val trackingManager: TrackingManager,
    loadNextDeparturesCallback: (timetableCollector: TimetableCollector?, selection: Int) -> Unit
) : ListAdapter<NearbyStationItem, RecyclerView.ViewHolder>(
    object : BaseItemCallback<NearbyStationItem>() {
        override fun areItemsTheSame(
            oldItem: NearbyStationItem,
            newItem: NearbyStationItem
        ) = oldItem == newItem
    }
) {

    private val singleSelectionManager: SingleSelectionManager =
        SingleSelectionManager(this).apply {
            addListener(SingleSelectionManager.Listener { selectionManager ->
                val selection = selectionManager.selection

                if (selection == SingleSelectionManager.INVALID_SELECTION) {
                    loadNextDeparturesCallback(null, selection)
                    return@Listener
                }

                when (val selected = currentList[selection]) {
                    is NearbyDbStationItem -> {
                        loadNextDeparturesCallback(selected.dbStationSearchResult.timetable.first, selection)
                    }
                    is NearbyHafasStationItem -> {
                        selected.onLoadDetails()
                    }
                }
            })

        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<*> =
        when (viewType) {
            1 -> NearbyDeparturesViewHolder(parent, owner, singleSelectionManager, trackingManager)
            else -> NearbyDbDeparturesViewHolder(
                parent,
                owner,
                singleSelectionManager,
                trackingManager
            )
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = currentList[position]

        item?.bindViewHolder(holder)
    }

    override fun getItemViewType(position: Int) = currentList[position]?.type ?: 0

    override fun getItemCount() = currentList.size

    fun clearSelection() {
        singleSelectionManager.clearSelection()
    }

    fun setData(stopPlaces: List<StopPlace>?) {
        clearSelection()


        submitList(stopPlaces?.mapNotNull { stopPlace ->
            when {
                stopPlace.isDbStation -> {

//                    val timetableCollectorConnector =
//                        TimetableCollectorConnector(owner) // neue Instanz

                    NearbyDbStationItem(
                        StopPlaceSearchResult(
                            coroutineScope,
                            stopPlace,
                            recentSearchesStore,
                            favoriteStationsStore,
                            timetableRepository
                        )
                    )
                }
                stopPlace.isLocalTransportStation -> {
                    NearbyHafasStationItem(
                        HafasStationSearchResult(
                            stopPlace.toHafasStation(),
                            recentSearchesStore,
                            favoriteHafasStationsStore
                        )
                    )
                }
                else -> null
            }
        }
        )

    }

    companion object {
        val TAG = NearbyDeparturesAdapter::class.java.simpleName
    }
}

interface NearbyStationItem {
    val type: Int

    val distance: Float

    fun bindViewHolder(holder: RecyclerView.ViewHolder)

    fun onLoadDetails()
}