/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.hub;

import android.view.ViewGroup;

import androidx.lifecycle.LifecycleOwner;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.analytics.TrackingManager;
import de.deutschebahn.bahnhoflive.ui.search.HafasStationSearchResult;
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager;

class NearbyDeparturesViewHolder extends DeparturesViewHolder {

    private final DistanceViewHolder distanceViewHolder;

    public NearbyDeparturesViewHolder(ViewGroup parent, LifecycleOwner owner, SingleSelectionManager singleSelectionManager, TrackingManager trackingManager) {
        super(parent, R.layout.card_nearby_departures, owner, singleSelectionManager, trackingManager, null, TrackingManager.UiElement.ABFAHRT_NAEHE_OPNV);
        distanceViewHolder = new DistanceViewHolder(itemView);
    }

    @Override
    protected void onBind(HafasStationSearchResult item) {
        super.onBind(item);
        distanceViewHolder.bind(item.getTimetable().getStation().dist / 1000f);
    }
}
