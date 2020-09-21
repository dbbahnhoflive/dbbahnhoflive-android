/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.search

import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.StopPlace
import de.deutschebahn.bahnhoflive.persistence.FavoriteStationsStore
import de.deutschebahn.bahnhoflive.persistence.RecentSearchesStore
import de.deutschebahn.bahnhoflive.repository.InternalStation

internal class StopPlaceSearchResult(
    stopPlace: StopPlace,
    recentSearchesStore: RecentSearchesStore,
    favoriteDbStationsStore: FavoriteStationsStore<InternalStation>
) :
    DBStationSearchResult(stopPlace.asInternalStation, recentSearchesStore, favoriteDbStationsStore)
