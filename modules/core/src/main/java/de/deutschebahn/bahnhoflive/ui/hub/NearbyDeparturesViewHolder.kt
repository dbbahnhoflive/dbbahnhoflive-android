/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.hub

import android.location.Location
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.DistanceCalculator
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation
import de.deutschebahn.bahnhoflive.ui.search.HafasStationSearchResult
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager

internal class NearbyDeparturesViewHolder(
    parent: View,
    owner: LifecycleOwner?,
    singleSelectionManager: SingleSelectionManager?,
    trackingManager: TrackingManager,
    locationMutableLiveData: MutableLiveData<Location>?
) : DeparturesViewHolder(
    parent,
//    R.layout.card_nearby_departures,
    owner,
    singleSelectionManager,
    trackingManager,
    null,
    TrackingManager.UiElement.ABFAHRT_NAEHE_OPNV
) {
    private val distanceViewHolder: DistanceViewHolder = DistanceViewHolder(itemView)
    private var locationMutableLiveData: MutableLiveData<Location>? = null

    init {
        this.locationMutableLiveData = locationMutableLiveData
    }

    private fun calculateDistance(station: HafasStation, location: Location?): Float {
        return if (location != null) {
            val distanceCalculator = DistanceCalculator(
                location.latitude,
                location.longitude
            )
            distanceCalculator.calculateDistance(station.latitude, station.longitude)
        } else {
            -1.0f
        }
    }

    override fun onBind(item: HafasStationSearchResult?) {
        super.onBind(item)
        item?.let {
            val distance = calculateDistance(
                it.timetable.station,
                locationMutableLiveData?.value
            )
            distanceViewHolder.bind(distance)
        }
    }
}
