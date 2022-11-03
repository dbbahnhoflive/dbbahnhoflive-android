/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.features;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import de.deutschebahn.bahnhoflive.backend.db.ris.model.LocalService;
import de.deutschebahn.bahnhoflive.backend.local.model.DailyOpeningHours;
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContent;
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContentType;
import de.deutschebahn.bahnhoflive.ui.ServiceContentFragment;
import de.deutschebahn.bahnhoflive.ui.accessibility.ContextXKt;
import de.deutschebahn.bahnhoflive.ui.station.StaticInfoCollection;
import de.deutschebahn.bahnhoflive.ui.station.info.StaticInfo;

public class MapOrInfoLink extends MapLink {
    private final String serviceContentType;
    private final String trackingTag;

    public MapOrInfoLink(String serviceContentType, String trackingTag) {
        super();
        this.serviceContentType = serviceContentType;
        this.trackingTag = trackingTag;
    }

    @Override
    public ServiceContentFragment createServiceContentFragment(Context context, @NonNull StationFeature stationFeature) {
        final StaticInfo staticInfo = getStaticInfo(stationFeature);
        if (staticInfo == null) {
            return null;
        }

        List<DailyOpeningHours> openingHours = null;
        if (ServiceContentType.Local.TRAVEL_CENTER.equals(staticInfo.type)) {
            final LocalService travelCenter = stationFeature.getRisServicesAndCategory().getClosestTravelCenter();
            if (travelCenter != null) {
                openingHours = travelCenter.getParsedOpeningHours();
            }
        }

        final String title = context.getString(stationFeature.getStationFeatureTemplate().getDefinition().getLabel());
        final Bundle args = ServiceContentFragment.createArgs(
                title, new ServiceContent(staticInfo, null, null, null, openingHours), trackingTag);
        return ServiceContentFragment.create(args);
    }

    @Nullable
    @Override
    public Intent createMapActivityIntent(Context context, StationFeature stationFeature) {
        if (ContextXKt.isSpokenFeedbackAccessibilityEnabled(context)) {
            return null;
        }

        return super.createMapActivityIntent(context, stationFeature);
    }

    @Override
    public boolean isAvailable(Context context, StationFeature stationFeature) {
        return super.isAvailable(context, stationFeature) || getStaticInfo(stationFeature) != null;
    }

    private StaticInfo getStaticInfo(StationFeature stationFeature) {
        final StaticInfoCollection staticInfoCollection = stationFeature.getStaticInfoCollection();
        if (staticInfoCollection != null) {
            return staticInfoCollection.typedStationInfos.get(serviceContentType);
        }
        return null;
    }
}
