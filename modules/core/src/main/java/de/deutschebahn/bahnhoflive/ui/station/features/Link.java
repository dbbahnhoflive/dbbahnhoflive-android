package de.deutschebahn.bahnhoflive.ui.station.features;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.util.List;

import de.deutschebahn.bahnhoflive.ui.ServiceContentFragment;

public abstract class Link {
    @Nullable
    public List<? extends Parcelable> getPois(StationFeature stationFeature) {
        return null;
    }

    @Nullable
    public ServiceContentFragment createServiceContentFragment(Context context, StationFeature stationFeature) {
        return null;
    }

    @Nullable
    public Intent createMapActivityIntent(Context context, StationFeature stationFeature) {
        return null;
    }

    public boolean isAvailable(StationFeature stationFeature) {
        return true;
    }
}
