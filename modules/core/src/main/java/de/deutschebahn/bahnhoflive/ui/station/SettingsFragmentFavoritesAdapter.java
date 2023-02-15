/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station;

import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.persistence.FavoriteStationsStore;
import de.deutschebahn.bahnhoflive.repository.EvaIdsProviderKt;
import de.deutschebahn.bahnhoflive.repository.InternalStation;
import de.deutschebahn.bahnhoflive.ui.DbStationWrapper;
import de.deutschebahn.bahnhoflive.ui.StationWrapper;
import de.deutschebahn.bahnhoflive.ui.ViewHolder;
import de.deutschebahn.bahnhoflive.ui.hub.StationImageResolver;
import de.deutschebahn.bahnhoflive.view.CompoundButtonChecker;
import de.deutschebahn.bahnhoflive.view.SelectableItemViewHolder;
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager;
import kotlinx.coroutines.CoroutineScope;

class SettingsFragmentFavoritesAdapter extends RecyclerView.Adapter<ViewHolder<StationWrapper>> {

    private final StationWrapper stationWrapper;
    private final List<StationWrapper<InternalStation>> stations;

    private final SingleSelectionManager selectionManager;

    public SettingsFragmentFavoritesAdapter(InternalStation station,
                                            FavoriteStationsStore<InternalStation> favoriteStationsStore,
                                            SingleSelectionManager selectionManager,
                                            StationImageResolver stationImageResolver) {
        stations = favoriteStationsStore.getAll();
        this.stationWrapper = find(stations, station, stationImageResolver, favoriteStationsStore);
        stations.remove(this.stationWrapper);

        this.selectionManager = selectionManager;
    }

    private StationWrapper find(List<StationWrapper<InternalStation>> stationWrappers,
                                InternalStation station,
                                StationImageResolver stationImageResolver,
                                FavoriteStationsStore<InternalStation> favoriteStationsStore) {
        for (StationWrapper wrapper : stationWrappers) {
            if (wrapper.wraps(station)) {
                return wrapper;
            }
        }

        return new DbStationWrapper(station, favoriteStationsStore, 0, null, null);

    }

    @Override
    public ViewHolder<StationWrapper> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new StationViewHolder(parent, selectionManager);
    }

    @Override
    public void onBindViewHolder(ViewHolder<StationWrapper> holder, int position) {
        final StationWrapper station = position == 0 ? this.stationWrapper : stations.get(position - 1);

        holder.bind(station);
    }

    @Override
    public int getItemCount() {
        return stations.size() + 1;
    }

    public static class StationViewHolder extends SelectableItemViewHolder<StationWrapper> implements CompoundButton.OnCheckedChangeListener {

        private final TextView titleView;
        private final CompoundButtonChecker bookmarkSwitch;

        public StationViewHolder(ViewGroup parent, SingleSelectionManager selectionManager) {
            super(parent, R.layout.card_expandable_setting_station, selectionManager);

            titleView = findTextView(R.id.title);

            bookmarkSwitch = new CompoundButtonChecker(itemView.findViewById(R.id.bookmarked_switch), this);
        }


        @Override
        protected void onBind(StationWrapper item) {
            super.onBind(item);

            titleView.setText(item.getTitle());

            bookmarkSwitch.setChecked(item.isFavorite());
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            final StationWrapper stationWrapper = getItem();
            stationWrapper.setFavorite(isChecked);
        }

    }

}
