package de.deutschebahn.bahnhoflive.model.parking

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LiveCapacity(
    val facilityId: String,
    val parkingStatus: ParkingStatus
) : Parcelable