/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository

import android.content.Context
import android.content.SharedPreferences
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds
import de.deutschebahn.bahnhoflive.persistence.*
import de.deutschebahn.bahnhoflive.repository.station.UpdatedStationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ApplicationServices(
    context: Context,
    val repositories: RepositoryHolder
) {

    val recentSearchesStore by lazy { RecentSearchesStore(context, evaIdsProvider) }

    val favoriteDbStationStore by lazy {
        FavoriteStationsStore(
            context,
            "dbstations",
            InternalStationItemAdapter(evaIdsProvider)
        )
    }

    val favoriteHafasStationsStore by lazy {
        val legacyFavoriteHafasStations =
            if (favoriteStationStoreVersions.getInt("hafasFavorites", 0) < 1) {
                val legacyFavoriteHafasStationsStore =
                    FavoriteStationsStore(
                        context,
                        "hafasstations",
                        LegacyHafasStationItemAdapter()
                    )
                legacyFavoriteHafasStationsStore.all.also {
                    legacyFavoriteHafasStationsStore.clear()
                    favoriteStationStoreVersions.edit()
                        .putInt("hafasFavorites", 1)
                        .commit()
                }
            } else null

        FavoriteStationsStore(
            context,
            "hafasstations",
            HafasStationItemAdapter()
        ).apply {
            adopt(legacyFavoriteHafasStations)
        }

    }

    val favoriteStationStoreVersions: SharedPreferences by lazy {
        context.getSharedPreferences("favorite_station_store_versions.pref", Context.MODE_PRIVATE)
    }

    val mapConsentRepository by lazy {
        MapConsentRepository(context)
    }

    val updatedStationRepository by lazy {
        UpdatedStationRepository(
            repositories.stationRepository
        )
    }

    val evaIdsProvider: EvaIdsProvider by lazy {
        object : EvaIdsProvider {
            override fun withEvaIds(station: Station, action: (evaIds: EvaIds?) -> Unit) {
                GlobalScope.launch {
                    val updatedStation = updatedStationRepository.getUpdatedStation(station)

                    withContext(Dispatchers.Main) {
                        action(updatedStation?.getOrNull()?.evaIds)
                    }

                }
            }
        }
    }
}