package de.deutschebahn.bahnhoflive.backend.rimap.model;

import java.util.List;

public class StationFeatureCollection {
    public final List<StationFeature> features;

    public StationFeatureCollection(List<StationFeature> features) {
        this.features = features;
    }
}
