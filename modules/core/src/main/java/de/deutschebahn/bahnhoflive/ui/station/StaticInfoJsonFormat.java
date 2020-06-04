package de.deutschebahn.bahnhoflive.ui.station;

import java.util.List;

import de.deutschebahn.bahnhoflive.ui.station.info.StaticInfo;

class StaticInfoJsonFormat {

    final List<StaticInfo> staticInfo;

    public StaticInfoJsonFormat(List<StaticInfo> staticInfo) {
        this.staticInfo = staticInfo;
    }
}
