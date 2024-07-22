/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository;


import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import de.deutschebahn.bahnhoflive.backend.db.ris.model.Coordinate2D;
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds;


public interface Station {
    String getId();

    String getTitle();

    @Nullable
    LatLng getLocation();

    void setPosition(Coordinate2D position);

    @Nullable
    EvaIds getEvaIds();

    void addEvaIds(EvaIds ids);
}
