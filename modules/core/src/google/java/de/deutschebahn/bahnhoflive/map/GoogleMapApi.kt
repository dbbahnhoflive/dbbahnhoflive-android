package de.deutschebahn.bahnhoflive.map

import android.content.Context
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import de.deutschebahn.bahnhoflive.map.model.*
import de.deutschebahn.bahnhoflive.ui.map.ZoomChangeMonitor
import de.deutschebahn.bahnhoflive.ui.map.content.MapType

class GoogleMapApi(private val googleMap: GoogleMap) : MapApi {

    override fun addTileOverlay(apiTileOverlayOptions: ApiTileOverlayOptions): ApiTileOverlay? =
        googleMap.addTileOverlay(apiTileOverlayOptions.tileOverlayOptions)?.let {
            GoogleTileOverlay(it)
        }

    override fun clear() {
        googleMap.clear()
    }

    override fun animateCamera(geoPosition: GeoPosition, zoom: Float): CameraUpdateJob =
        GoogleCameraUpdateJob(
            googleMap, CameraUpdateFactory.newLatLngZoom(geoPosition.toLatLng(), zoom)
        )

    override fun animateCamera(bounds: GeoPositionBounds, padding: Int): CameraUpdateJob =
        GoogleCameraUpdateJob(
            googleMap, CameraUpdateFactory.newLatLngBounds(
                LatLngBounds(
                    bounds.bottomLeft.toLatLng(),
                    bounds.topRight.toLatLng()
                ), padding
            )
        )

    override fun addMarker(apiMarkerOptions: ApiMarkerOptions): ApiMarker? =
        googleMap.addMarker(apiMarkerOptions.markerOptions)?.let {
            GoogleMarker(it)
        }

    override fun setMaxZoomPreference(maxZoomPreference: Float) {
        googleMap.setMaxZoomPreference(maxZoomPreference)
    }

    override fun setMapStyle(context: Context, mapStyleResource: Int) {
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, mapStyleResource))
    }

    override fun setMapType(mapType: MapType) {
        googleMap.mapType = when (mapType) {
            MapType.OSM -> GoogleMap.MAP_TYPE_NONE
            MapType.GOOGLE_MAPS -> GoogleMap.MAP_TYPE_NORMAL
        }
    }

    override fun setOnMapClickListener(onMapClickListener: MapApi.OnMapClickListener) {
        googleMap.setOnMapClickListener {
            onMapClickListener.onMapClick(it.toGeoPosition())
        }
    }

    override fun setOnCameraIdleListener(onCameraIdleListener: ZoomChangeMonitor) {
        googleMap.setOnCameraIdleListener {
            onCameraIdleListener.onCameraIdle()
        }
    }

    override fun setOnMarkerClickListener(onMarkerClickListener: MapApi.OnMarkerClickListener) {
        googleMap.setOnMarkerClickListener { marker ->
            onMarkerClickListener.onMarkerClick(GoogleMarker(marker))
        }
    }

    override fun setIndoorEnabled(indoorEnabled: Boolean) {
        googleMap.isIndoorEnabled = indoorEnabled
    }

    override fun setBuildingsEnabled(buildingsEnabled: Boolean) {
        googleMap.isBuildingsEnabled = buildingsEnabled
    }

    override val apiUiSettings: ApiUiSettings
        get() = GoogleUiSettings(googleMap.uiSettings)

    override val cameraZoom
        get() = googleMap.cameraPosition.zoom

}