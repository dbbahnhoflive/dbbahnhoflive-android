/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.rimap.model;

public class StationProperties {
    public String name;
    public String zoneid;
    public String evanr;

    public Double lon;
    public Double lat;

    public Integer level_b4;
    public Integer level_b3;
    public Integer level_b2;
    public Integer level_b1;
    public Integer level_l0;
    public Integer level_l1;
    public Integer level_l2;
    public Integer level_l3;
    public Integer level_l4;

    public String level_init;
}
