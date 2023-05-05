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
import de.deutschebahn.bahnhoflive.repository.DbTimetableResource
import de.deutschebahn.bahnhoflive.repository.InternalStation
import de.deutschebahn.bahnhoflive.ui.station.StationActivity

class StopPlaceSearchResult(
    val stopPlace: StopPlace,
    val recentSearchesStore: RecentSearchesStore,
    val favoriteStationsStore: FavoriteStationsStore<InternalStation>
) : StationSearchResult<InternalStation, DbTimetableResource>(
    R.drawable.legacy_dbmappinicon,
    recentSearchesStore,
    favoriteStationsStore
) {

    private val dbTimetableResource: DbTimetableResource =
        DbTimetableResource(stopPlace.asInternalStation, stopPlace)

    override fun getTitle(): CharSequence {
        return dbTimetableResource.getStationName()
    }

    override fun isFavorite(): Boolean {
        return favoriteStationsStore.isFavorite(dbTimetableResource.getStationId())
    }

    override fun setFavorite(favorite: Boolean) {
        if (favorite) {
            favoriteStationsStore.add(InternalStation.of(dbTimetableResource.station))
        } else {
            favoriteStationsStore.remove(dbTimetableResource.getStationId())
        }
    }

    override fun onClick(context: Context, details: Boolean) {
        recentSearchesStore.put(dbTimetableResource.internalStation)
        val intent =
            StationActivity.createIntent(context, dbTimetableResource.getInternalStation(), details)
        context.startActivity(intent)
    }

    override fun isLocal(): Boolean {
        return false
    }

    override fun getTimetable(): DbTimetableResource {
        return dbTimetableResource
    }
}
