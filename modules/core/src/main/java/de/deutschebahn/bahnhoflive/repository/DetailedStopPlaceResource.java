package de.deutschebahn.bahnhoflive.repository;

import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.DetailedStopPlace;

public class DetailedStopPlaceResource extends RemoteResource<DetailedStopPlace> {

    private String stadaId;

    public void initialize(String id) {
        this.stadaId = id;

        loadData(false);
    }

    @Override
    protected void onStartLoading(boolean force) {
        if (force || getMutableData().getValue() == null) {
            baseApplication.getRepositories().getStationRepository().queryStationDetails(
                    new Listener(), stadaId, force, null);
        }
    }

    @Override
    public boolean isLoadingPreconditionsMet() {
        return stadaId != null;
    }

    public void initialize(Station station) {
        if (station != null) {
            initialize(station.getId());
        }
    }
}
