/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository;

import java.util.List;

import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus;

public class ElevatorsResource extends RemoteResource<List<FacilityStatus>> {

    private String stationId;

    public void initialize(Station station) {
        stationId = station.getId();

        loadData(false);
    }

    @Override
    public boolean isLoadingPreconditionsMet() {
        return stationId != null;
    }

    @Override
    protected void onStartLoading(boolean force) {
        baseApplication.getRepositories().getElevatorStatusRepository()
                .queryStationElevatorStatuses(stationId, new Listener());
    }
}
