package de.deutschebahn.bahnhoflive.backend.local.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DailyOpeningHours(
    val dayOfWeek: Int,
    val timestamp: Long,
    val list: List<OpeningHour>
) : Parcelable
