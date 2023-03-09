/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.hub;
import android.location.Location;
import android.view.ViewGroup;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.analytics.TrackingManager;
import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.DistanceCalculator;
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation;
import de.deutschebahn.bahnhoflive.ui.search.HafasStationSearchResult;
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager;

class NearbyDeparturesViewHolder extends DeparturesViewHolder {

    private final DistanceViewHolder distanceViewHolder;

    private MutableLiveData<Location> locationMutableLiveData=null;

    public NearbyDeparturesViewHolder(ViewGroup parent,
                                      LifecycleOwner owner,
                                      SingleSelectionManager singleSelectionManager,
                                      TrackingManager trackingManager,
                                      MutableLiveData<Location> locationMutableLiveData) {
        super(parent, R.layout.card_nearby_departures, owner, singleSelectionManager, trackingManager, null, TrackingManager.UiElement.ABFAHRT_NAEHE_OPNV);
        distanceViewHolder = new DistanceViewHolder(itemView);
        this.locationMutableLiveData = locationMutableLiveData;
    }

    float calculateDistance(HafasStation station, Location location ) {

      if(location!=null) {
          DistanceCalculator distanceCalculator = new DistanceCalculator(location.getLatitude(),
                  location.getLongitude());

          return distanceCalculator.calculateDistance(station.latitude, station.longitude);
      }
      else
          return -1.0f;
    }

    @Override
    protected void onBind(HafasStationSearchResult item) {
        if(item!=null) {
        super.onBind(item);

            float distance = calculateDistance(item.getTimetable().station,
                    locationMutableLiveData.getValue());
            distanceViewHolder.bind(distance);
    }
        }
}
