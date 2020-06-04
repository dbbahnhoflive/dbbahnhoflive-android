package de.deutschebahn.bahnhoflive.repository;

import java.util.List;

import de.deutschebahn.bahnhoflive.backend.rimap.model.RimapPOI;

public class RimapPOIListResource extends RemoteResource<List<RimapPOI>> {

    private Station stationId;

    @Override
    public boolean isLoadingPreconditionsMet() {
        return stationId != null;
    }

    @Override
    protected void onStartLoading(boolean force) {
        baseApplication.getRepositories().getMapRepository().queryPois(stationId, new Listener(), !force);
    }

    public void initialize(Station stationId) {
        this.stationId = stationId;
    }

    public void load() {
        loadData(false);
    }
}
