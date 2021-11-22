package de.deutschebahn.bahnhoflive.map.model

interface ApiMarker {
    fun remove()

    var tag: Any?
    var isVisible: Boolean

    val position: GeoPosition

    fun setIcon(apiBitmapDescriptor: ApiBitmapDescriptor)
}