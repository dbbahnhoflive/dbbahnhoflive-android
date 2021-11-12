/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.location;

import android.location.Location;

import androidx.annotation.Nullable;

import com.huawei.hms.maps.model.LatLng;

public class LocationSmoother {

    public final static LocationSmoother SINGLETON = new LocationSmoother();

    private Double latestLatitude;
    private Double latestLongitude;

    private final float[] results = new float[1];

    private LocationSmoother() {}

    private boolean latestLocationNeedsUpdate(double latitude, double longitude) {
        if (latestLongitude == null) {
            return true;
        }

        Location.distanceBetween(latestLatitude, latestLongitude,
                latitude, longitude, results);

        return results[0] > 100;
    }

    @Nullable
    public synchronized LatLng smooth(LatLng latLng) {
        if (latLng == null) {
            return null;
        }

        if (latestLocationNeedsUpdate(latLng.latitude, latLng.longitude)) {
            latestLatitude = latLng.latitude;
            latestLongitude = latLng.longitude;

            return latLng;
        }

        return new LatLng(latestLatitude, latestLongitude);
    }

    @Nullable
    public synchronized Location smooth(Location location) {
        if (location == null) {
            return null;
        }

        if (latestLocationNeedsUpdate(location.getLatitude(), location.getLongitude())) {
            latestLatitude = location.getLatitude();
            latestLongitude = location.getLongitude();
        } else {
            location.setLatitude(latestLatitude);
            location.setLongitude(latestLongitude);
        }

        return location;
    }

    public boolean update(Double latitude, Double longitude) {
        if (latestLocationNeedsUpdate(latitude, longitude)) {
            latestLatitude = latitude;
            latestLongitude = longitude;

            return true;
        }

        return false;
    }

    public Double getLatestLatitude() {
        return latestLatitude;
    }

    public Double getLatestLongitude() {
        return latestLongitude;
    }

}
