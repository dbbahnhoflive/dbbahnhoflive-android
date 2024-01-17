package de.deutschebahn.bahnhoflive.backend.local.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OpeningHour(
    val dayOfWeek: Int,
    val fromMinuteOfDay: Int,
    val toMinuteOfDay: Int,
    val note: String? = null
) : Parcelable
