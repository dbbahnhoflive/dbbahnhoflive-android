package de.deutschebahn.bahnhoflive.ui.station.features

interface ServicesAndCategory {
    val hasSzentrale: Boolean
    val hasMobilityService: Boolean
    val hasRailwayMission: Boolean
    val hasMobileService: Boolean
    val hasTaxiRank: Boolean
    val hasLostAndFound: Boolean
    val hasCarRental: Boolean
    val hasBicycleParking: Boolean
    val hasParking: Boolean
    val hasTravelNecessities: Boolean
    val hasDbLounge: Boolean
    val hasTravelCenter: Boolean
    val hasDbInformation: Boolean
    val hasLockerSystem: Boolean
    val hasWifi: Boolean
    val hasPublicFacilities: Boolean
    val category: Int
}