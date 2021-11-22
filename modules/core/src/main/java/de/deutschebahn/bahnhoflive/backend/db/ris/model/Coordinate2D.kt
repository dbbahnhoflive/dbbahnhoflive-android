package de.deutschebahn.bahnhoflive.backend.db.ris.model

import de.deutschebahn.bahnhoflive.map.model.GeoPosition

class Coordinate2D {

    fun toLatLng() = longitude?.let { longitude ->
        latitude?.let { latitude ->
            GeoPosition(latitude, longitude)
        }
    }

    var longitude: Double = 0.0
    var latitude: Double = 0.0
}
