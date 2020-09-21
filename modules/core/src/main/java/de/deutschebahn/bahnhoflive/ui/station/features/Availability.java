/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.features;

import androidx.annotation.NonNull;

import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.DetailedStopPlace;

public interface Availability {
    boolean isAvailable(DetailedStopPlace detailedStopPlace, StationFeature stationFeature);

    boolean isVisible(@NonNull DetailedStopPlace detailedStopPlace, StationFeature stationFeature);
}
