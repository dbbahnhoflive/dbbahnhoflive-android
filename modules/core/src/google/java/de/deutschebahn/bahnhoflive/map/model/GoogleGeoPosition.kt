package de.deutschebahn.bahnhoflive.map.model

import com.google.android.gms.maps.model.LatLng

fun LatLng.toGeoPosition() = GeoPosition(longitude, latitude)

fun GeoPosition.toLatLng() = LatLng(latitude, longitude)