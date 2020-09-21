/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.search;

import de.deutschebahn.bahnhoflive.persistence.FavoriteStationsStore;
import de.deutschebahn.bahnhoflive.persistence.RecentSearchesStore;

abstract class StationSearchResult<T, U> implements SearchResult {
    protected final RecentSearchesStore recentSearchesStore;
    protected final FavoriteStationsStore<T> favoriteStationsStore;

    private final int icon;

    StationSearchResult(int icon, RecentSearchesStore recentSearchesStore, FavoriteStationsStore<T> favoriteStationsStore) {
        this.icon = icon;
        this.recentSearchesStore = recentSearchesStore;
        this.favoriteStationsStore = favoriteStationsStore;
    }

    @Override
    public int getIcon() {
        return icon;
    }

    public abstract U getTimetable();
}
