package de.deutschebahn.bahnhoflive.ui.station.features

import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.DistanceCalculator
import de.deutschebahn.bahnhoflive.backend.db.ris.model.Coordinate2D
import de.deutschebahn.bahnhoflive.backend.db.ris.model.LocalService
import de.deutschebahn.bahnhoflive.backend.db.ris.model.LocalServices
import de.deutschebahn.bahnhoflive.backend.db.ris.model.RISStation
import de.deutschebahn.bahnhoflive.util.openhours.OpenHoursParser

class RISServicesAndCategory(
    val station: RISStation?,
    val localServices: LocalServices?,
    val openHoursParser: OpenHoursParser
) : ServicesAndCategory {
    fun has(type: LocalService.Type): Boolean = localServices?.get(type) != null

    override val hasSzentrale: Boolean
        get() = localServices?.hasSzentrale == true
    override val hasMobilityService: Boolean
        get() = localServices?.hasMobilityService == true
    override val hasRailwayMission: Boolean
        get() = localServices?.hasRailwayMission == true
    override val hasMobileService: Boolean
        get() = localServices?.hasMobileService == true
    override val hasTaxiRank: Boolean
        get() = localServices?.hasTaxiRank == true
    override val hasLostAndFound: Boolean
        get() = localServices?.hasLostAndFound == true
    override val hasCarRental: Boolean
        get() = localServices?.hasCarRental == true
    override val hasBicycleParking: Boolean
        get() = localServices?.hasBicycleParking == true
    override val hasParking: Boolean
        get() = localServices?.hasParking == true
    override val hasTravelNecessities: Boolean
        get() = localServices?.hasTravelNecessities == true
    override val hasDbLounge: Boolean
        get() = localServices?.hasDbLounge == true
    override val hasTravelCenter: Boolean
        get() = localServices?.hasTravelCenter == true
    override val hasDbInformation: Boolean
        get() = localServices?.hasDbInformation == true
    override val hasLockerSystem: Boolean
        get() = localServices?.hasLockerSystem == true
    override val hasWifi: Boolean
        get() = localServices?.hasWifi == true
    override val hasPublicFacilities: Boolean
        get() = localServices?.hasPublicFacilities == true
    override val category: Int
        get() = station?.category ?: 0

    val closestTravelCenter
        get() = localServices?.travelCenters?.run {
            if (size > 1) {
                station?.position?.let {
                    DistanceCalculator(it.latitude, it.longitude).run {
                        nullsLast { position1: Coordinate2D, position2: Coordinate2D ->
                            ((calculateDistance(
                                position1.latitude,
                                position1.longitude
                            ) - calculateDistance(
                                position2.latitude,
                                position2.longitude
                            )) * 1000).toInt()
                        }
                    }
                }?.let { distanceComparator ->
                    sortedWith { travelCenter1, travelCenter2 ->
                        distanceComparator.compare(travelCenter1.position, travelCenter2.position)
                    }
                } ?: this
            } else {
                this
            }.firstOrNull()
        }

    fun prepareOpenHours(doneListener: () -> Unit) {
        if (localServices?.openHoursProcessingPending == true) {
            localServices.openHoursProcessingPending = false
            openHoursParser.visitAll(localServices, doneListener)
        }
    }
}