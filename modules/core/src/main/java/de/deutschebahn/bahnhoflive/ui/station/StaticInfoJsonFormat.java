/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station;

import java.util.List;

import de.deutschebahn.bahnhoflive.ui.station.info.StaticInfo;

class StaticInfoJsonFormat {

    final List<StaticInfo> staticInfo;

    public StaticInfoJsonFormat(List<StaticInfo> staticInfo) {
        this.staticInfo = staticInfo;
    }
}
