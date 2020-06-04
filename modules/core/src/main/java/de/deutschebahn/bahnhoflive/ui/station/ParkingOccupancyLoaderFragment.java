package de.deutschebahn.bahnhoflive.ui.station;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.List;

import de.deutschebahn.bahnhoflive.BaseApplication;
import de.deutschebahn.bahnhoflive.backend.BaseRestListener;
import de.deutschebahn.bahnhoflive.backend.RestHelper;
import de.deutschebahn.bahnhoflive.backend.bahnpark.model.BahnparkSite;
import de.deutschebahn.bahnhoflive.repository.Station;
import de.deutschebahn.bahnhoflive.ui.station.parking.Parkings;

public class ParkingOccupancyLoaderFragment extends LoaderFragment<Parkings, ParkingOccupancyLoaderFragment.Listener> {

    private final Handler handler = new Handler(Looper.getMainLooper());

    public static final Factory<ParkingOccupancyLoaderFragment> FACTORY = new Factory<ParkingOccupancyLoaderFragment>() {
        @Override
        public ParkingOccupancyLoaderFragment createLoaderFragment() {
            return new ParkingOccupancyLoaderFragment();
        }
    };

    private final RestHelper restHelper = BaseApplication.get().getRestHelper();
    private Station station;

    public static ParkingOccupancyLoaderFragment of(Activity activity) {
        return LoaderFragment.of(activity, ParkingOccupancyLoaderFragment.class.getSimpleName(), FACTORY);
    }

    public void setStation(Station station) {
        this.station = station;

        if (!isLoading() && !isDataAvailable()) {
            load(station);
        }
    }

    public void useInstanceStateIfBetter(Bundle bundle) {
        final Parkings parkings = bundle.getParcelable(getTag());
        if (parkings != null) {
            setData(parkings);
            notifyListeners(0);
            setState(null, true, true);
        }
    }

    public interface Listener {
        void onBahnparkSitesWithOccupancyUpdated(List<BahnparkSite> bahnparkSites, boolean errors);
    }

    @Override
    protected void notifyListener(Listener listener, int errors) {
        final Parkings data = getData();
        listener.onBahnparkSitesWithOccupancyUpdated(data == null ? null : data.getBahnparkSites(),
                errors > 0);
    }

    private void load(Station station) {
        setState(true, null, null);
        final BaseRestListener<List<BahnparkSite>> listener = new BaseRestListener<List<BahnparkSite>>() {
            @Override
            public void onSuccess(List<BahnparkSite> payload) {
                onBahnparkSitesResponse(payload);
            }

            @Override
            public void onFail(VolleyError reason) {
                super.onFail(reason);

                setState(false, null, false);
                notifyListeners(1);
            }
        };
        handler.postDelayed(() -> {
            listener.onSuccess(new ArrayList<>());
        }, 100);
    }

    private void onBahnparkSitesResponse(List<BahnparkSite> bahnparkSites) {
        setData(new Parkings(bahnparkSites));
        notifyListeners(0);
        setState(false, true, true);
    }

    public void refresh() {
        if (!isLoading() && station != null) {
            load(station);
        }
    }
}
