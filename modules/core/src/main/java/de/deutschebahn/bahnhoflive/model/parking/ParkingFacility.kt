/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.model.parking

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ParkingFacility(
    val id: String,
    val name: String,
    val roofed: Boolean,
    val capacities: Map<String, Capacity>,
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
    val featureTags: Set<FeatureTag>,
    val location: LatLng?,
    val liveCapacity: LiveCapacity? = null
) : Parcelable