/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository;

import java.util.concurrent.CountDownLatch;

import de.deutschebahn.bahnhoflive.backend.BaseRestListener;
import de.deutschebahn.bahnhoflive.backend.db.ris.model.LocalServices;
import de.deutschebahn.bahnhoflive.backend.db.ris.model.RISStation;
import de.deutschebahn.bahnhoflive.repository.station.StationRepository;
import de.deutschebahn.bahnhoflive.ui.station.features.RISServicesAndCategory;
import de.deutschebahn.bahnhoflive.util.openhours.OpenHoursParser;
import kotlin.Unit;

public class RisServiceAndCategoryResource extends RemoteResource<RISServicesAndCategory> {

    private final OpenHoursParser openHoursParser;
    private String stadaId;

    public RisServiceAndCategoryResource(OpenHoursParser openHoursParser) {
        this.openHoursParser = openHoursParser;
    }

    public void initialize(String id) {
        this.stadaId = id;

        loadData(false);
    }

    class Collector {
        private RISStation risStation;
        private LocalServices localServices;

        private final CountDownLatch countDownLatch = new CountDownLatch(2);
        private final Listener listener = new Listener();

        final BaseRestListener<RISStation> stationListener = new BaseRestListener<RISStation>() {
            @Override
            public void onSuccess(RISStation payload) {
                risStation = payload;
                super.onSuccess(payload);
            }

            @Override
            public void onDone() {
                testDone();
            }
        };
        final BaseRestListener<LocalServices> localServicesListener = new BaseRestListener<LocalServices>() {
            @Override
            public void onSuccess(LocalServices payload) {
                localServices = payload;
                super.onSuccess(payload);
            }

            @Override
            public void onDone() {
                testDone();
            }
        };

        synchronized private void testDone() {
            countDownLatch.countDown();
            if (countDownLatch.getCount() < 1L) {
                final RISServicesAndCategory risServicesAndCategory = new RISServicesAndCategory(risStation, localServices, openHoursParser);
                risServicesAndCategory.prepareOpenHours(() -> {
                    listener.onSuccess(risServicesAndCategory);
                    return Unit.INSTANCE;
                });
            }
        }
    }

    @Override
    protected void onStartLoading(boolean force) {
        if (force || getMutableData().getValue() == null) {
            final Collector collector = new Collector();

            final StationRepository stationRepository = baseApplication.getRepositories().getStationRepository();
            stationRepository.queryStation(
                    collector.stationListener, stadaId, force, null);
            stationRepository.queryLocalServices(
                    collector.localServicesListener, stadaId, force, null
            );
        }
    }

    @Override
    public boolean isLoadingPreconditionsMet() {
        return stadaId != null;
    }

    public void initialize(Station station) {
        if (station != null) {
            initialize(station.getId());
        }
    }
}
