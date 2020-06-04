package de.deutschebahn.bahnhoflive.repository;

import de.deutschebahn.bahnhoflive.backend.rimap.model.StationFeatureCollection;

public class RimapStationFeatureCollectionResource extends RemoteResource<StationFeatureCollection> {

    private String id;

    @Override
    protected void onStartLoading(boolean force) {
        baseApplication.getRepositories().getMapRepository().queryStationInfo(
                id, new Listener(), !force, null);
    }

    @Override
    public boolean isLoadingPreconditionsMet() {
        return id != null;
    }

    public void initialize(Station station) {
        if (station != null) {
            initialize(station.getId());
        }
    }

    @Override
    protected boolean loadData(boolean force) {
        return super.loadData(force);
    }

    public void initialize(String id) {
        if (id != null) {
            this.id = id;
        }
    }
}
