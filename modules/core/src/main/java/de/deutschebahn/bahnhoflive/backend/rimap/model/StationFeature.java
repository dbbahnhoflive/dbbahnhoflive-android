/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.rimap.model;

public class StationFeature {
    public final StationProperties properties;

    public StationFeature(StationProperties properties) {
        this.properties = properties;
    }
}
