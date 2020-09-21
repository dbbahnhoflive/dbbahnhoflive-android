/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.einkaufsbahnhof.model;

import java.util.List;

public class StationResponse {
    public final int status;

    public final Station station;

    public final List<Store> stores;

    public StationResponse(int status, Station station, List<Store> stores) {
        this.status = status;
        this.station = station;
        this.stores = stores;
    }
}
