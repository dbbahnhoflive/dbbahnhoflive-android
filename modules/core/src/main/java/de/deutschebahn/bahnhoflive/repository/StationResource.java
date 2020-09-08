package de.deutschebahn.bahnhoflive.repository;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.android.volley.VolleyError;

import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.DetailedStopPlace;
import de.deutschebahn.bahnhoflive.backend.rimap.model.StationFeatureCollection;

public class StationResource extends MediatorResource<Station> {

    private final DetailedStopPlaceResource detailedStopPlaceResource;
    private final RimapStationFeatureCollectionResource rimapStationFeatureCollectionResource;

    private final Observer<VolleyError> rimapErrorObserver = new Observer<VolleyError>() {
        @Override
        public void onChanged(@Nullable VolleyError volleyError) {
            final StationFeatureCollection stationFeatureCollection = rimapStationFeatureCollectionResource.getData().getValue();
            if (volleyError == null && (stationFeatureCollection == null || !stationFeatureCollection.features.isEmpty())) {
                clearRimapError();
            }
        }
    };

    private Observer<StationFeatureCollection> rimapDataObserver = new Observer<StationFeatureCollection>() {
        @Override
        public void onChanged(@Nullable StationFeatureCollection stationFeatureCollection) {
            final RimapStationWrapper rimapStationWrapper = RimapStationWrapper.wrap(stationFeatureCollection);
            if (rimapStationWrapper != null) {
                data.setValue(rimapStationWrapper);
                loadingStatus.setValue(LoadingStatus.IDLE);
            }
        }
    };


    public StationResource(String id) {
        this();

        detailedStopPlaceResource.initialize(id);
        rimapStationFeatureCollectionResource.initialize(id);
    }

    public StationResource() {
        this(new DetailedStopPlaceResource(), new RimapStationFeatureCollectionResource());
    }

    public StationResource(final DetailedStopPlaceResource detailedStopPlaceResource,
                           final RimapStationFeatureCollectionResource rimapStationFeatureCollectionResource) {
        this.detailedStopPlaceResource = detailedStopPlaceResource;
        this.rimapStationFeatureCollectionResource = rimapStationFeatureCollectionResource;

        data.addSource(detailedStopPlaceResource.getData(), new Observer<DetailedStopPlace>() {
            @Override
            public void onChanged(@Nullable DetailedStopPlace detailedStopPlace) {
                final DetailedStopPlaceStationWrapper station = DetailedStopPlaceStationWrapper.Companion.of(detailedStopPlace);
                if (station != null) {
                    data.setValue(station);
                }
                loadingStatus.setValue(LoadingStatus.IDLE);
            }
        });
        data.addSource(detailedStopPlaceResource.getError(), new Observer<VolleyError>() {
            @Override
            public void onChanged(@Nullable VolleyError volleyError) {
                if (volleyError == null) {
                    clearStadaError();
                } else {
                    rimapStationFeatureCollectionResource.loadIfNecessary();
                    data.addSource(rimapStationFeatureCollectionResource.getMutableData(), rimapDataObserver);
                    data.addSource(rimapStationFeatureCollectionResource.getMutableError(), rimapErrorObserver);
                }
            }
        });
    }

    private void clearStadaError() {
        data.removeSource(rimapStationFeatureCollectionResource.getMutableData());
        data.removeSource(rimapStationFeatureCollectionResource.getMutableError());

        clearRimapError();
    }

    private void clearRimapError() {
        error.setValue(null);
        loadingStatus.setValue(LoadingStatus.IDLE);
    }

    @Override
    protected boolean onRefresh() {
        final boolean loading = detailedStopPlaceResource.loadIfNecessary();
        if (loading) {
            loadingStatus.setValue(LoadingStatus.BUSY);
        }
        return loading;
    }

    public void initialize(Station station) {
        if (station != null) {
            if (data.getValue() == null) {
                data.setValue(station);
            }

            detailedStopPlaceResource.initialize(station);
            rimapStationFeatureCollectionResource.initialize(station);
        }
    }
}
