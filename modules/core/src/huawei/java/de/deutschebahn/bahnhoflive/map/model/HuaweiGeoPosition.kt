package de.deutschebahn.bahnhoflive.map.model

import com.huawei.hms.maps.model.LatLng

fun LatLng.toGeoPosition() = GeoPosition(latitude, longitude)

fun GeoPosition.toLatLng() = LatLng(latitude, longitude)