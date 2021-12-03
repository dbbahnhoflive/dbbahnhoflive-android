/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.features;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import de.deutschebahn.bahnhoflive.backend.rimap.model.RimapPOI;
import de.deutschebahn.bahnhoflive.repository.DetailedStopPlaceStationWrapper;
import de.deutschebahn.bahnhoflive.repository.VenueFeature;
import de.deutschebahn.bahnhoflive.ui.map.Content;
import de.deutschebahn.bahnhoflive.ui.map.InitialPoiManager;
import de.deutschebahn.bahnhoflive.ui.map.MapActivity;
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter;
import de.deutschebahn.bahnhoflive.ui.station.shop.Shop;
import de.deutschebahn.bahnhoflive.util.Collections;

public class MapLink extends Link {

    protected Content.Source getMapSource() {
        return Content.Source.RIMAP;
    }

    @Nullable
    @Override
    public List<? extends Parcelable> getPois(StationFeature stationFeature) {
        List<Shop> venues = stationFeature.getVenues();
        return getRimapPOIS(venues);
    }

    @Override
    @Nullable
    public Intent createMapActivityIntent(Context context, StationFeature stationFeature) {
        final List<? extends Parcelable> pois = getPois(stationFeature);

        if (!Collections.hasContent(pois)) {
            return null;
        }

        final DetailedStopPlaceStationWrapper stationWrapper = DetailedStopPlaceStationWrapper.Companion.of(stationFeature.getDetailedStopPlace());
        if (stationWrapper == null) {
            return null;
        }

        final Intent intent = MapActivity.Companion.createIntent(context, stationWrapper);

        final VenueFeature venueFeature = stationFeature.getStationFeatureTemplate().getDefinition().getVenueFeature();
        if (venueFeature != null) {
            RimapFilter.putPreset(intent, venueFeature.mapPreset);
        }

        InitialPoiManager.putInitialPoi(intent, getMapSource(), pois.get(0));

        return intent;
    }

    @Nullable
    private List<RimapPOI> getRimapPOIS(List<Shop> venues) {
        if (Collections.hasContent(venues)) {
            List<RimapPOI> rimapPOIs = new ArrayList<>(venues.size());

            for (Shop venue : venues) {
                final RimapPOI rimapPOI = venue.getRimapPOI();
                if (rimapPOI != null) {
                    rimapPOIs.add(rimapPOI);
                }
            }
            return rimapPOIs;
        }

        return null;
    }

    @Override
    public boolean isAvailable(StationFeature stationFeature) {
        return Collections.hasContent(getPois(stationFeature));
    }
}
