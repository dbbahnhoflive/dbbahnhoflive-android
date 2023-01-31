/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.search;

import androidx.annotation.NonNull;

import de.deutschebahn.bahnhoflive.persistence.FavoriteStationsStore;
import de.deutschebahn.bahnhoflive.persistence.RecentSearchesStore;

public abstract class StationSearchResult<T, U> implements SearchResult {
    @NonNull
    protected final RecentSearchesStore recentSearchesStore;
    @NonNull
    protected final FavoriteStationsStore<T> favoriteStationsStore;

    private final int icon;

    public StationSearchResult(int icon, @NonNull RecentSearchesStore recentSearchesStore, @NonNull FavoriteStationsStore<T> favoriteStationsStore) {
        this.icon = icon;
        this.recentSearchesStore = recentSearchesStore;
        this.favoriteStationsStore = favoriteStationsStore;
    }

    @Override
    public int getIcon() {
        return icon;
    }

    public abstract U getTimetable();

    public float getDistance() { return 0.0f; }
}
