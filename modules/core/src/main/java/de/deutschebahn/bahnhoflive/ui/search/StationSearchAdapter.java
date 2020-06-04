package de.deutschebahn.bahnhoflive.ui.search;

import android.view.ViewGroup;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.deutschebahn.bahnhoflive.analytics.TrackingManager;
import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.StopPlace;
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation;
import de.deutschebahn.bahnhoflive.persistence.FavoriteStationsStore;
import de.deutschebahn.bahnhoflive.persistence.RecentSearchesStore;
import de.deutschebahn.bahnhoflive.repository.InternalStation;
import de.deutschebahn.bahnhoflive.ui.ViewHolder;
import de.deutschebahn.bahnhoflive.ui.hub.DbDeparturesViewHolder;
import de.deutschebahn.bahnhoflive.ui.hub.DeparturesViewHolder;
import de.deutschebahn.bahnhoflive.ui.hub.HubViewModel;
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager;

class StationSearchAdapter extends RecyclerView.Adapter<ViewHolder> {

    private final SingleSelectionManager singleSelectionManager = new SingleSelectionManager(this);

    private final FavoriteStationsStore<InternalStation> favoriteDbStationsStore;
    private final FavoriteStationsStore<HafasStation> favoriteHafasStationsStore;

    private final RecentSearchesStore recentSearchesStore;
    private final HubViewModel hubViewModel;

    private List<StopPlace> dbStations;
    private List<HafasStation> hafasStations;
    private List<SearchResult> searchResults;

    private boolean hafasError;
    private boolean dbError;
    private final LifecycleOwner owner;
    private final TrackingManager trackingManager;

    StationSearchAdapter(FragmentActivity context, RecentSearchesStore recentSearchesStore, LifecycleOwner owner, TrackingManager trackingManager) {
        hubViewModel = ViewModelProviders.of(context).get(HubViewModel.class);

        this.favoriteDbStationsStore = FavoriteStationsStore.getFavoriteDbStationsStore(context);
        this.favoriteHafasStationsStore = FavoriteStationsStore.getFavoriteHafasStationsStore(context);
        this.recentSearchesStore = recentSearchesStore;
        this.owner = owner;
        this.trackingManager = trackingManager;
        singleSelectionManager.addListener(new SingleSelectionManager.Listener() {
            @Override
            public void onSelectionChanged(SingleSelectionManager selectionManager) {
                final SearchResult selectedItem = selectionManager.getSelectedItem(searchResults);
                if (selectedItem instanceof DBStationSearchResult) {
                    ((DBStationSearchResult) selectedItem).getTimetable().loadIfNecessary();
                } else if (selectedItem instanceof HafasStationSearchResult) {
                    ((HafasStationSearchResult) selectedItem).getTimetable().requestTimetable(true, "search");
                }
            }
        });
        showRecents();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                return new DbDeparturesViewHolder(parent, singleSelectionManager, owner, trackingManager, TrackingManager.UiElement.ABFAHRT_SUCHE_BHF);
            default:
                return new DeparturesViewHolder(parent, owner, singleSelectionManager, trackingManager, TrackingManager.UiElement.ABFAHRT_SUCHE_OPNV);
        }
    }

    @Override
    public int getItemViewType(int position) {
        final SearchResult searchResult = searchResults.get(position);

        if (searchResult instanceof DBStationSearchResult) {
            return 0;
        }

        if (searchResult instanceof HafasStationSearchResult) {
            return 1;
        }
        return 2;
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

    public void setDBStations(final List<StopPlace> stations) {
        this.dbStations = stations;

        updateItems();
    }

    private void updateItems() {
        singleSelectionManager.clearSelection();

        searchResults.clear();

        if (dbStations != null) {
            for (StopPlace dbStation : dbStations) {
                searchResults.add(new StopPlaceSearchResult(dbStation, recentSearchesStore, favoriteDbStationsStore));
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

    public void showRecents() {
        singleSelectionManager.clearSelection();

        searchResults = recentSearchesStore.loadRecentStations();

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
