package de.deutschebahn.bahnhoflive.location;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GPSLocationManager {

    public interface GPSLocationManagerListener {
        void didUpdateLocation(Location location);
    }

    public static GPSLocationManager gpsLocationManager;
    private static LocationManager locationManager;

    public GPSLocationManager() {
    }

    public static GPSLocationManager getInstance(Activity activity) {
        if (gpsLocationManager == null) {
            gpsLocationManager = new GPSLocationManager();

            if (!isLocationServicesAllowed(activity)) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                        2);
            } else {
                locationManager = getLocationManager(activity);
            }
        }
        return gpsLocationManager;
    }

    public static LocationManager getLocationManager(Context context) {
        return (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public static boolean isLocationServicesAllowed(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            Log.d(GPSLocationManager.class.getSimpleName(), "ACCESS_FINE_LOCATION Permission not granted");
            return false;
        } else {
            return true;
        }
    }

    public @Nullable
    Location getMyLocation(Context context) {

        Location gpsLocation = null;
        Location networkLocation = null;

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } else if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        Location myLocation = null;
        Date now = new Date();

        if (gpsLocation == null && networkLocation == null) {
            myLocation = getBestLastKnownLocation(context);
        } else {

            if (gpsLocation != null) {
                myLocation = gpsLocation;
            } else if (networkLocation != null) {
                myLocation = networkLocation;
            }

            double deltaTime = now.getTime()-myLocation.getTime();
            if (deltaTime > 300000) { // 5 minutes
                myLocation = getBestLastKnownLocation(context);
            }
        }

        if (myLocation != null) {
            Log.d(getClass().getSimpleName(), String.format("getMyLocation() = %s", myLocation.toString()));
        } else {
            Log.d(getClass().getSimpleName(), "no location available");
        }

        return myLocation;
    }

    public void requestSingleLocation(Context context, final GPSLocationManagerListener listener) {
        if (isLocationServicesAllowed(context))

            Log.d(GPSLocationManager.this.getClass().getSimpleName(), "requestSingleLocation()");
        final LocationManager locationManager = getLocationManager(context);
        List<String> providers = locationManager.getProviders(true);
        if (providers == null) {
            return;
        }
        List<Location> locations = new ArrayList<>();
        for (String p: providers) {
            locations.add(locationManager.getLastKnownLocation(p));

            locationManager.requestSingleUpdate(p, new BaseLocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.e(GPSLocationManager.this.getClass().getSimpleName(),"new location");

                    if (listener != null) {
                        listener.didUpdateLocation(location);
                    }

                    locationManager.removeUpdates(this);
                }
            }, context.getMainLooper());
        }

        if (locations.size() > 0) {
            Location lastknownLocation = getNewestLocation(locations);
            if (listener != null && lastknownLocation != null) {
                listener.didUpdateLocation(lastknownLocation);
            }
        }
    }

    private Location getNewestLocation(List<Location> locations) {
        Location result = null;
        if (locations!=null) {
            for (Location l: locations) {
                if (result == null) {
                    result = l;
                } else {
                    if (l!=null && l.getTime() > result.getTime()) {
                        result = l;
                    }
                }
            }
        }
        return result;
    }

    private Location getBestLastKnownLocation(Context context) {
        Criteria criteria = new Criteria();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
        } else if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        }

        String provider = locationManager.getBestProvider(criteria, true);
        return locationManager.getLastKnownLocation(provider);
    }
}
