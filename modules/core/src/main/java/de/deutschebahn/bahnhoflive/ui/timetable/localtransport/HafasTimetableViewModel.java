/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.timetable.localtransport;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.VolleyError;

import java.util.List;

import de.deutschebahn.bahnhoflive.backend.hafas.HafasDepartures;
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation;
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStationProduct;
import de.deutschebahn.bahnhoflive.backend.local.model.RrtPoint;
import de.deutschebahn.bahnhoflive.repository.HafasStationResource;
import de.deutschebahn.bahnhoflive.repository.HafasTimetableResource;
import de.deutschebahn.bahnhoflive.repository.MediatorResource;
import de.deutschebahn.bahnhoflive.repository.MergedStation;
import de.deutschebahn.bahnhoflive.repository.Resource;
import de.deutschebahn.bahnhoflive.repository.Station;
import de.deutschebahn.bahnhoflive.repository.StationResource;
import de.deutschebahn.bahnhoflive.util.ManagedObserver;
import de.deutschebahn.bahnhoflive.util.Token;

public class HafasTimetableViewModel extends AndroidViewModel {

    public static final String ORIGIN_STATION = "station";
    public final HafasTimetableResource hafasTimetableResource = new HafasTimetableResource();

    private final MediatorResource<HafasDepartures> mediatorResource = new MediatorResource<HafasDepartures>() {
        @Override
        protected boolean onRefresh() {
            return hafasTimetableResource.refresh() ||
                    hafasStationResource.refresh() ||
                    (stationResource != null && stationResource.refresh()) ||
                    super.onRefresh();
        }
    };

    private final HafasStationResource hafasStationResource = new HafasStationResource();
    public final MutableLiveData<RrtPoint> pendingRailReplacementPointLiveData = new MutableLiveData(null);

    private ManagedObserver<HafasStation> hafasStationObserver;
    private ManagedObserver<VolleyError> hafasStationErrorObserver;
    private ManagedObserver<MergedStation> stationObserver;
    private StationResource stationResource;

    private final Token initializationPending = new Token();
    private Station station;
    private List<HafasStation> hafasStations;

    public String filterName = null;

    public HafasTimetableViewModel(Application application) {
        super(application);
        mediatorResource.addSource(hafasTimetableResource);
    }

    public Resource<HafasDepartures, VolleyError> getHafasTimetableResource() {
        return mediatorResource;
    }

    /**
     * Initialization from {@link DeparturesActivity}
     */
    public void initialize(HafasStation hafasStation, @Nullable HafasDepartures departures, boolean filterStricly, Station station, List<HafasStation> hafasStations) {
        if (initializationPending.take()) {
            hafasTimetableResource.initialize(hafasStation, departures, filterStricly, HafasStationResource.ORIGIN_TIMETABLE);
            hafasStationResource.initialize(hafasStation);
            this.station = station;
            this.hafasStations = hafasStations;
        }
    }

    /**
     * Initialization from {@link de.deutschebahn.bahnhoflive.ui.station.StationActivity}
     */
    public void initialize(@NonNull final StationResource stationResource) {
        if (initializationPending.take()) {
            this.stationResource = stationResource;

            mediatorResource.addErrorSource(hafasStationResource);
            mediatorResource.addLoadingStatusSource(hafasStationResource);

            hafasStationObserver = new ManagedObserver<HafasStation>(hafasStationResource.getData()) {
                @Override
                public void onChanged(@Nullable HafasStation hafasStation) {
                    hafasTimetableResource.initialize(hafasStation, null, false, ORIGIN_STATION);

                    mediatorResource.removeSource(hafasStationResource);
                    destroy();
                    hafasStationObserver = null;
                }
            };
            hafasStationErrorObserver = new ManagedObserver<VolleyError>(hafasStationResource.getError()) {
                @Override
                public void onChanged(final VolleyError volleyError) {
                    if (volleyError != null) {
                        hafasTimetableResource.setError(volleyError);
                    }
                }
            };

            mediatorResource.addErrorSource(stationResource);
            mediatorResource.addLoadingStatusSource(stationResource);

            stationObserver = new ManagedObserver<MergedStation>(stationResource.getData()) {
                @Override
                public void onChanged(@Nullable MergedStation station) {
                    hafasStationResource.initialize(station);

                    if (hafasStationResource.isLoadingPreconditionsMet()) {
                        mediatorResource.removeSource(stationResource);
                        destroy();
                        stationObserver = null;
                    }
                }
            };

        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if (stationObserver != null) {
            stationObserver.destroy();
            stationObserver = null;
        }

        if (hafasStationObserver != null) {
            hafasStationObserver.destroy();
            hafasStationObserver = null;
        }

        if (hafasStationErrorObserver != null) {
            hafasStationErrorObserver.destroy();
            hafasStationErrorObserver = null;
        }
    }

    public HafasStationResource getHafasStationResource() {
        return hafasStationResource;
    }

    public void loadMore() {
        hafasTimetableResource.addHour();
        hafasTimetableResource.refresh();
    }

    public final MutableLiveData<HafasStationProduct> selectedHafasStationProduct = new MutableLiveData<>();

    @Nullable
    public Station getStation() {
        return station;
    }

    @Nullable
    public List<HafasStation> getHafasStations() {
        return hafasStations;
    }

}
