/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.einkaufsbahnhof.model;

import java.util.List;

public class StationList {
    public int status;

    public int numberOfStations;

    public List<Station> stations;
}
