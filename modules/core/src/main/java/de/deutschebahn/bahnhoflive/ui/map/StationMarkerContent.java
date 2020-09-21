/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.repository.DbTimetableResource;
import de.deutschebahn.bahnhoflive.repository.Station;
import de.deutschebahn.bahnhoflive.ui.ViewHolder;
import de.deutschebahn.bahnhoflive.ui.station.StationActivity;

class StationMarkerContent extends MarkerContent {
    private final Station station;
    private DbTimetableResource timetable;

    public StationMarkerContent(Station station) {
        super(R.drawable.legacy_dbmappinicon);
        this.station = station;
    }

    @Override
    public String getTitle() {
        return station.getTitle();
    }

    @Override
    public int getMapIcon() {
        return R.drawable.legacy_dbmappinicon;
    }

    @Override
    public MarkerOptions createMarkerOptions() {
        final MarkerOptions markerOptions = super.createMarkerOptions();
        final LatLng location = station.getLocation();
        if (location != null) {
            markerOptions.position(location);
        }
        return markerOptions;
    }

    @Override
    public boolean wraps(Parcelable item) {
        return station.equals(item);
    }

    @Override
    public void onFlyoutClick(Context context) {
        final Intent intent = StationActivity.createIntent(context, station);
        context.startActivity(intent);
    }

    @Override
    public ViewType getViewType() {
        return ViewType.DB_STATION;
    }

    public void setTimetable(DbTimetableResource timetable) {
        this.timetable = timetable;
    }

    @Override
    public void bindTo(ViewHolder<MarkerBinder> flyoutViewHolder) {
    }

    @Override
    public int getPreSelectionRating() {
        return 1;
    }

    public DbTimetableResource getDepartures() {
        return timetable;
    }

}
