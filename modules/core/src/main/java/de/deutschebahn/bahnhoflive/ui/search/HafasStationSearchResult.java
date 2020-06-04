package de.deutschebahn.bahnhoflive.ui.search;

import android.content.Context;
import android.content.Intent;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation;
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasTimetable;
import de.deutschebahn.bahnhoflive.persistence.FavoriteStationsStore;
import de.deutschebahn.bahnhoflive.persistence.RecentSearchesStore;
import de.deutschebahn.bahnhoflive.ui.timetable.localtransport.DeparturesActivity;

public class HafasStationSearchResult extends StationSearchResult<HafasStation, HafasTimetable> {
    private final HafasTimetable hafasTimetable;

    public HafasStationSearchResult(HafasStation hafasStation, RecentSearchesStore recentSearchesStore, FavoriteStationsStore<HafasStation> favoriteStationsStore) {
        super(R.drawable.app_haltestelle, recentSearchesStore, favoriteStationsStore);
        this.hafasTimetable = new HafasTimetable(hafasStation);
    }

    @Override
    public CharSequence getTitle() {
        return hafasTimetable.station.name;
    }

    @Override
    public boolean isFavorite() {
        return favoriteStationsStore.isFavorite(hafasTimetable.station);
    }

    @Override
    public void setFavorite(boolean favorite) {
        if (favorite) {
            favoriteStationsStore.add(hafasTimetable.station);
        } else {
            favoriteStationsStore.remove(hafasTimetable.station);
        }

    }

    @Override
    public void onClick(Context context, boolean details) {
        if (recentSearchesStore != null) {
            recentSearchesStore.putHafasStation(hafasTimetable.station);
        }

        final Intent intent = DeparturesActivity.createIntent(context, hafasTimetable.getStation(), hafasTimetable.getDepartures());
        context.startActivity(intent);
    }

    @Override
    public boolean isLocal() {
        return true;
    }

    @Override
    public HafasTimetable getTimetable() {
        return hafasTimetable;
    }
}
