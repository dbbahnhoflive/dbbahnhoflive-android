package de.deutschebahn.bahnhoflive.map.model

import com.huawei.hms.maps.model.LatLng

fun LatLng.toGeoPosition() = GeoPosition(longitude, latitude)

fun GeoPosition.toLatLng() = LatLng(latitude, longitude)