package de.deutschebahn.bahnhoflive.backend.db.parkinginformation.model

import androidx.annotation.NonNull

class ParkingFacility {

    @NonNull
    var id: String? = null

    @NonNull
    var name: ParkingFacilityName? = null

    val resolvedName get() = name?.name ?: "Unbekannte Parkmöglichkeit"

    var url: String? = null

    var hasPrognosis: Boolean? = null

}