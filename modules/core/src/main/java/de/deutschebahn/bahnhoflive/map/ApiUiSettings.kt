package de.deutschebahn.bahnhoflive.map

interface ApiUiSettings {
    fun setScrollGesturesEnabled(value: Boolean)
    fun setZoomGesturesEnabled(value: Boolean)
    fun setTiltGesturesEnabled(value: Boolean)
    fun setRotateGesturesEnabled(value: Boolean)
    fun setMyLocationButtonEnabled(value: Boolean)
    fun setMapToolbarEnabled(value: Boolean)
    fun setCompassEnabled(value: Boolean)
    fun setZoomControlsEnabled(value: Boolean)
    fun setIndoorLevelPickerEnabled(value: Boolean)
}