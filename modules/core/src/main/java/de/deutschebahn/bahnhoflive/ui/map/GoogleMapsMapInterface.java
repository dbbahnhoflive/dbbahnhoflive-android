/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.ui.map.content.BackgroundLayer;
import de.deutschebahn.bahnhoflive.ui.map.content.IndoorLayer;
import de.deutschebahn.bahnhoflive.ui.map.content.MapType;

class GoogleMapsMapInterface extends MapInterface {

    public static final LatLngBounds BOUNDS_OF_GERMANY = new LatLngBounds(new LatLng(47.212363, 5.749376), new LatLng(55.072294, 14.890002));

    @NonNull
    private final GoogleMap googleMap;

    private final BackgroundLayer backgroundLayer = new BackgroundLayer();
    private final IndoorLayer indoorLayer = new IndoorLayer();

    private Context context;

    GoogleMapsMapInterface(MapInterface mapInterface, @NonNull final GoogleMap googleMap, Context context, GoogleMap.OnMarkerClickListener onMarkerClickListener, GoogleMap.OnMapClickListener onMapClickListener, ZoomChangeMonitor.Listener zoomChangeListener, LatLng location, float zoom) {
        super(mapInterface);

        this.googleMap = googleMap;
        this.context = context;

        ZoomChangeMonitor zoomChangeMonitor = new ZoomChangeMonitor(googleMap, zoomChangeListener);

        googleMap.clear();

        final UiSettings uiSettings = googleMap.getUiSettings();

        uiSettings.setIndoorLevelPickerEnabled(false);
        uiSettings.setZoomControlsEnabled(false);
        uiSettings.setCompassEnabled(false);
        uiSettings.setMapToolbarEnabled(false);
        uiSettings.setMyLocationButtonEnabled(false);
        uiSettings.setRotateGesturesEnabled(false);
        uiSettings.setTiltGesturesEnabled(false);

        uiSettings.setZoomGesturesEnabled(true);
        uiSettings.setScrollGesturesEnabled(true);

        googleMap.setBuildingsEnabled(false);
        googleMap.setIndoorEnabled(false);

        googleMap.setOnMarkerClickListener(onMarkerClickListener);

        googleMap.setOnCameraIdleListener(zoomChangeMonitor);
        googleMap.setOnMapClickListener(onMapClickListener);
        updateMapStyle();
        updateViews();

        setLocation(location, zoom);
    }


    @Override
    public void setIndoorLevel(int level) {
        indoorLayer.setLevel(level, googleMap);
    }

    public void updateMapType() {
        super.updateMapType();

        backgroundLayer.reset();
        indoorLayer.reset();

        if (currentMapType == MapType.OSM) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);

            backgroundLayer.attach(this.googleMap);
        } else if (currentMapType == MapType.GOOGLE_MAPS) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            backgroundLayer.detach();
        }
        indoorLayer.attach(googleMap);

        updateMaxZoom();
    }

    @Override
    public void updateMapStyle() {
        int mapStyle = levelCount > 0 ? R.raw.mapstyle_indoor : R.raw.mapstyle;
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, mapStyle));
        updateMaxZoom();
    }

    public void updateMaxZoom() {
        googleMap.setMaxZoomPreference(levelCount > 0 || currentMapType == MapType.GOOGLE_MAPS ? 20 : 17);
    }

    @Override
    public void scrollToMarker(MarkerBinder markerBinder) {
        if (markerBinder == null) {
            return;
        }

        final Marker marker = markerBinder.getMarker();
        if (marker != null) {
            final LatLngBounds bounds = null; // markerBinder.getBounds();
            final CameraUpdate cameraUpdate = bounds == null ?
                    CameraUpdateFactory.newLatLngZoom(marker.getPosition(), markerBinder.getMarkerContent().getZoom(googleMap.getCameraPosition().zoom))
                    : CameraUpdateFactory.newLatLngBounds(bounds, 32);

            googleMap.animateCamera(cameraUpdate, 100, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {

                }

                @Override
                public void onCancel() {

                }
            });
        }
    }

    @Override
    public void setLocation(LatLng latLng, float zoom) {
        pendingLocation = new PendingLocation(latLng, zoom);
        takePendingLocation();
    }

    synchronized
    private void takePendingLocation() {
        if (pendingLocation == null || !isLaidOut()) {
            return;
        }

        final LatLng latLng = pendingLocation.latLng;
        final float zoom = pendingLocation.zoom;

        this.pendingLocation = null;

        if (latLng == null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(BOUNDS_OF_GERMANY, 0), 400, null);
        } else {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom), 400, null);
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
