package de.deutschebahn.bahnhoflive.map.model

import com.google.android.gms.maps.model.Marker

class GoogleMarker(val marker: Marker) : ApiMarker {
    override fun remove() {
        marker.remove()
    }

    override var tag by marker::tag

    override var isVisible
        get() = marker.isVisible
        set(value) = marker.setVisible(value)

    override val position: GeoPosition by lazy {
        marker.position.toGeoPosition()
    }

    override fun setIcon(apiBitmapDescriptor: ApiBitmapDescriptor) {
        marker.setIcon(apiBitmapDescriptor.bitmapDescriptor)
    }

}