package de.deutschebahn.bahnhoflive.repository;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds;


public interface Station {
    String getId();

    String getTitle();

    @Nullable
    LatLng getLocation();

    @NonNull
    EvaIds getEvaIds();
}
