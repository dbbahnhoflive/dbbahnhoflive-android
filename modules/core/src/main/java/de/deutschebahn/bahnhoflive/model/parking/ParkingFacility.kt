package de.deutschebahn.bahnhoflive.model.parking

import de.deutschebahn.bahnhoflive.R

data class ParkingFacility(
    val id: String,
    val name: String = "Parkplatz",
    val roofed: Boolean,
    val icon: Int = R.drawable.app_parkplatz,
    val capacities: Map<String, Capacity> = emptyMap(),
    val parkingCapacityTotal: Int,
    val parkingCapacityHandicapped: Int,
    val parkingCapacityFamily: Int,
    val parkingCapacityWoman: Int,
    val hasPrognosis: Boolean,
    val isOutOfService: Boolean,
    val access: String?,
    val mainAccess: String?,
    val nightAccess: String?,
    val openingHours: String?,
    val is24h: Boolean,
    val freeParking: String?,
    val maxParkingTime: String?,
    val tariffNotes: String?,
    val discount: String?,
    val specialTariff: String?,
    val paymentOptions: String?,
    val prices: List<Price>,
    val distanceToStation: String?,
    val operator: String?,
    val featureTags: Set<FeatureTag>

)