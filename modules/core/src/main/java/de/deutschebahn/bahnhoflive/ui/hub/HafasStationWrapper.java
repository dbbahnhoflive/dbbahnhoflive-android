/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.hub;

import androidx.annotation.NonNull;

import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation;
import de.deutschebahn.bahnhoflive.persistence.FavoriteStationsStore;
import de.deutschebahn.bahnhoflive.ui.StationWrapper;
import de.deutschebahn.bahnhoflive.ui.search.HafasStationSearchResult;

public class HafasStationWrapper extends HafasStationSearchResult implements StationWrapper<HafasStation> {
    private final long timestamp;

    public HafasStationWrapper(HafasStation station, FavoriteStationsStore<HafasStation> favoriteStationsStore, long timestamp) {
        super(station, null, favoriteStationsStore);
        this.timestamp = timestamp;
    }


    @Override
    public long getFavoriteTimestamp() {
        return timestamp;
    }

    @Override
    public boolean wraps(Object o) {
        return o == null ? getTimetable() == null : o.equals(getTimetable());
    }

    @NonNull
    @Override
    public HafasStation getWrappedStation() {
        return getTimetable().getStation();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof HafasStationWrapper ? getTimetable().equals(((HafasStationWrapper) obj).getTimetable()) : super.equals(obj);
    }

    @Override
    public int hashCode() {
        return getTimetable().hashCode();
    }
}
