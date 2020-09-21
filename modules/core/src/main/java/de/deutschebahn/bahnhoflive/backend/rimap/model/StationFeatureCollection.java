/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.rimap.model;

import java.util.List;

public class StationFeatureCollection {
    public final List<StationFeature> features;

    public StationFeatureCollection(List<StationFeature> features) {
        this.features = features;
    }
}
