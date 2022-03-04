/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.features;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import de.deutschebahn.bahnhoflive.backend.db.ris.model.LocalService;
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContent;
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContentType;
import de.deutschebahn.bahnhoflive.ui.ServiceContentFragment;
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

        String additionalText = null;
        if (ServiceContentType.Local.TRAVEL_CENTER.equals(staticInfo.type)) {
            final LocalService travelCenter = stationFeature.getRisServicesAndCategory().getClosestTravelCenter();
            if (travelCenter != null) {
                additionalText = travelCenter.getOpeningHours(); // TODO 2116: new AvailabilityRenderer().renderSchedule(travelCenter.getOpeningHours());
            }
        }

        final String title = context.getString(stationFeature.getStationFeatureTemplate().getDefinition().getLabel());
        final Bundle args = ServiceContentFragment.createArgs(
                title, new ServiceContent(staticInfo, additionalText), trackingTag);
        return ServiceContentFragment.create(args);
    }

    @Override
    public boolean isAvailable(StationFeature stationFeature) {
        return super.isAvailable(stationFeature) || getStaticInfo(stationFeature) != null;

    }

    private StaticInfo getStaticInfo(StationFeature stationFeature) {
        final StaticInfoCollection staticInfoCollection = stationFeature.getStaticInfoCollection();
        if (staticInfoCollection != null) {
            return staticInfoCollection.typedStationInfos.get(serviceContentType);
        }
        return null;
    }
}
