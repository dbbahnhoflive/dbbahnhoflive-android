/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.deutschebahn.bahnhoflive.ui.station.info.StaticInfo;

public class StaticInfoCollection {

    @NonNull
    public final HashMap<String, StaticInfo> typedStationInfos;
    final List<String> orderedTypes;

    public StaticInfoCollection(StaticInfoJsonFormat staticInfoJsonFormat) {
        orderedTypes = new ArrayList<>();
        typedStationInfos = new HashMap<>();

        if (staticInfoJsonFormat == null) {
            return;
        }

        for (StaticInfo staticInfo : staticInfoJsonFormat.staticInfo) {
            orderedTypes.add(staticInfo.type);
            typedStationInfos.put(staticInfo.type, staticInfo);
        }
    }
}
