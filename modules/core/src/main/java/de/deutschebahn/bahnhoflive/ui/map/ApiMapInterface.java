/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.map.ApiUiSettings;
import de.deutschebahn.bahnhoflive.map.CameraUpdateJob;
import de.deutschebahn.bahnhoflive.map.MapApi;
import de.deutschebahn.bahnhoflive.map.model.ApiMarker;
import de.deutschebahn.bahnhoflive.map.model.GeoPosition;
import de.deutschebahn.bahnhoflive.map.model.GeoPositionBounds;
import de.deutschebahn.bahnhoflive.ui.map.content.BackgroundLayer;
import de.deutschebahn.bahnhoflive.ui.map.content.IndoorLayer;
import de.deutschebahn.bahnhoflive.ui.map.content.MapType;

class ApiMapInterface extends MapInterface {

    public static final GeoPositionBounds BOUNDS_OF_GERMANY = new GeoPositionBounds(new GeoPosition(47.212363, 5.749376), new GeoPosition(55.072294, 14.890002));

    @NonNull
    private final MapApi mapApi;

    private final BackgroundLayer backgroundLayer = new BackgroundLayer();
    private final IndoorLayer indoorLayer = new IndoorLayer(zoneId);

    private Context context;

    ApiMapInterface(MapInterface mapInterface, @NonNull final MapApi mapApi, Context context, MapApi.OnMarkerClickListener onMarkerClickListener, MapApi.OnMapClickListener onMapClickListener, ZoomChangeMonitor.Listener zoomChangeListener, GeoPosition location, float zoom) {
        super(mapInterface);

        this.mapApi = mapApi;
        this.context = context;

        ZoomChangeMonitor zoomChangeMonitor = new ZoomChangeMonitor(mapApi, zoomChangeListener);

        mapApi.clear();

        final ApiUiSettings apiUiSettings = mapApi.getApiUiSettings();

        apiUiSettings.setIndoorLevelPickerEnabled(false);
        apiUiSettings.setZoomControlsEnabled(false);
        apiUiSettings.setCompassEnabled(false);
        apiUiSettings.setMapToolbarEnabled(false);
        apiUiSettings.setMyLocationButtonEnabled(false);
        apiUiSettings.setRotateGesturesEnabled(false);
        apiUiSettings.setTiltGesturesEnabled(false);

        apiUiSettings.setZoomGesturesEnabled(true);
        apiUiSettings.setScrollGesturesEnabled(true);

        mapApi.setBuildingsEnabled(false);
        mapApi.setIndoorEnabled(false);

        mapApi.setOnMarkerClickListener(onMarkerClickListener);

        mapApi.setOnCameraIdleListener(zoomChangeMonitor);
        mapApi.setOnMapClickListener(onMapClickListener);
        updateMapStyle();
        updateViews();

        setLocation(location, zoom);
    }


    @Override
    public void setIndoorLevel(int level) {
        indoorLayer.setLevel(level, mapApi);
    }

    public void updateMapType() {
        super.updateMapType();

        backgroundLayer.reset();
        indoorLayer.reset();

        mapApi.setMapType(MapType.OSM);
        backgroundLayer.attach(this.mapApi);

        indoorLayer.attach(mapApi);

        updateMaxZoom();
    }

    @Override
    public void updateMapStyle() {
        @RawRes int mapStyle = levelCount > 0 ? R.raw.mapstyle_indoor : R.raw.mapstyle;
        mapApi.setMapStyle(context, mapStyle);
        updateMaxZoom();
    }

    public void updateMaxZoom() {
        mapApi.setMaxZoomPreference(levelCount > 0 ? 20 : 17);
    }

    @Override
    public void scrollToMarker(MarkerBinder markerBinder) {
        if (markerBinder == null) {
            return;
        }

        final ApiMarker apiMarker = markerBinder.getMarker();
        if (apiMarker != null) {
            final GeoPositionBounds bounds = null; // markerBinder.getBounds();
            final CameraUpdateJob cameraUpdateJob = bounds == null ?
                    mapApi.animateCamera(apiMarker.getPosition(), markerBinder.getMarkerContent().getZoom(mapApi.getCameraZoom()))
                    : mapApi.animateCamera(bounds, 32);

            cameraUpdateJob.run(100, null);
        }
    }

    @Override
    public void setLocation(GeoPosition geoPosition, float zoom) {
        pendingLocation = new PendingLocation(geoPosition, zoom);
        takePendingLocation();
    }

    synchronized
    private void takePendingLocation() {
        if (pendingLocation == null || !isLaidOut()) {
            return;
        }

        final GeoPosition geoPosition = pendingLocation.geoPosition;
        final float zoom = pendingLocation.zoom;

        this.pendingLocation = null;

        if (geoPosition == null) {
            mapApi.animateCamera(BOUNDS_OF_GERMANY, 0).run(400, null);
        } else {
            mapApi.animateCamera(geoPosition, zoom).run(400, null);
        }
    }

    @Override
    public void updateViews() {
        updateLevelPicker();
        updateMapType();
    }

    @Override
    public void setLaidOut(boolean laidOut) {
        super.setLaidOut(laidOut);
        takePendingLocation();
    }
}
