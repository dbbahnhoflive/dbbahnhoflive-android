package de.deutschebahn.bahnhoflive.ui.station.features

import de.deutschebahn.bahnhoflive.backend.bahnpark.model.BahnparkSite
import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus
import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.DetailedStopPlace
import de.deutschebahn.bahnhoflive.ui.station.StaticInfoCollection
import de.deutschebahn.bahnhoflive.ui.station.shop.CategorizedShops
import de.deutschebahn.bahnhoflive.ui.station.shop.Shop

class StationFeature(
        val stationFeatureTemplate: StationFeatureTemplate,
        val detailedStopPlace: DetailedStopPlace,
        val staticInfoCollection: StaticInfoCollection?,
        categorizedShops: CategorizedShops?,
        val bahnparkSites: List<BahnparkSite>?,
        val facilityStatuses: List<FacilityStatus>?
) {
    val venues: List<Shop>?

    val isFeatured: Boolean
        get() = stationFeatureTemplate.definition.availability.isAvailable(detailedStopPlace, this)

    val isVisible: Boolean
        get() = stationFeatureTemplate.definition.availability.isVisible(detailedStopPlace, this)

    val isLinkVisible: Boolean
        get() = (isFeatured
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
