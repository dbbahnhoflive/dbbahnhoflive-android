/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds;
import de.deutschebahn.bahnhoflive.map.model.GeoPosition;


public interface Station {
    String getId();

    String getTitle();

    @Nullable
    GeoPosition getLocation();

    @NonNull
    EvaIds getEvaIds();
}
