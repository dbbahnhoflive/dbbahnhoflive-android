/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui;

import de.deutschebahn.bahnhoflive.persistence.FavoriteStationsStore;
import de.deutschebahn.bahnhoflive.repository.DbTimetableResource;
import de.deutschebahn.bahnhoflive.repository.InternalStation;
import de.deutschebahn.bahnhoflive.ui.search.StoredStationSearchResult;

public class DbStationWrapper extends StoredStationSearchResult implements StationWrapper<InternalStation> {

    private final DbTimetableResource dbTimetableResource;
    private final long timestamp;

    public DbStationWrapper(InternalStation station, FavoriteStationsStore<InternalStation> favoriteStationsStore, long timestamp) {
        super(station, null, favoriteStationsStore);
        this.dbTimetableResource = new DbTimetableResource(station);
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DbStationWrapper ?
                dbTimetableResource.equals(((DbStationWrapper) obj).dbTimetableResource) :
                super.equals(obj);
    }

    @Override
    public long getFavoriteTimestamp() {
        return timestamp;
    }

    @Override
    public boolean wraps(Object o) {
        if (o instanceof InternalStation) {
            return dbTimetableResource != null && dbTimetableResource.getStationId() != null && ((InternalStation) o).getId().equals(dbTimetableResource.getStationId());
        }

        return false;
    }

    @Override
    public InternalStation getWrappedStation() {
        return dbTimetableResource.getInternalStation();
    }

    @Override
    public int hashCode() {
        return dbTimetableResource.hashCode();
    }
}
