package de.deutschebahn.bahnhoflive.repository;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

import de.deutschebahn.bahnhoflive.backend.bahnpark.model.BahnparkSite;

public class ParkingsResource extends RemoteResource<List<BahnparkSite>> {

    private String stationId;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onStartLoading(boolean force) {
        handler.postDelayed(() -> {
            try {
                new Listener().onSuccess(new ArrayList<>());
            } catch (Exception ignored) {
            }
        }, 100);
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
