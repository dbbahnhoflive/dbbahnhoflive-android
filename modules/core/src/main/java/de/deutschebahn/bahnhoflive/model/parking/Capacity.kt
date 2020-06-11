package de.deutschebahn.bahnhoflive.model.parking

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Capacity(
    val type: String,
    val total: Int
) : Parcelable