/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.persistence;

import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation;
import de.deutschebahn.bahnhoflive.ui.StationWrapper;
import de.deutschebahn.bahnhoflive.ui.hub.HafasStationWrapper;

public class HafasStationItemAdapter implements FavoriteStationsStore.ItemAdapter<HafasStation> {
    @Override
    public String getId(HafasStation item) {
        return item.extId;
    }

    @Override
    public Class<HafasStation> getItemClass() {
        return HafasStation.class;
    }

    @Override
    public StationWrapper<HafasStation> createStationWrapper(HafasStation station, long timestamp, FavoriteStationsStore<HafasStation> favoriteStationsStore) {
        return new HafasStationWrapper(station, favoriteStationsStore, timestamp);
    }
}
