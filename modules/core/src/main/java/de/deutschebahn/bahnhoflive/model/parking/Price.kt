package de.deutschebahn.bahnhoflive.model.parking

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Price(
    val price: Float,
    val duration: Duration,
    val period: String?,
    val group: String?
) : Parcelable