package de.deutschebahn.bahnhoflive.map.model

import com.google.android.gms.maps.model.MarkerOptions

class ApiMarkerOptions {

    val markerOptions = MarkerOptions()

    fun position(geoPosition: GeoPosition): ApiMarkerOptions = this.also {
        markerOptions.position(geoPosition.toLatLng())
    }

    fun visible(visible: Boolean): ApiMarkerOptions = this.also {
        markerOptions.visible(visible)
    }

    fun zIndex(zIndex: Float): ApiMarkerOptions = this.also {
        markerOptions.zIndex(zIndex)
    }

    fun anchor(u: Float, v: Float): ApiMarkerOptions = this.also {
        markerOptions.anchor(u, v)
    }

    fun icon(iconDescriptorApi: ApiBitmapDescriptor) = this.also {
        markerOptions.icon(iconDescriptorApi.bitmapDescriptor)
    }
}