/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.features

import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus
import de.deutschebahn.bahnhoflive.model.parking.ParkingFacility
import de.deutschebahn.bahnhoflive.repository.Station
import de.deutschebahn.bahnhoflive.ui.station.StaticInfoCollection
import de.deutschebahn.bahnhoflive.ui.station.shop.CategorizedShops
import de.deutschebahn.bahnhoflive.ui.station.shop.Shop

class StationFeature(
    val station: Station,
    val stationFeatureTemplate: StationFeatureTemplate,
    val risServicesAndCategory: RISServicesAndCategory,
    val staticInfoCollection: StaticInfoCollection?,
    categorizedShops: CategorizedShops?,
    val parkingFacilities: List<ParkingFacility>?,
    val facilityStatuses: List<FacilityStatus>?
) {
    val venues: List<Shop>?

    val isFeatured: Boolean?
        get() = stationFeatureTemplate.definition.availability.isAvailable(
            risServicesAndCategory,
            this
        )

    val isVisible: Boolean
        get() = stationFeatureTemplate.definition.availability.isVisible(
            risServicesAndCategory,
            this
        )

    val isLinkVisible: Boolean
        get() = (isFeatured != false
                && (stationFeatureTemplate.link?.isAvailable(this)) == true)

    init {

        var venues: List<Shop>? = null
        if (categorizedShops != null) {
            val featureVenues = categorizedShops.featureVenues
            if (featureVenues != null) {
                venues = featureVenues[stationFeatureTemplate.definition.venueFeature]
            }
        }
        this.venues = venues
    }
}
