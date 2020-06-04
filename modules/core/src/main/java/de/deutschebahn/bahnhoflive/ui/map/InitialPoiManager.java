package de.deutschebahn.bahnhoflive.ui.map;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public class InitialPoiManager {

    public static final String ARG_SOURCE = "initialPoiSource";
    public static final String ARG_POI = "initialPoi";
    public static final String STATE_DONE = "initialPoiManagerDone";

    private final Parcelable initialItem;

    @Nullable
    public final Content.Source source;

    private boolean done = false;

    public static void putInitialPoi(Intent intent, Content.Source source, Parcelable poiItem) {
        intent.putExtra(ARG_SOURCE, source);
        intent.putExtra(ARG_POI, poiItem);
    }

    public InitialPoiManager(Intent intent, Bundle savedInstanceState) {
        source = intent == null ? null : (Content.Source) intent.getSerializableExtra(ARG_SOURCE);

        if (savedInstanceState == null || !savedInstanceState.getBoolean(STATE_DONE, false)) {
            initialItem = intent.getParcelableExtra(ARG_POI);
        } else {
            initialItem = null;
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_DONE, done);
    }

    public boolean isInitial(MarkerBinder markerBinder) {
        return markerBinder.getMarkerContent().wraps(initialItem);
    }
}
