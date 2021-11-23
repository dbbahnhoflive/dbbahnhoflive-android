package de.deutschebahn.bahnhoflive.map.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class GeoPosition(
    @JvmField val latitude: Double,
    @JvmField val longitude: Double
) : Parcelable