/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.deutschebahn.bahnhoflive.push.FacilityPushManager;
import de.deutschebahn.bahnhoflive.ui.ViewHolder;

class FlyoutsAdapter extends RecyclerView.Adapter<ViewHolder<MarkerBinder>> {

    private final Content content;

    private final List<MarkerBinder> visibleMarkerBinders = new ArrayList<>();

    private int actualItemCount = 0;

    private final FacilityPushManager facilityPushManager = FacilityPushManager.getInstance();
    private final LifecycleOwner owner;
    private final MapViewModel mapViewModel;

    public FlyoutsAdapter(Content content, LifecycleOwner owner, MapViewModel mapViewModel) {
        this.content = content;
        this.owner = owner;
        this.mapViewModel = mapViewModel;
    }

    @NonNull
    @Override
    public ViewHolder<MarkerBinder> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final MarkerContent.ViewType contentViewType = MarkerContent.ViewType.VALUES[viewType];

        return createViewHolder(parent, contentViewType);
    }

    @NonNull
    private ViewHolder<MarkerBinder> createViewHolder(ViewGroup parent, MarkerContent.ViewType contentViewType) {
        switch (contentViewType) {
            case STATION:
                return new StationFlyoutViewHolder(parent, owner);
            case DB_STATION:
                return new DbStationFlyoutViewHolder(parent, owner);
            case BOOKMARKABLE:
                return new ElevatorFlyoutViewHolder(parent, facilityPushManager);
            case TRACK:
                return new TrackFlyoutViewHolder(parent, mapViewModel);
            case COMMON:
            default:
                return new CommonFlyoutViewHolder(parent);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder<MarkerBinder> holder, final int virtualPosition) {
        holder.bind(getMarkerBinder(virtualPosition));
    }

    private int getActualPosition(int virtualPosition) {
        return virtualPosition % actualItemCount;
    }

    @Override
    public int getItemCount() {
        return actualItemCount;
    }

    @Override
    public int getItemViewType(int virtualPosition) {
        final MarkerBinder markerBinder = getMarkerBinder(virtualPosition);

        return markerBinder.getMarkerContent().getViewType().ordinal();
    }

    private MarkerBinder getMarkerBinder(int virtualPosition) {
        final int actualPosition = getActualPosition(virtualPosition);
        return visibleMarkerBinders.get(actualPosition);
    }

    private void updateItemCounts() {
        actualItemCount = visibleMarkerBinders.size();
    }

    int getCentralPosition(MarkerContent markerContent) {
        for (int i = 0; i < visibleMarkerBinders.size(); i++) {
            if (visibleMarkerBinders.get(i).getMarkerContent() == markerContent) {
                return i;
            }
        }

        return -1;
    }

    void visibilityChanged() {
        visibleMarkerBinders.clear();

        visibleMarkerBinders.addAll(content.getVisibleMarkerBinders());

        updateItemCounts();

        notifyDataSetChanged();
    }

    public MarkerBinder getFirstItem() {
        final List<MarkerBinder> visibleMarkerBinders = this.visibleMarkerBinders;
        return (visibleMarkerBinders.isEmpty()) ? null : visibleMarkerBinders.get(0);
    }
}
