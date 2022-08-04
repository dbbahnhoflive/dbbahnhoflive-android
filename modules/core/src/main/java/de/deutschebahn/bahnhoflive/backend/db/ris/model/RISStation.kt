package de.deutschebahn.bahnhoflive.backend.db.ris.model

import com.google.android.gms.maps.model.LatLng

class RISStation {
    var stationID: String? = null

    var stationCategory: String? = null

    var position: Coordinate2D? = null

    val category: Int = stationCategory?.let {
        kotlin.runCatching { StationCategory.valueOf(it).ordinal + 1 }.getOrNull()
    } ?: -1


    val location by lazy {
        position?.let {
            LatLng(it.latitude, it.longitude)
        }
    }
}