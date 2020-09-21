/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository;

import androidx.annotation.Nullable;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.android.volley.VolleyError;

import java.util.List;

import de.deutschebahn.bahnhoflive.backend.einkaufsbahnhof.model.StationResponse;
import de.deutschebahn.bahnhoflive.backend.rimap.model.RimapPOI;
import de.deutschebahn.bahnhoflive.ui.station.shop.CategorizedShops;

public class ShopsResource extends Resource<CategorizedShops, VolleyError> {

    private final MediatorLiveData<CategorizedShops> data;

    private MediatorLiveData<LoadingStatus> loadingStatus;
    private MediatorLiveData<VolleyError> error;

    private final Observer<LoadingStatus> loadingStatusForwarder = new Observer<LoadingStatus>() {
        @Override
        public void onChanged(@Nullable LoadingStatus loadingStatus) {
            ShopsResource.this.loadingStatus.setValue(loadingStatus);
        }
    };
    private final Observer<VolleyError> errorForwarder = new Observer<VolleyError>() {
        @Override
        public void onChanged(@Nullable VolleyError throwable) {
            error.setValue(throwable);
        }
    };

    public final RimapPOIListResource rimapPOIListResource = new RimapPOIListResource();
    private final EinkaufsbahnhofStationResponseResource einkaufsbahnhofStationResponseResource = new EinkaufsbahnhofStationResponseResource();

    private boolean skipRimap = false;

    private boolean skipEinkaufsbahnhof = false;

    public void initialize(Station station) {
        rimapPOIListResource.initialize(station);
        einkaufsbahnhofStationResponseResource.initialize(station.getId());

        rimapPOIListResource.load();
    }

    public ShopsResource() {
        this(new MediatorLiveData<CategorizedShops>(), new MediatorLiveData<LoadingStatus>(), new MediatorLiveData<VolleyError>());
    }

    private ShopsResource(final MediatorLiveData<CategorizedShops> data, MediatorLiveData<LoadingStatus> loadingStatus, final MediatorLiveData<VolleyError> error) {
        super(data, loadingStatus, error);

        this.data = data;
        this.loadingStatus = loadingStatus;
        this.error = error;

        loadingStatus.addSource(rimapPOIListResource.getLoadingStatus(), loadingStatusForwarder);
        loadingStatus.addSource(einkaufsbahnhofStationResponseResource.getLoadingStatus(), loadingStatusForwarder);
        error.addSource(einkaufsbahnhofStationResponseResource.getError(), errorForwarder);

        error.addSource(rimapPOIListResource.getError(), new Observer<VolleyError>() {
            @Override
            public void onChanged(@Nullable VolleyError volleyError) {
                if (volleyError == null) {
                    error.setValue(volleyError);
                }
            }
        });

        data.addSource(rimapPOIListResource.getError(), new Observer<VolleyError>() {
            @Override
            public void onChanged(@Nullable VolleyError volleyError) {
                if (volleyError != null) {
                    einkaufsbahnhofStationResponseResource.refresh();
                }
            }
        });

        data.addSource(rimapPOIListResource.getData(), new Observer<List<RimapPOI>>() {
            @Override
            public void onChanged(@Nullable List<RimapPOI> rimapPOIs) {
                if (rimapPOIs == null || rimapPOIs.isEmpty()) {
                    skipRimap = true;
                    einkaufsbahnhofStationResponseResource.load();
                    return;
                }

                final CategorizedShops categorizedShops = new CategorizedShops(rimapPOIs);
                data.setValue(categorizedShops);
            }
        });

        data.addSource(einkaufsbahnhofStationResponseResource.getData(), new Observer<StationResponse>() {
            @Override
            public void onChanged(@Nullable StationResponse stationResponse) {
                if (stationResponse == null || stationResponse.stores == null || stationResponse.stores.isEmpty()) {
                    skipEinkaufsbahnhof = true;
                    if (data.getValue() == null) {
                        data.setValue(null); // trigger value update without overwriting existing data
                    }
                    return;
                }

                final CategorizedShops categorizedShops = new CategorizedShops(stationResponse);
                data.setValue(categorizedShops);
            }
        });
    }

    @Override
    protected boolean onRefresh() {
        if (skipEinkaufsbahnhof) {
            super.onRefresh();
        } else if (skipRimap) {
            einkaufsbahnhofStationResponseResource.refresh();
        } else {
            rimapPOIListResource.refresh();
        }
        return false;
    }
}
