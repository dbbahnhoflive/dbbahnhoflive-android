package de.deutschebahn.bahnhoflive.map.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class GeoPosition(
    @JvmField val longitude: Double,
    @JvmField val latitude: Double
) : Parcelable