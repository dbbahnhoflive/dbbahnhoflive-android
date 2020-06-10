package de.deutschebahn.bahnhoflive.model.parking

import de.deutschebahn.bahnhoflive.R

data class ParkingFacility(
    val id: String,
    val name: String = "Parkplatz",
    val icon: Int = R.drawable.app_parkplatz,
    val capacities: Map<String, Capacity> = emptyMap(),
    val parkingCapacityTotal: Int,
    val hasPrognosis: Boolean
)