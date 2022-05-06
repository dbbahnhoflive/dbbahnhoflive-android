/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.search;

import android.content.Context;
import android.content.Intent;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.persistence.FavoriteStationsStore;
import de.deutschebahn.bahnhoflive.persistence.RecentSearchesStore;
import de.deutschebahn.bahnhoflive.repository.DbTimetableResource;
import de.deutschebahn.bahnhoflive.repository.InternalStation;
import de.deutschebahn.bahnhoflive.ui.station.StationActivity;

public class StoredStationSearchResult extends StationSearchResult<InternalStation, DbTimetableResource> {
    private final DbTimetableResource dbTimetableResource;

    public StoredStationSearchResult(InternalStation dbStation, RecentSearchesStore recentSearchesStore, FavoriteStationsStore<InternalStation> favoriteStationsStore) {
        super(R.drawable.legacy_dbmappinicon, recentSearchesStore, favoriteStationsStore);
        this.dbTimetableResource = new DbTimetableResource(dbStation);
    }

    @Override
    public CharSequence getTitle() {
        return dbTimetableResource.getStationName();
    }

    @Override
    public boolean isFavorite() {
        return favoriteStationsStore.isFavorite(dbTimetableResource.getStationId());
    }

    @Override
    public void setFavorite(boolean favorite) {
        if (favorite) {
            favoriteStationsStore.add(dbTimetableResource.getInternalStation());
        } else {
            favoriteStationsStore.remove(dbTimetableResource.getStationId());
        }
    }

    @Override
    public void onClick(Context context, boolean details) {
        final Intent intent = StationActivity.createIntent(context, dbTimetableResource.getInternalStation(), details);
        context.startActivity(intent);
        if (recentSearchesStore != null) {
            recentSearchesStore.put(dbTimetableResource.getInternalStation());
        }
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public DbTimetableResource getTimetable() {
        return dbTimetableResource;
    }
}
