package de.deutschebahn.bahnhoflive.backend.db.ris.model

import de.deutschebahn.bahnhoflive.backend.db.ris.model.LocalService.Type.*

class LocalServices {
    var localServices: List<LocalService>? = null

    val localServicesByType by lazy {
        localServices?.associateBy { it.type } ?: emptyMap()
    }

    internal val travelCenters by lazy {
        localServices?.filter {
            it.type == TRAVEL_CENTER.tag
        }
    }

    var openHoursProcessingPending = true

    val hasPublicFacilities: Boolean get() = localServicesByType.contains(PUBLIC_RESTROOM.tag)

    val hasWifi: Boolean get() = localServicesByType.contains(WIFI.tag)
    val hasTaxiRank: Boolean get() = localServicesByType.contains(TAXI_RANK.tag)
    val hasLostAndFound: Boolean get() = localServicesByType.contains(LOST_PROPERTY_OFFICE.tag)
    val hasCarRental: Boolean get() = localServicesByType.contains(CAR_RENTAL.tag)
    val hasBicycleParking: Boolean get() = localServicesByType.contains(BICYCLE_PARKING.tag)
    val hasParking: Boolean get() = localServicesByType.contains(CAR_PARKING.tag)
    val hasTravelNecessities: Boolean get() = localServicesByType.contains(TRAVEL_NECESSITIES.tag)
    val hasDbLounge: Boolean get() = localServicesByType.contains(TRAVEL_LOUNGE.tag)
    val hasLockerSystem: Boolean get() = localServicesByType.contains(LOCKER.tag)
    val hasDbInformation: Boolean get() = localServicesByType.contains(INFORMATION_COUNTER.tag)
    val hasTravelCenter: Boolean get() = localServicesByType.contains(TRAVEL_CENTER.tag)
    val hasMobileService: Boolean get() = localServicesByType.contains(MOBILE_TRAVEL_SERVICE.tag)
    val hasRailwayMission: Boolean get() = localServicesByType.contains(RAILWAY_MISSION.tag)
    val hasMobilityService: Boolean
        get() = localServicesByType[HANDICAPPED_TRAVELLER_SERVICE.tag]?.let { handicappedTravelerServiceLocalService ->
            handicappedTravelerServiceLocalService.description != "no"
        } == true
    val hasSzentrale: Boolean get() = localServicesByType.contains(TRIPLE_S_CENTER.tag)

    fun get(type: LocalService.Type) = localServicesByType[type.tag]
    fun hasService(type: LocalService.Type): Boolean = get(type) != null
}