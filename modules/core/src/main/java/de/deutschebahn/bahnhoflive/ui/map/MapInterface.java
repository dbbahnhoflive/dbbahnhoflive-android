/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map;

import androidx.annotation.Nullable;

import com.huawei.hms.maps.model.LatLng;

import de.deutschebahn.bahnhoflive.ui.map.content.MapType;

class MapInterface {


    public interface MapTypeListener {
        void onMapTypeChanged(MapType mapType);
    }

    public static MapInterface createPlaceholder(MapTypeListener mapTypeListener) {
        return new MapInterface(mapTypeListener);
    }

    protected Integer zoneId;
    protected int levelCount;
    public MapType currentMapType = MapType.OSM;

    private boolean laidOut = false;

    protected PendingLocation pendingLocation;

    private final MapTypeListener mapTypeListener;

    private MapInterface(MapTypeListener mapTypeListener) {
        this.mapTypeListener = mapTypeListener;
    }

    protected MapInterface(MapInterface source) {
        this.zoneId = source.zoneId;
        this.levelCount = source.levelCount;
        this.currentMapType = source.currentMapType;
        this.laidOut = source.laidOut;
        this.pendingLocation = source.pendingLocation;
        mapTypeListener = source.mapTypeListener;
    }

    public void setZoneId(int zoneId) {
        this.zoneId = zoneId;
    }

    public void setIndoorLevel(int level) {
    }

    public void updateLevelPicker() {
    }

    protected void updateMapType() {
        mapTypeListener.onMapTypeChanged(currentMapType);
    }

    public void setLocation(LatLng loc, float zoom) {
        pendingLocation = new PendingLocation(loc, zoom);
    }

    public void updateViews() {

    }

    protected void updateMapStyle() {
    }

    public void scrollToMarker(@Nullable MarkerBinder markerBinder) {
    }

    public int getLevelCount() {
        return levelCount;
    }

    /**
     * @deprecated We don't use Google map content any more.
     */
    @Deprecated
    public void setMapTypeGoogle() {
        currentMapType = MapType.GOOGLE_MAPS;
        updateMapType();
    }

    public void setMapTypeOsm() {
        currentMapType = MapType.OSM;
        updateMapType();
    }

    public void setLevelCount(int levelCount) {
        this.levelCount = levelCount;
        updateMapStyle();
    }

    synchronized
    public void setLaidOut(boolean laidOut) {
        this.laidOut = laidOut;
        updateViews();
    }


    public void onDestroyView() {
        laidOut = false;
    }

    synchronized
    public boolean isLaidOut() {
        return laidOut;
    }

    protected static class PendingLocation {
        protected final LatLng latLng;
        protected final float zoom;

        public PendingLocation(LatLng latLng, float zoom) {
            this.latLng = latLng;
            this.zoom = zoom;
        }
    }
}
