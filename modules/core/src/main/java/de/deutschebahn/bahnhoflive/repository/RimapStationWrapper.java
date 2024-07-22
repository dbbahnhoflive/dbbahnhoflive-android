/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import de.deutschebahn.bahnhoflive.backend.db.ris.model.Coordinate2D;
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds;
import de.deutschebahn.bahnhoflive.backend.rimap.model.StationFeatureCollection;
import de.deutschebahn.bahnhoflive.backend.rimap.model.StationProperties;
import de.deutschebahn.bahnhoflive.util.Collections;

public class RimapStationWrapper implements Station {

    @NonNull
    private final EvaIds evaIds;

    private final StationProperties stationProperties;

    private final LatLng location;

    private RimapStationWrapper(StationFeatureCollection stationFeatureCollection) {
        stationProperties = stationFeatureCollection.features.get(0).properties;

        location = stationProperties.lat == null || stationProperties.lon == null || (
                stationProperties.lat == 0 && stationProperties.lon == 0
        ) ? null :
                new LatLng(stationProperties.lat, stationProperties.lon);

        evaIds = new EvaIds(stationFeatureCollection);
    }

    public static RimapStationWrapper wrap(StationFeatureCollection stationFeatureCollection) {
        if (stationFeatureCollection != null &&
                Collections.hasContent(stationFeatureCollection.features)) {
            return new RimapStationWrapper(stationFeatureCollection);
        }

        return null;
    }

    @Override
    public String getId() {
        return stationProperties.zoneid;
    }

    @Override
    public String getTitle() {
        return stationProperties.name;
    }

    @Override
    public LatLng getLocation() {
        return location;
    }

    @Override
    @NonNull
    public EvaIds getEvaIds() {
        return evaIds;
    }

    @Override
    public void addEvaIds(EvaIds ids)
    {

    }

    @Override
    public void setPosition(Coordinate2D coord) {

    }

}
