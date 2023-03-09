/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.search

import android.content.Context
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.db.ris.model.StopPlace
import de.deutschebahn.bahnhoflive.persistence.FavoriteStationsStore
import de.deutschebahn.bahnhoflive.persistence.RecentSearchesStore
import de.deutschebahn.bahnhoflive.repository.InternalStation
import de.deutschebahn.bahnhoflive.repository.Station
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableCollector
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableRepository
import de.deutschebahn.bahnhoflive.ui.station.StationActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow

class StopPlaceSearchResult(
    val coroutineScope: CoroutineScope,
    val stopPlace: StopPlace,
    val recentSearchesStore: RecentSearchesStore,
    val favoriteStationsStore: FavoriteStationsStore<InternalStation>,
    val timetableRepository: TimetableRepository
) : StationSearchResult<InternalStation, TimetableCollector>(
    R.drawable.legacy_dbmappinicon,
    recentSearchesStore,
    favoriteStationsStore
) {

    private val internalStation = stopPlace.asInternalStation

    private val timetableCollector = timetableRepository.createTimetableCollector(
                flow { emit(stopPlace.evaIds) }, coroutineScope
            )


    override fun getTitle(): CharSequence {
        return stopPlace.name ?: ""
    }

    override fun isFavorite(): Boolean {
        return stopPlace.stationID?.let {
            favoriteStationsStore.isFavorite(it)
        } ?: false
    }

    override fun setFavorite(favorite: Boolean) {
        if (favorite) {
            favoriteStationsStore.add(internalStation)
        } else {
            internalStation?.id.let {
                favoriteStationsStore.remove(it)
            }
        }
    }

    override fun onClick(context: Context, details: Boolean) {
        val intent =
            StationActivity.createIntent(context, internalStation, details)
        context.startActivity(intent)
        recentSearchesStore.put(internalStation)
    }

    override fun isLocal(): Boolean {
        return false
    }

    override fun getTimetable(): TimetableCollector {
        return timetableCollector
        }

        fun getStation(): Station {
            return internalStation as Station
        }

    override fun getDistance(): Float {
        if(stopPlace.distanceInKm!=null)
          return stopPlace.distanceInKm!!
        else
            return 0.0f
    }
}
