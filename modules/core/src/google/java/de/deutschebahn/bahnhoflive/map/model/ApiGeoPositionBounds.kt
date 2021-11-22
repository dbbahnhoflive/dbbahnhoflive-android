package de.deutschebahn.bahnhoflive.map.model

import com.google.android.gms.maps.model.LatLngBounds

val GeoPositionBounds.center: GeoPosition
    get() = LatLngBounds(
        bottomLeft.toLatLng(),
        topRight.toLatLng()
    ).center.toGeoPosition()