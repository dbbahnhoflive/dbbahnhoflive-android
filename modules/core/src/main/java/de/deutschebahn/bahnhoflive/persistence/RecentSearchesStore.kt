/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.persistence

import android.content.Context
import de.deutschebahn.bahnhoflive.BaseApplication.Companion.get
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds
import de.deutschebahn.bahnhoflive.repository.InternalStation
import de.deutschebahn.bahnhoflive.repository.Station
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableRepository
import de.deutschebahn.bahnhoflive.ui.StationWrapper
import de.deutschebahn.bahnhoflive.ui.search.HafasStationSearchResult
import de.deutschebahn.bahnhoflive.ui.search.SearchResult
import de.deutschebahn.bahnhoflive.ui.search.StoredStationSearchResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.Continuation

class RecentSearchesStore(context: Context?) {
    private val favoriteStationsStore: FavoriteStationsStore<InternalStation>
    private val recentDbStationsStore: FavoriteStationsStore<InternalStation>
    private val favoriteHafasStationsStore: FavoriteStationsStore<HafasStation>
    private val recentHafasStationsStore: FavoriteStationsStore<HafasStation>

    init {
        val applicationServices = get().applicationServices
        favoriteStationsStore = applicationServices.favoriteDbStationStore
        recentDbStationsStore =
            FavoriteStationsStore(context, "recent_dbstations", InternalStationItemAdapter())
        favoriteHafasStationsStore = applicationServices.favoriteHafasStationsStore
        val favoriteStationStoreVersions = applicationServices.favoriteStationStoreVersions
        val legacyRecentHafasStations: List<StationWrapper<HafasStation>>?
        if (favoriteStationStoreVersions.getInt("hafasRecents", 0) < 1) {
            val legacyRecentHafasStationsStore = FavoriteStationsStore(
                context,
                "recent_hafasstations",
                LegacyHafasStationItemAdapter()
            )
            legacyRecentHafasStations = legacyRecentHafasStationsStore.all
            legacyRecentHafasStationsStore.clear()
            favoriteStationStoreVersions.edit()
                .putInt("hafasRecents", 1)
                .commit()
        } else {
            legacyRecentHafasStations = null
        }
        recentHafasStationsStore =
            FavoriteStationsStore(context, "recent_hafasstations", HafasStationItemAdapter())
        recentHafasStationsStore.adopt(legacyRecentHafasStations)
    }

    fun loadRecentStations(
        coroutineScope: CoroutineScope?,
        timetableRepository: TimetableRepository
    ): List<SearchResult> {
        val all = recentDbStationsStore.all
        val allHafas = recentHafasStationsStore.all
        val searchResults = ArrayList<SearchResult>()
        for (stationWrapper in all) {
            searchResults.add(
                StoredStationSearchResult(stationWrapper.wrappedStation,
                    this,
                    favoriteStationsStore,
                    coroutineScope?.let {itCoroutineScope ->
                        timetableRepository.createTimetableCollector(
                            flow {
                                stationWrapper.wrappedStation.evaIds?.let {itEvaIds->
                                  emit(itEvaIds)
                                }
                            }, itCoroutineScope
                        )
                    }
                )
            )
        }

        for (hafasStationWrapper in allHafas) {
            searchResults.add(
                HafasStationSearchResult(
                    hafasStationWrapper.wrappedStation,
                    this,
                    favoriteHafasStationsStore
                )
            )
        }
        return searchResults
    }

    fun put(station: Station?) {
        recentDbStationsStore.add(InternalStation.of(station))
    }

    fun putHafasStation(hafasStation: HafasStation) {
        recentHafasStationsStore.add(hafasStation)
    }

    fun clear() {
        recentDbStationsStore.clear()
        recentHafasStationsStore.clear()
    }
}