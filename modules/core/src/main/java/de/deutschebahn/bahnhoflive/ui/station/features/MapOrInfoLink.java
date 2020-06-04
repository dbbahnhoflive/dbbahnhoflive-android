package de.deutschebahn.bahnhoflive.ui.station.features;

import android.content.Context;
import android.os.Bundle;

import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContent;
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
    public ServiceContentFragment createServiceContentFragment(Context context, StationFeature stationFeature) {
        final String title = context.getString(stationFeature.getStationFeatureTemplate().getDefinition().getLabel());
        final Bundle args = ServiceContentFragment.createArgs(
                title, new ServiceContent(getStaticInfo(stationFeature)), trackingTag);
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
