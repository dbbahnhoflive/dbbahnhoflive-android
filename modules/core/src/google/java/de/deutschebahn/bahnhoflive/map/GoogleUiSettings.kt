package de.deutschebahn.bahnhoflive.map

import com.google.android.gms.maps.UiSettings

class GoogleUiSettings(val uiSettings: UiSettings) : ApiUiSettings {
    override fun setScrollGesturesEnabled(value: Boolean) {
        uiSettings.isScrollGesturesEnabled = value
    }

    override fun setZoomGesturesEnabled(value: Boolean) {
        uiSettings.isZoomGesturesEnabled = value
    }

    override fun setTiltGesturesEnabled(value: Boolean) {
        uiSettings.isTiltGesturesEnabled = value
    }

    override fun setRotateGesturesEnabled(value: Boolean) {
        uiSettings.isRotateGesturesEnabled = value
    }

    override fun setMyLocationButtonEnabled(value: Boolean) {
        uiSettings.isMyLocationButtonEnabled = value
    }

    override fun setMapToolbarEnabled(value: Boolean) {
        uiSettings.isMapToolbarEnabled = value
    }

    override fun setCompassEnabled(value: Boolean) {
        uiSettings.isCompassEnabled = value
    }

    override fun setZoomControlsEnabled(value: Boolean) {
        uiSettings.isZoomControlsEnabled = value
    }

    override fun setIndoorLevelPickerEnabled(value: Boolean) {
        uiSettings.isIndoorLevelPickerEnabled = value
    }
}