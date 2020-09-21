/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

import de.deutschebahn.bahnhoflive.BaseApplication;
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation;
import de.deutschebahn.bahnhoflive.repository.ApplicationServices;
import de.deutschebahn.bahnhoflive.repository.InternalStation;
import de.deutschebahn.bahnhoflive.repository.Station;
import de.deutschebahn.bahnhoflive.ui.StationWrapper;
import de.deutschebahn.bahnhoflive.ui.search.DBStationSearchResult;
import de.deutschebahn.bahnhoflive.ui.search.HafasStationSearchResult;
import de.deutschebahn.bahnhoflive.ui.search.SearchResult;

public class RecentSearchesStore {

    private final FavoriteStationsStore<InternalStation> favoriteStationsStore;
    private final FavoriteStationsStore<InternalStation> recentDbStationsStore;

    private final FavoriteStationsStore<HafasStation> favoriteHafasStationsStore;
    private final FavoriteStationsStore<HafasStation> recentHafasStationsStore;

    public RecentSearchesStore(Context context) {

        final ApplicationServices applicationServices = BaseApplication.get().getApplicationServices();

        favoriteStationsStore = applicationServices.getFavoriteDbStationStore();
        recentDbStationsStore = new FavoriteStationsStore<>(context, "recent_dbstations", new InternalStationItemAdapter());

        favoriteHafasStationsStore = applicationServices.getFavoriteHafasStationsStore();

        final SharedPreferences favoriteStationStoreVersions = applicationServices.getFavoriteStationStoreVersions();

        final List<StationWrapper<HafasStation>> legacyRecentHafasStations;
        if (favoriteStationStoreVersions.getInt("hafasRecents", 0) < 1) {
            final FavoriteStationsStore<HafasStation> legacyRecentHafasStationsStore = new FavoriteStationsStore<>(context, "recent_hafasstations", new LegacyHafasStationItemAdapter());
            legacyRecentHafasStations = legacyRecentHafasStationsStore.getAll();
            legacyRecentHafasStationsStore.clear();

            favoriteStationStoreVersions.edit()
                    .putInt("hafasRecents", 1)
                    .commit();
        } else {
            legacyRecentHafasStations = null;
        }

        recentHafasStationsStore = new FavoriteStationsStore<>(context, "recent_hafasstations", new HafasStationItemAdapter());
        recentHafasStationsStore.adopt(legacyRecentHafasStations);
    }

    public List<SearchResult> loadRecentStations() {
        final List<StationWrapper<InternalStation>> all = recentDbStationsStore.getAll();
        final List<StationWrapper<HafasStation>> allHafas = recentHafasStationsStore.getAll();

        final ArrayList<SearchResult> searchResults = new ArrayList<>();

        for (StationWrapper<InternalStation> stationWrapper : all) {
            searchResults.add(new DBStationSearchResult(stationWrapper.getWrappedStation(), this, favoriteStationsStore));
        }
        for (StationWrapper<HafasStation> hafasStationWrapper : allHafas) {
            searchResults.add(new HafasStationSearchResult(hafasStationWrapper.getWrappedStation(), this, favoriteHafasStationsStore));
        }

        return searchResults;
    }

    public void put(Station station) {
        recentDbStationsStore.add(InternalStation.of(station));
    }

    public void putHafasStation(HafasStation hafasStation) {
        recentHafasStationsStore.add(hafasStation);
    }

    public void clear() {
        recentDbStationsStore.clear();
        recentHafasStationsStore.clear();
    }
}
