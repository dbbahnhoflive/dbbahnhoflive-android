package de.deutschebahn.bahnhoflive.repository;

import androidx.annotation.NonNull;

import com.android.volley.VolleyError;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import de.deutschebahn.bahnhoflive.BaseApplication;
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener;
import de.deutschebahn.bahnhoflive.backend.hafas.LocalTransportFilter;
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation;
import de.deutschebahn.bahnhoflive.backend.hafas.model.ProductCategory;
import de.deutschebahn.bahnhoflive.util.Collections;

public class HafasStationResource extends RemoteResource<HafasStation> {

    public static final String ORIGIN_TIMETABLE = "timetable";

    private Station station;

    public void initialize(Station station) {
        if (!isLoadingPreconditionsMet()) {
            this.station = station;

            loadData(false);
        }
    }

    @Override
    public boolean isLoadingPreconditionsMet() {
        return station != null && station.getLocation() != null;
    }

    @Override
    protected void onStartLoading(boolean force) {
        final LatLng location = station.getLocation();
        if (location == null) {
            setError(new VolleyError("Missing location"));
            return;
        }

        BaseApplication.get().getRepositories().getLocalTransportRepository()
                .queryNearbyStations(location.latitude, location.longitude, new LocalTransportFilter(1, ProductCategory.BITMASK_EXTENDED_LOCAL_TRANSPORT),
                        new VolleyRestListener<List<HafasStation>>() {
                            final Listener listener = new Listener();

                            @Override
                            public void onSuccess(@NonNull List<HafasStation> payload) {
                                if (Collections.hasContent(payload)) {
                                    listener.onSuccess(payload.get(0));
                                } else {
                                    onFail(new VolleyError("Not found"));
                                }
                            }

                            @Override
                            public void onFail(VolleyError reason) {
                                listener.onFail(reason);
                            }
                        }, ORIGIN_TIMETABLE, 2000);

    }

    public void initialize(HafasStation hafasStation) {
        setResult(hafasStation);
    }
}
