/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.features;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface Availability {

    @Nullable
    Boolean isAvailable(ServicesAndCategory servicesAndCategory, StationFeature stationFeature);

    boolean isVisible(@NonNull ServicesAndCategory servicesAndCategory, StationFeature stationFeature);
}
