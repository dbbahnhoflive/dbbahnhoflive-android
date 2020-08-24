package de.deutschebahn.bahnhoflive.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.Map;

import de.deutschebahn.bahnhoflive.repository.StationResource;
import de.deutschebahn.bahnhoflive.repository.StationResourceProvider;

public class StadaStationCacheViewModel extends ViewModel implements StationResourceProvider {
    private final Map<String, StationResource> cache = new HashMap<>();

    @Override
    public StationResource getStationResource(String id) {
        return getStationResource(id, new StationResource(id));
    }

    @NonNull
    private StationResource getStationResource(String id, StationResource newResource) {
        StationResource stationResource = cache.get(id);

        if (stationResource == null) {
            stationResource = newResource;
            cache.put(id, stationResource);
        } else {
            stationResource.refresh();
        }

        return stationResource;
    }

}
