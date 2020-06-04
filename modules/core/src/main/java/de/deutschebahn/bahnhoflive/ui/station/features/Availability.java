package de.deutschebahn.bahnhoflive.ui.station.features;

import androidx.annotation.NonNull;

import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.DetailedStopPlace;

public interface Availability {
    boolean isAvailable(DetailedStopPlace detailedStopPlace, StationFeature stationFeature);

    boolean isVisible(@NonNull DetailedStopPlace detailedStopPlace, StationFeature stationFeature);
}
