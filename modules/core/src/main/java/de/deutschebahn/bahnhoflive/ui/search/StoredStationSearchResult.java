/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.search;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.persistence.FavoriteStationsStore;
import de.deutschebahn.bahnhoflive.persistence.RecentSearchesStore;
import de.deutschebahn.bahnhoflive.repository.InternalStation;
import de.deutschebahn.bahnhoflive.repository.Station;
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableCollector;
import de.deutschebahn.bahnhoflive.ui.station.StationActivity;

public class StoredStationSearchResult extends StationSearchResult<InternalStation, TimetableCollector> {
    private final TimetableCollector timetableCollector;

    @NonNull
    protected final Station station;

    public StoredStationSearchResult(@NonNull InternalStation dbStation,
                                     RecentSearchesStore recentSearchesStore,
                                     FavoriteStationsStore<InternalStation> favoriteStationsStore,
                                     TimetableCollector timetableCollector) {
        super(R.drawable.legacy_dbmappinicon, recentSearchesStore, favoriteStationsStore);
        this.timetableCollector = timetableCollector;
        station = dbStation;
    }


    @Override
    public String getTitle() {
        return station.getTitle();
    }

    @Override
    public boolean isFavorite() {
        return favoriteStationsStore.isFavorite(station.getId());
    }

    @Override
    public void setFavorite(boolean favorite) {
        if (favorite) {
            favoriteStationsStore.add(InternalStation.of(station));
        } else {
            favoriteStationsStore.remove(station.getId());
        }
    }

    @Override
    public void onClick(Context context, boolean details) {
        final Intent intent = StationActivity.createIntent(context, station, details);
        context.startActivity(intent);
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public TimetableCollector getTimetable() {
        return timetableCollector;
    }

}
