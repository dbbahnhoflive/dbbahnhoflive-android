package de.deutschebahn.bahnhoflive.ui.hub;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.deutschebahn.bahnhoflive.location.BaseLocationListener;
import de.deutschebahn.bahnhoflive.permission.Permission;

public class LocationFragment extends Fragment {

    public static final String FRAGMENT_TAG = "locator";

    private LocationManager locationManager;

    private final Criteria criteria;
    private final ArrayList<LocationListener> locationListeners = new ArrayList<>();
    private Location location;
    private LocationCollector locationCollector;

    public LocationFragment() {
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onDestroy() {
        locationManager = null;

        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();

        acquireLocation(false);
    }

    public void addLocationListener(LocationListener locationListener) {
        if (location != null) {
            locationListener.onLocationChanged(location);
        }

        locationListeners.add(locationListener);
    }

    public void removeLocationListener(LocationListener locationListener) {
        locationListeners.remove(locationListener);
    }

    @SuppressWarnings("MissingPermission")
    public boolean acquireLocation(boolean forceUpdate) {
        if (locationManager != null) {
            if (Permission.LOCATION.isGranted()) {
                final List<String> providers = locationManager.getProviders(true);
                if (providers != null) {
                    if (forceUpdate || !tryLastKnownLocations(providers)) {
                        if (locationCollector != null) {
                            locationCollector.cancel();
                        }
                        locationCollector = new LocationCollector(providers);
                    } else {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean tryLastKnownLocations(List<String> providers) {
        final ArrayList<Location> lastKnownLocations = new ArrayList<>(providers.size());
        for (String provider : providers) {
            @SuppressLint("MissingPermission") final Location lastKnownLocation = locationManager.getLastKnownLocation(provider);
            if (lastKnownLocation != null) {
                lastKnownLocations.add(lastKnownLocation);
            }
        }

        final Location bestSuitableLocation = findBestSuitableLocation(lastKnownLocations);

        if (bestSuitableLocation != null) {
            updateLocation(bestSuitableLocation);
            return true;
        }

        return false;
    }

    private Location findBestSuitableLocation(@NonNull List<Location> locations) {
        if (locations.isEmpty()) {
            return null;
        }

        final long now = System.currentTimeMillis();

        Collections.sort(locations, new Comparator<Location>() {
            @Override
            public int compare(Location o1, Location o2) {
                return (int) (
                        (o2.getTime() - o1.getTime()) / 120 +
                        o2.getAccuracy() - o1.getAccuracy()
                ); // 1 minute of currency is worth 500 meters of accuracy
            }
        });

        for (Location location : locations) {
            if (isAcceptable(location, now)) {
                return location;
            }
        }

        return locations.get(0);
    }

    private boolean isAcceptable(Location location, long now) {
        return location.getAccuracy() < 500 && now - location.getTime() < 60 * 1000;
    }

    private boolean isGreat(Location location, long now) {
        return location.getAccuracy() < 50 && now - location.getTime() < 30 * 1000;
    }

    private void updateLocation(Location location) {
        if (location != null) {
            this.location = location;

            notifiyLocationListeners(location);
        }
    }

    private void notifiyLocationListeners(Location location) {
        for (LocationListener listener : locationListeners) {
            listener.onLocationChanged(location);
        }
    }

    @Override
    public void onStop() {
        if (locationCollector != null) {
            locationCollector.cancel();
        }

        super.onStop();
    }

    public static LocationFragment get(FragmentManager fragmentManager) {
        final Fragment fragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG);

        if (fragment instanceof LocationFragment) {
            return (LocationFragment) fragment;
        }

        final LocationFragment locationFragment = new LocationFragment();

        fragmentManager.beginTransaction()
                .add(locationFragment, FRAGMENT_TAG)
                .commit();

        return locationFragment;
    }

    private class LocationCollector extends BaseLocationListener {
        private final List<String> providers;

        private final List<Location> locations;

        @SuppressLint("MissingPermission")
        public LocationCollector(List<String> providers) {
            this.providers = providers;
            locations = new ArrayList<>(providers.size());

            for (String provider : providers) {
                locationManager.requestSingleUpdate(provider, this, Looper.getMainLooper());
            }
        }

        @Override
        public void onLocationChanged(Location location) {
            locations.add(location);

            if (isGreat(location, System.currentTimeMillis())) {
                providers.clear(); // exit early because we have a great candidate
            }

            onProviderDone(location.getProvider());
        }

        @Override
        public void onProviderDisabled(String provider) {
            onProviderDone(provider);
        }

        private void onProviderDone(String provider) {
            if (!providers.remove(provider)) {
                providers.remove("passive");
            }

            if (providers.isEmpty()) {
                onDone();
            }
        }

        private void onDone() {
            cancel();

            final Location bestSuitableLocation = findBestSuitableLocation(locations);

            if (bestSuitableLocation != null) {
                updateLocation(bestSuitableLocation);
            }
        }

        public void cancel() {
            locationCollector = null;
            locationManager.removeUpdates(this);
        }
    }
}
