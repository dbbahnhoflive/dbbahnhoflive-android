/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import com.google.android.gms.maps.model.MarkerOptions;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.backend.RestHelper;
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation;
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasTimetable;
import de.deutschebahn.bahnhoflive.ui.ViewHolder;
import de.deutschebahn.bahnhoflive.ui.timetable.localtransport.DeparturesActivity;

class HafasMarkerContent extends MarkerContent {
    private final HafasTimetable hafasTimetable;
    private final RestHelper restHelper;
    private ViewHolder<MarkerBinder> latestBoundViewHolder;

    public HafasMarkerContent(HafasTimetable hafasTimetable, RestHelper restHelper) {
        super(R.drawable.app_karte_haltestelle);
        this.hafasTimetable = hafasTimetable;
        this.restHelper = restHelper;
    }

    @Override
    public String getTitle() {
        return getStation().name;
    }

    @Override
    public MarkerOptions createMarkerOptions() {
        final MarkerOptions markerOptions = super.createMarkerOptions();

        try {
            markerOptions.position(getStation().getLocation()); // can cause exception
        }
        catch(Exception e) {

        }

        return markerOptions;
    }

    protected HafasStation getStation() {
        return hafasTimetable.getStation();
    }

    @Override
    public ViewType getViewType() {
        return ViewType.STATION;
    }

    @Override
    public int getMapIcon() {
        return R.drawable.app_karte_haltestelle;
    }

    public HafasTimetable getHafasTimetable() {
        return hafasTimetable;
    }

    @Override
    public boolean wraps(Parcelable item) {
        final HafasStation myStation = hafasTimetable.station;
        if (myStation == null) {
            return false;
        }

        if (!(item instanceof HafasTimetable)) {
            return false;
        }

        final HafasTimetable timetableItem = (HafasTimetable) item;
        return myStation.equals(timetableItem.station);
    }

    @Override
    public void onFlyoutClick(Context context) {
        final Intent intent = DeparturesActivity.createIntent(context, hafasTimetable.getStation(), hafasTimetable.getResource().getData().getValue());
        context.startActivity(intent);
    }

    @Override
    public void bindTo(ViewHolder<MarkerBinder> flyoutViewHolder) {
        latestBoundViewHolder = flyoutViewHolder;
    }

    @Override
    public void onHighlighted(boolean highlighted) {
        if (highlighted && hafasTimetable != null) {
            hafasTimetable.requestTimetable(true, MapOverlayFragment.ORIGIN_MAP, false);
        }
    }

    @Override
    public int getPreSelectionRating() {
        return -1;
    }
}
