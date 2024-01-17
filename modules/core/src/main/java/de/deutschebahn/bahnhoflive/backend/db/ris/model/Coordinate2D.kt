package de.deutschebahn.bahnhoflive.backend.db.ris.model

import com.google.android.gms.maps.model.LatLng

class Coordinate2D {

    private var longitude: Double = 0.0
    private var latitude: Double = 0.0

    fun toLatLng() = LatLng(latitude, longitude)

    fun getLatitude() : Double = latitude

    @SuppressWarnings("unused")
    fun setLatitude(value:Double?) = if(value!=null)  latitude=value else latitude=0.0

    fun getLongitude() : Double = longitude
    @SuppressWarnings("unused")
    fun setLongitude(value:Double?) = if(value!=null)  longitude=value else longitude=0.0

}
