package de.deutschebahn.bahnhoflive.backend.local.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OpeningHour(
    val from: Long,
    val to: Long,
    val note: String?
) : Parcelable
