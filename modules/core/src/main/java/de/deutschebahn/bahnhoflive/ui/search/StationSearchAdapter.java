/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.search;

import android.util.Log;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.deutschebahn.bahnhoflive.BaseApplication;
import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.analytics.TrackingManager;
import de.deutschebahn.bahnhoflive.backend.StopPlaceXKt;
import de.deutschebahn.bahnhoflive.backend.db.ris.model.StopPlace;
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation;
import de.deutschebahn.bahnhoflive.persistence.FavoriteStationsStore;
import de.deutschebahn.bahnhoflive.persistence.RecentSearchesStore;
import de.deutschebahn.bahnhoflive.repository.InternalStation;
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableRepository;
import de.deutschebahn.bahnhoflive.ui.ViewHolder;
import de.deutschebahn.bahnhoflive.ui.hub.DbDeparturesViewHolder;
import de.deutschebahn.bahnhoflive.ui.hub.DeparturesViewHolder;
import de.deutschebahn.bahnhoflive.ui.hub.HubViewModel;
import de.deutschebahn.bahnhoflive.ui.station.timetable.TimetableCollectorConnector;
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager;
import kotlin.Unit;
import kotlinx.coroutines.CoroutineScope;

class StationSearchAdapter extends RecyclerView.Adapter<ViewHolder> {

    private final SingleSelectionManager singleSelectionManager = new SingleSelectionManager(this);

    private final FavoriteStationsStore<InternalStation> favoriteDbStationsStore;
    private final FavoriteStationsStore<HafasStation> favoriteHafasStationsStore;

    private final RecentSearchesStore recentSearchesStore;
    private final HubViewModel hubViewModel;
    private final SearchItemPickedListener searchItemPickedListener;

    private List<? extends StopPlace> dbStations;
    private List<HafasStation> hafasStations;
    private List<SearchResult> searchResults;

    private boolean hafasError;
    private boolean dbError;
    private final LifecycleOwner owner;
    private final TrackingManager trackingManager;
    private final CoroutineScope coroutineScope;
    private final TimetableRepository timetableRepository;



    StationSearchAdapter(FragmentActivity context, RecentSearchesStore recentSearchesStore, SearchItemPickedListener searchItemPickedListener,
                         LifecycleOwner owner, TrackingManager trackingManager, CoroutineScope coroutineScope, TimetableRepository timetableRepository) {
        hubViewModel = new ViewModelProvider(context).get(HubViewModel.class);
        this.coroutineScope = coroutineScope;
        this.timetableRepository = timetableRepository;

        this.favoriteDbStationsStore = BaseApplication.get().getApplicationServices().getFavoriteDbStationStore();
        this.favoriteHafasStationsStore = BaseApplication.get().getApplicationServices().getFavoriteHafasStationsStore();
        this.recentSearchesStore = recentSearchesStore;
        this.searchItemPickedListener = searchItemPickedListener;
        this.owner = owner;
        this.trackingManager = trackingManager;
        singleSelectionManager.addListener(selectionManager -> {
            final SearchResult selectedItem = selectionManager.getSelectedItem(searchResults);
            if (selectedItem instanceof StoredStationSearchResult) {

                final StoredStationSearchResult searchResult = (StoredStationSearchResult) selectedItem;
Log.d("cr", "selItem: " + selectedItem.toString() + " searcgResult: " + searchResult.toString());
                ((StoredStationSearchResult) selectedItem).getTimetable().getFirst().loadIfNecessary();

                if(searchResult.timetableCollectorConnector == null)
                    searchResult.timetableCollectorConnector = new TimetableCollectorConnector(owner);

                // get Station-Abfahrten
                if (searchResult.timetableCollectorConnector != null)
                    searchResult.timetableCollectorConnector.setStationAndRequestDestinationStations(searchResult.station, timetable -> {
                                notifyDataSetChanged();
                                return Unit.INSTANCE;
                            },
                            integer -> {
                                return Unit.INSTANCE;
                            },
                            aBoolean -> {
                                return Unit.INSTANCE;
                            }

                    );
            } else if (selectedItem instanceof StopPlaceSearchResult) { // long click on (temp.) searchresult
                StopPlaceSearchResult searchResult = ((StopPlaceSearchResult) selectedItem);


                // get Station-Abfahrten
//                if(searchResult.getTimetableCollectorConnector() != null)
//                    searchResult.getTimetableCollectorConnector().setStationAndRequestDestinationStations(searchResult.getStation(), timetable -> {
//                                notifyDataSetChanged();
//                                return Unit.INSTANCE;
//                            },
//                            integer -> {
//                                return Unit.INSTANCE;
//                            },
//                            aBoolean -> {
//                                return Unit.INSTANCE;
//                            }
//
//                    );

            } else if (selectedItem instanceof HafasStationSearchResult) {
                ((HafasStationSearchResult) selectedItem).getTimetable().requestTimetable(true, "search");
            }
        });
        showRecents(coroutineScope, timetableRepository);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
            case 2:
                return new DbDeparturesViewHolder(parent, singleSelectionManager, owner,
                        trackingManager, searchItemPickedListener, TrackingManager.UiElement.ABFAHRT_SUCHE_BHF);
            case 1:
                return new DeparturesViewHolder(parent, owner, singleSelectionManager, trackingManager,
                        searchItemPickedListener, TrackingManager.UiElement.ABFAHRT_SUCHE_OPNV);
            default:
                return new StationSearchViewHolder(parent, R.layout.card_station_suggestion);
        }
    }

    @Override
    public int getItemViewType(int position) {
        final SearchResult searchResult = searchResults.get(position);

        if (searchResult instanceof StoredStationSearchResult) {
            return 0;
        }

        if (searchResult instanceof HafasStationSearchResult) {
            return 1;
        }

        if (searchResult instanceof StopPlaceSearchResult) {
            return 2;
        }
        return 3;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final SearchResult searchResult = searchResults.get(position);
        holder.bind(searchResult);
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    public void setDBStations(final List<? extends StopPlace> stations) {
        dbError = false;
        this.dbStations = stations;
        updateItems();
    }

    private void updateItems() {
        singleSelectionManager.clearSelection();

        searchResults.clear();

        if (dbStations != null) {
            for (StopPlace dbStation : dbStations) {
                final SearchResult searchResult;
                if (dbStation.isDbStation()) {

//                    TimetableCollectorConnector timetableCollectorConnector  = new TimetableCollectorConnector(this.owner);

                    searchResult = new StopPlaceSearchResult(coroutineScope, dbStation,
                            recentSearchesStore, favoriteDbStationsStore, timetableRepository); //, timetableCollectorConnector);
                } else {
                    final HafasStation hafasStation = StopPlaceXKt.toHafasStation(dbStation);
                    searchResult = new HafasStationSearchResult(hafasStation, recentSearchesStore, favoriteHafasStationsStore);
                }
                searchResults.add(searchResult);
            }
        }

        if (hafasStations != null) {
            for (HafasStation hafasStation : hafasStations) {
                searchResults.add(new HafasStationSearchResult(hafasStation, recentSearchesStore, favoriteHafasStationsStore));
            }
        }

        notifyDataSetChanged();
    }

    public void setHafasStations(List<HafasStation> stations) {
        hafasStations = stations;
        hafasError = false;

        updateItems();
    }

    public void showRecents(CoroutineScope coroutineScope, TimetableRepository timetableRepository) {
        singleSelectionManager.clearSelection();

        searchResults = recentSearchesStore.loadRecentStations(coroutineScope, timetableRepository);

        notifyDataSetChanged();
    }

    public void setHafasError() {
        hafasError = true;

        updateItems();
    }

    public void setDBError() {
        dbError = true;

        updateItems();
    }

    public boolean hasErrors() {
        return dbError || hafasError;
    }

    public void clearSelection() {
        singleSelectionManager.clearSelection();
    }
}
