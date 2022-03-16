/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository

import android.content.Context
import android.content.SharedPreferences
import de.deutschebahn.bahnhoflive.persistence.*

class ApplicationServices(context: Context) {

    val recentSearchesStore by lazy { RecentSearchesStore(context) }

    val favoriteDbStationStore by lazy {
        FavoriteStationsStore(
            context,
            "dbstations",
            InternalStationItemAdapter()
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

}