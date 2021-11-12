package de.deutschebahn.bahnhoflive.backend.db.ris.model

import com.huawei.hms.maps.model.LatLng

class Coordinate2D {

    fun toLatLng() = longitude?.let { longitude ->
        latitude?.let { latitude ->
            LatLng(latitude, longitude)
        }
    }

    var longitude: Double = 0.0
    var latitude: Double = 0.0
}
