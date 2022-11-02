/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui;

import androidx.annotation.NonNull;

import de.deutschebahn.bahnhoflive.persistence.FavoriteStationsStore;
import de.deutschebahn.bahnhoflive.repository.InternalStation;
import de.deutschebahn.bahnhoflive.ui.search.StoredStationSearchResult;
import kotlinx.coroutines.CoroutineScope;

public class DbStationWrapper extends StoredStationSearchResult implements StationWrapper<InternalStation> {

    private final long timestamp;

    public DbStationWrapper(InternalStation station, FavoriteStationsStore<InternalStation> favoriteStationsStore, long timestamp, CoroutineScope coroutineScope) {
        super(station, null, favoriteStationsStore, evaIdsProvider);
        this.timestamp = timestamp;
    }

    @Override
    public long getFavoriteTimestamp() {
        return timestamp;
    }

    @Override
    public boolean wraps(Object o) {
        if (o instanceof InternalStation) {
            return ((InternalStation) o).getId().equals(station.getId());
        }

        return false;
    }

    @NonNull
    @Override
    public InternalStation getWrappedStation() {
        return InternalStation.of(station);
    }

}
