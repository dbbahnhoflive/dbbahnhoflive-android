package de.deutschebahn.bahnhoflive.map

import android.content.Context
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.model.LatLngBounds
import com.huawei.hms.maps.model.MapStyleOptions
import de.deutschebahn.bahnhoflive.map.model.*
import de.deutschebahn.bahnhoflive.ui.map.ZoomChangeMonitor
import de.deutschebahn.bahnhoflive.ui.map.content.MapType

class HuaweiMapApi(private val huaweiMap: HuaweiMap) : MapApi {

    override fun addTileOverlay(apiTileOverlayOptions: ApiTileOverlayOptions): ApiTileOverlay? =
        huaweiMap.addTileOverlay(apiTileOverlayOptions.tileOverlayOptions)?.let {
            HuaweiTileOverlay(it)
        }

    override fun clear() {
        huaweiMap.clear()
    }

    override fun animateCamera(geoPosition: GeoPosition, zoom: Float): CameraUpdateJob =
        HuaweiCameraUpdateJob(
            huaweiMap, CameraUpdateFactory.newLatLngZoom(geoPosition.toLatLng(), zoom)
        )

    override fun animateCamera(bounds: GeoPositionBounds, padding: Int): CameraUpdateJob =
        HuaweiCameraUpdateJob(
            huaweiMap, CameraUpdateFactory.newLatLngBounds(
                LatLngBounds(
                    bounds.bottomLeft.toLatLng(),
                    bounds.topRight.toLatLng()
                ), padding
            )
        )

    override fun addMarker(apiMarkerOptions: ApiMarkerOptions): ApiMarker? =
        huaweiMap.addMarker(apiMarkerOptions.markerOptions)?.let {
            HuaweiMarker(it)
        }

    override fun setMaxZoomPreference(maxZoomPreference: Float) {
        huaweiMap.setMaxZoomPreference(maxZoomPreference)
    }

    override fun setMapStyle(context: Context, mapStyleResource: Int) {
        huaweiMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, mapStyleResource))
    }

    override fun setMapType(mapType: MapType) {
        huaweiMap.mapType = HuaweiMap.MAP_TYPE_NONE
    }

    override fun setOnMapClickListener(onMapClickListener: MapApi.OnMapClickListener) {
        huaweiMap.setOnMapClickListener {
            onMapClickListener.onMapClick(it.toGeoPosition())
        }
    }

    override fun setOnCameraIdleListener(onCameraIdleListener: ZoomChangeMonitor) {
        huaweiMap.setOnCameraIdleListener {
            onCameraIdleListener.onCameraIdle()
        }
    }

    override fun setOnMarkerClickListener(onMarkerClickListener: MapApi.OnMarkerClickListener) {
        huaweiMap.setOnMarkerClickListener { marker ->
            onMarkerClickListener.onMarkerClick(HuaweiMarker(marker))
        }
    }

    override fun setIndoorEnabled(indoorEnabled: Boolean) {
        huaweiMap.isIndoorEnabled = indoorEnabled
    }

    override fun setBuildingsEnabled(buildingsEnabled: Boolean) {
        huaweiMap.isBuildingsEnabled = buildingsEnabled
    }

    override val apiUiSettings: ApiUiSettings
        get() = HuaweiUiSettings(huaweiMap.uiSettings)

    override val cameraZoom
        get() = huaweiMap.cameraPosition.zoom

}