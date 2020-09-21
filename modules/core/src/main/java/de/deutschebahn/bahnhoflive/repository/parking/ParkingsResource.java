/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository.parking;

import android.os.Handler;
import android.os.Looper;

import java.util.List;

import de.deutschebahn.bahnhoflive.BaseApplication;
import de.deutschebahn.bahnhoflive.model.parking.ParkingFacility;
import de.deutschebahn.bahnhoflive.repository.RemoteResource;
import de.deutschebahn.bahnhoflive.repository.Station;

public class ParkingsResource extends RemoteResource<List<ParkingFacility>> {

    private String stationId;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onStartLoading(boolean force) {
        BaseApplication.get().getRepositories().getParkingRepository().queryFacilities(stationId, new Listener());
    }

    @Override
    public boolean isLoadingPreconditionsMet() {
        return stationId != null;
    }

    public void initialize(final Station station) {
        this.stationId = station.getId();

        loadData(false);
    }
}
