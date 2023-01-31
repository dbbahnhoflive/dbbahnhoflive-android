/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.hub

import android.util.Log
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.db.ris.model.StopPlace
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation
import de.deutschebahn.bahnhoflive.backend.toHafasStation
import de.deutschebahn.bahnhoflive.persistence.FavoriteStationsStore
import de.deutschebahn.bahnhoflive.persistence.RecentSearchesStore
import de.deutschebahn.bahnhoflive.repository.InternalStation
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableRepository
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.ui.search.HafasStationSearchResult
import de.deutschebahn.bahnhoflive.ui.search.StopPlaceSearchResult
import de.deutschebahn.bahnhoflive.ui.station.timetable.TimetableCollectorConnector
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager
import kotlinx.coroutines.CoroutineScope

internal class NearbyDeparturesAdapter(
    private val coroutineScope: CoroutineScope,
    private val owner: LifecycleOwner,
    private val recentSearchesStore: RecentSearchesStore,
    private val favoriteHafasStationsStore: FavoriteStationsStore<HafasStation>,
    private val favoriteStationsStore: FavoriteStationsStore<InternalStation>,
    private val timetableRepository: TimetableRepository,
    val trackingManager: TrackingManager
) : androidx.recyclerview.widget.RecyclerView.Adapter<ViewHolder<*>>() {

    private val singleSelectionManager: SingleSelectionManager =
        SingleSelectionManager(this).apply {
            addListener(SingleSelectionManager.Listener { selectionManager ->
                val selection = selectionManager.selection

                if (selection == SingleSelectionManager.INVALID_SELECTION) {
                    return@Listener
                }

                val selected = items?.get(selection)

                when (selected) {
                    is NearbyDbStationItem -> {

                        selected.timetableCollectorConnector?.setStationAndRequestDestinationStations(selected.station, onTimetableReceived = {
                            Log.d("cr", "onTimetableReceived")
                            notifyDataSetChanged()
//                            selected.onLoadDetails()

                        })

                    }
                    is NearbyHafasStationItem -> {

                        selected?.onLoadDetails()
                    }
            }
            })

        }

    private var items: List<NearbyStationItem>? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<*> =
        when (viewType) {
            1 -> NearbyDeparturesViewHolder(parent, owner, singleSelectionManager, trackingManager)
            else -> NearbyDbDeparturesViewHolder(
                parent,
                singleSelectionManager,
                owner,
                trackingManager
            )
        }

    override fun onBindViewHolder(holder: ViewHolder<*>, position: Int) {
        val item = items?.get(position)

        item?.bindViewHolder(holder)
    }

    override fun getItemViewType(position: Int) = items?.get(position)?.type ?: 0

    override fun getItemCount() = items?.size ?: 0

    fun clearSelection() {
        singleSelectionManager.clearSelection()
    }

    fun setData(stopPlaces: List<StopPlace>?) {
        clearSelection()

        items = stopPlaces?.mapNotNull { stopPlace ->
            when {
                stopPlace.isDbStation -> {

                    val timetableCollectorConnector =  TimetableCollectorConnector(owner) // neue Instanz

                    NearbyDbStationItem(
                        StopPlaceSearchResult(
                            coroutineScope,
                            stopPlace,
                            recentSearchesStore,
                            favoriteStationsStore,
                            timetableRepository,
                            timetableCollectorConnector
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

        notifyDataSetChanged()

    }

    companion object {
        val TAG = NearbyDeparturesAdapter::class.java.simpleName
    }
}

interface NearbyStationItem {
    val type: Int

    val distance: Float

    fun bindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder)

    fun onLoadDetails()
}