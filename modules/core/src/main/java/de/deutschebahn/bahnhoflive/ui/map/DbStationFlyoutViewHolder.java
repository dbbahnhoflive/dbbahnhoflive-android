/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map;

import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.LifecycleOwner;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableCollector;
import de.deutschebahn.bahnhoflive.ui.timetable.localtransport.ReducedDbDeparturesViewHolder;
import de.deutschebahn.bahnhoflive.view.Views;

class DbStationFlyoutViewHolder extends FlyoutViewHolder {

    public final ReducedDbDeparturesViewHolder departuresViewHolder;

    public DbStationFlyoutViewHolder(ViewGroup parent, LifecycleOwner owner) {
        super(Views.inflate(parent, R.layout.flyout_station));

        departuresViewHolder = new ReducedDbDeparturesViewHolder(itemView, R.id.view_flipper, owner);

        final View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MarkerBinder markerBinder = getItem();
                markerBinder.getMarkerContent().onFlyoutClick(v.getContext());
            }
        };
        itemView.setOnClickListener(onClickListener);
        itemView.findViewById(R.id.view_flipper).setOnClickListener(onClickListener);
    }

    @Override
    protected void onBind(MarkerBinder item) {
        super.onBind(item);

        final MarkerContent markerContent = item.getMarkerContent();

        if (markerContent instanceof StationMarkerContent) {
            final StationMarkerContent stationMarkerContent = (StationMarkerContent) markerContent;

            final TimetableCollector timetableCollector = stationMarkerContent.getDepartures();
            departuresViewHolder.bind(timetableCollector == null ? null : timetableCollector.getTimetableStateFlow().getValue());
        }
    }

}
