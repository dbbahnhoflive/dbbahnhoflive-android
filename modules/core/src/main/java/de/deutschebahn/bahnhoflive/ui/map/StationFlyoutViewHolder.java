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
import de.deutschebahn.bahnhoflive.ui.timetable.localtransport.ReducedHafasDeparturesViewHolder;
import de.deutschebahn.bahnhoflive.view.Views;

class StationFlyoutViewHolder extends FlyoutViewHolder {

    public final ReducedHafasDeparturesViewHolder reducedHafasDeparturesViewHolder;
    private final LifecycleOwner owner;

    public StationFlyoutViewHolder(ViewGroup parent, LifecycleOwner owner) {
        super(Views.inflate(parent, R.layout.flyout_station), EquipmentID.UNKNOWN);
        this.owner = owner;

        reducedHafasDeparturesViewHolder = new ReducedHafasDeparturesViewHolder(itemView, this.owner);

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

        if (markerContent instanceof HafasMarkerContent) {
            final HafasMarkerContent hafasMarkerContent = (HafasMarkerContent) markerContent;
            markerContent.bindTo(this);

            reducedHafasDeparturesViewHolder.bind(hafasMarkerContent.getHafasTimetable().getResource());
        }
    }

}
