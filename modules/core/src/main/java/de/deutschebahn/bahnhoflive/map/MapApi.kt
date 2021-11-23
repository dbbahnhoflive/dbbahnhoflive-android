package de.deutschebahn.bahnhoflive.map

import android.content.Context
import androidx.annotation.RawRes
import de.deutschebahn.bahnhoflive.map.model.*
import de.deutschebahn.bahnhoflive.ui.map.ZoomChangeMonitor
import de.deutschebahn.bahnhoflive.ui.map.content.MapType

interface MapApi {
    fun addTileOverlay(apiTileOverlayOptions: ApiTileOverlayOptions): ApiTileOverlay?

    fun clear()

    fun addMarker(apiMarkerOptions: ApiMarkerOptions): ApiMarker?

    fun setMaxZoomPreference(maxZoomPreference: Float)
    fun setMapStyle(context: Context, @RawRes mapStyleResource: Int)
    fun setMapType(mapType: MapType)
    fun setOnMapClickListener(onMapClickListener: OnMapClickListener)
    fun setOnCameraIdleListener(onCameraIdleListener: ZoomChangeMonitor)
    fun setOnMarkerClickListener(onMarkerClickListener: OnMarkerClickListener)
    fun setIndoorEnabled(indoorEnabled: Boolean)
    fun setBuildingsEnabled(buildingsEnabled: Boolean)
    val apiUiSettings: ApiUiSettings
    val cameraZoom: Float


    interface OnCameraIdleListener {
        fun onCameraIdle()
    }

    interface OnMapClickListener {
        fun onMapClick(geoPosition: GeoPosition)
    }

    interface OnMarkerClickListener {
        fun onMarkerClick(apiMarker: ApiMarker): Boolean
    }

    interface CancelableCallback {
        fun onFinish()
        fun onCancel()
    }

    fun animateCamera(geoPosition: GeoPosition, zoom: Float): CameraUpdateJob
    fun animateCamera(bounds: GeoPositionBounds, padding: Int): CameraUpdateJob

}
