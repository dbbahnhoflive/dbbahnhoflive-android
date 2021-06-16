/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.features

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.DetailedStopPlace
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContentType
import de.deutschebahn.bahnhoflive.repository.VenueFeature
import de.deutschebahn.bahnhoflive.util.Collections

class StationFeatureDefinition(
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
    val serviceContentType: String,
    val venueFeature: VenueFeature?,
    val availability: Availability
) {

    private abstract class BasicAvailability : Availability {

        override fun isVisible(detailedStopPlace: DetailedStopPlace, stationFeature: StationFeature): Boolean {
            return true
        }
    }

    abstract class CategoryAvailability protected constructor(private val hideFromCategory: Int) : Availability {

        override fun isVisible(detailedStopPlace: DetailedStopPlace, stationFeature: StationFeature): Boolean {
            return hideFromCategory > detailedStopPlace.category || isAvailable(detailedStopPlace, stationFeature)
        }
    }

    companion object {

        val ACCESSIBILITY = StationFeatureDefinition(R.string.feature_accessibility,
            R.drawable.bahnhofsausstattung_stufenfreier_zugang,
            ServiceContentType.ACCESSIBLE,
            null,
            object : BasicAvailability() {
                override fun isAvailable(
                    detailedStopPlace: DetailedStopPlace,
                    stationFeature: StationFeature
                ): Boolean {
                    return detailedStopPlace.hasSteplessAccess
                }
            })
        val TOILET = StationFeatureDefinition(R.string.feature_toilet,
            R.drawable.bahnhofsausstattung_wc,
            ServiceContentType.WC,
            VenueFeature.WC,
            object : BasicAvailability() {
                override fun isAvailable(
                    detailedStopPlace: DetailedStopPlace,
                    stationFeature: StationFeature
                ): Boolean {
                    return detailedStopPlace.hasPublicFacilities
                }
            })
        val WIFI = StationFeatureDefinition(R.string.feature_wifi,
            R.drawable.rimap_wlan_grau,
            ServiceContentType.WIFI,
            VenueFeature.WIFI,
            object : CategoryAvailability(4) {
                override fun isAvailable(
                    detailedStopPlace: DetailedStopPlace,
                    stationFeature: StationFeature
                ): Boolean {
                    return detailedStopPlace.hasWifi
                }
            })
        val ELEVATORS = StationFeatureDefinition(
            R.string.feature_elevators,
            R.drawable.bahnhofsausstattung_aufzug,
            ServiceContentType.ELEVATION_AIDS,
            VenueFeature.ELEVATION_AIDS,
            object : Availability {
                override fun isAvailable(
                    detailedStopPlace: DetailedStopPlace,
                    stationFeature: StationFeature
                ): Boolean {
                    return isVisible(detailedStopPlace, stationFeature)
                }

                override fun isVisible(
                    detailedStopPlace: DetailedStopPlace,
                    stationFeature: StationFeature
                ): Boolean {
                    return Collections.hasContent(stationFeature.facilityStatuses)
                }
            })
        val LOCKERS = StationFeatureDefinition(R.string.feature_lockers,
            R.drawable.bahnhofsausstattung_schlie_faecher,
            ServiceContentType.LOCKERS,
            VenueFeature.LOCKERS,
            object : BasicAvailability() {
                override fun isAvailable(
                    detailedStopPlace: DetailedStopPlace,
                    stationFeature: StationFeature
                ): Boolean {
                    return detailedStopPlace.hasLockerSystem
                }
            })
        val DB_INFO = StationFeatureDefinition(R.string.feature_db_info,
            R.drawable.bahnhofsausstattung_db_info,
            ServiceContentType.DB_INFORMATION,
            VenueFeature.DB_INFORMATION,
            object : CategoryAvailability(4) {
                override fun isAvailable(
                    detailedStopPlace: DetailedStopPlace,
                    stationFeature: StationFeature
                ): Boolean {
                    return detailedStopPlace.hasDbInformation
                }
            })
        val TRAVEL_CENTER = StationFeatureDefinition(R.string.feature_db_reisezentrum,
            R.drawable.bahnhofsausstattung_db_reisezentrum,
            ServiceContentType.Local.TRAVEL_CENTER,
            VenueFeature.TRAVEL_CENTER,
            object : BasicAvailability() {
                override fun isAvailable(
                    detailedStopPlace: DetailedStopPlace,
                    stationFeature: StationFeature
                ): Boolean {
                    return detailedStopPlace.hasTravelCenter
                }
            })
        val DB_LOUNGE = StationFeatureDefinition(
            R.string.feature_db_lounge,
            R.drawable.bahnhofsausstattung_db_lounge,
            ServiceContentType.Local.DB_LOUNGE,
            VenueFeature.DB_LOUNGE,
            object : CategoryAvailability(4) {
                override fun isAvailable(
                    detailedStopPlace: DetailedStopPlace,
                    stationFeature: StationFeature
                ): Boolean {
                    return detailedStopPlace.hasDbLounge
                }
            })
        val TRAVELER_SUPPLIES = StationFeatureDefinition(R.string.feature_traveler_supplies,
            R.drawable.bahnhofsausstattung_reisebedarf,
            ServiceContentType.TRAVELERS_SUPPLIES,
            null,
            object : BasicAvailability() {
                override fun isAvailable(
                    detailedStopPlace: DetailedStopPlace,
                    stationFeature: StationFeature
                ): Boolean {
                    return detailedStopPlace.hasTravelNecessities
                }
            })
        val PARKING = StationFeatureDefinition(R.string.feature_parkings,
            R.drawable.bahnhofsausstattung_parkplatz,
            ServiceContentType.PARKING,
            VenueFeature.PARKING,
            object : CategoryAvailability(4) {
                override fun isAvailable(
                    detailedStopPlace: DetailedStopPlace,
                    stationFeature: StationFeature
                ): Boolean {
                    return detailedStopPlace.hasParking
                }
            })
        val BICYCLE_PARKING = StationFeatureDefinition(R.string.feature_bicycle,
            R.drawable.bahnhofsausstattung_fahrradstellplatz,
            ServiceContentType.BICYCLE,
            VenueFeature.BYCICLE_PARKING,
            object : CategoryAvailability(4) {
                override fun isAvailable(
                    detailedStopPlace: DetailedStopPlace,
                    stationFeature: StationFeature
                ): Boolean {
                    return detailedStopPlace.hasBicycleParking
                }
            })
        val CAR_RENTAL = StationFeatureDefinition(R.string.feature_rental,
            R.drawable.bahnhofsausstattung_mietwagen,
            ServiceContentType.CAR_RENTAL,
            VenueFeature.CAR_RENTAL,
            object : CategoryAvailability(4) {
                override fun isAvailable(
                    detailedStopPlace: DetailedStopPlace,
                    stationFeature: StationFeature
                ): Boolean {
                    return detailedStopPlace.hasCarRental
                }
            })
        val LOST_AND_FOUND = StationFeatureDefinition(R.string.feature_lost_and_found,
            R.drawable.bahnhofsausstattung_fundservice,
            ServiceContentType.Local.LOST_AND_FOUND,
            VenueFeature.LOST_AND_FOUND,
            object : CategoryAvailability(4) {
                override fun isAvailable(
                    detailedStopPlace: DetailedStopPlace,
                    stationFeature: StationFeature
                ): Boolean {
                    return detailedStopPlace.hasLostAndFound
                }
            })
        val TAXI = StationFeatureDefinition(R.string.feature_taxi,
            R.drawable.bahnhofsausstattung_taxi,
            ServiceContentType.TAXI,
            VenueFeature.TAXI,
            object : CategoryAvailability(4) {
                override fun isAvailable(
                    detailedStopPlace: DetailedStopPlace,
                    stationFeature: StationFeature
                ): Boolean {
                    return detailedStopPlace.hasTaxiRank
                }
            })
    }
}
