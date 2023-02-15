/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.persistence;

import de.deutschebahn.bahnhoflive.repository.InternalStation;
import de.deutschebahn.bahnhoflive.ui.DbStationWrapper;
import de.deutschebahn.bahnhoflive.ui.StationWrapper;

/**
 * Warning! Might be persisted by {@link de.deutschebahn.bahnhoflive.persistence.FavoriteStationsStore}
 */
public class InternalStationItemAdapter implements FavoriteStationsStore.ItemAdapter<InternalStation> {

    public InternalStationItemAdapter() {
    }

    @Override
    public String getId(InternalStation item) {
        return item.getId();
    }

    @Override
    public Class<InternalStation> getItemClass() {
        return InternalStation.class;
    }

    @Override
    public StationWrapper createStationWrapper(InternalStation station, long timestamp, FavoriteStationsStore<InternalStation> favoriteStationsStore) {
        return new DbStationWrapper(station, favoriteStationsStore, timestamp, null, null); // #cr
    }

}
