package de.deutschebahn.bahnhoflive.repository;

import android.os.Handler;
import android.os.Looper;

import java.util.List;

import de.deutschebahn.bahnhoflive.BaseApplication;
import de.deutschebahn.bahnhoflive.model.parking.ParkingFacility;

public class ParkingsResource extends RemoteResource<List<ParkingFacility>> {

    private String stationId;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onStartLoading(boolean force) {
        BaseApplication.get().getRepositories().getParkingRepository().queryFacilities(stationId, new Listener());
    }

    @Override
    public boolean isLoadingPreconditionsMet() {
        return stationId != null;
    }

    public void initialize(final Station station) {
        this.stationId = station.getId();

        loadData(false);
    }
}
