package de.deutschebahn.bahnhoflive.backend.local.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DailyOpeningHours(
    val dayOfWeek: Int,
    val timestamp: Long,
    val list: List<OpeningHour>
) : Parcelable