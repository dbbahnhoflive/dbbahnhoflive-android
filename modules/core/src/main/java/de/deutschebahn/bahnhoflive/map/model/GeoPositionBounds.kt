package de.deutschebahn.bahnhoflive.map.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class GeoPositionBounds(val bottomLeft: GeoPosition, val topRight: GeoPosition) : Parcelable
