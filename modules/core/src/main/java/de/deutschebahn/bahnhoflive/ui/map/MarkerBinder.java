/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map;

import androidx.annotation.NonNull;

import de.deutschebahn.bahnhoflive.map.MapApi;
import de.deutschebahn.bahnhoflive.map.model.ApiMarker;
import de.deutschebahn.bahnhoflive.map.model.ApiMarkerOptions;
import de.deutschebahn.bahnhoflive.map.model.GeoPositionBounds;
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.Filter;

public class MarkerBinder {

    @NonNull
    private final MarkerContent markerContent;

    private ApiMarker apiMarker;

    private boolean highlighted;
    private float zoom;
    private int level;
    private final Filter filterItem;

    public MarkerBinder(@NonNull MarkerContent markerContent, float zoom, int level, Filter filterItem) {
        this.markerContent = markerContent;
        this.zoom = zoom;
        this.level = level;
        this.filterItem = filterItem;

        updateVisibility();
    }

    public static MarkerBinder of(ApiMarker apiMarker) {
        return (MarkerBinder) apiMarker.getTag();
    }

    @NonNull
    public MarkerContent getMarkerContent() {
        return markerContent;
    }

    public ApiMarker getMarker() {
        return apiMarker;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
        updateMarker();
        markerContent.onHighlighted(highlighted);
    }

    private void updateMarker() {
        if (apiMarker != null) {
            apiMarker.setIcon(markerContent.getBitmapDescriptorFactory().createBitmapDescriptor(highlighted));
            updateVisibility();
        }
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;

        if (apiMarker != null && markerContent.acceptsZoom(zoom) != apiMarker.isVisible()) {
            updateVisibility();
        }
    }

    public void updateVisibility() {
        if (apiMarker != null) {
            apiMarker.setVisible(isVisible());
        }
    }

    public void setLevel(int level) {
        this.level = level;

        final ApiMarker apiMarker = this.apiMarker;
        if (apiMarker != null && markerContent.acceptsLevel(level) != apiMarker.isVisible()) {
            updateVisibility();
        }
    }

    public GeoPositionBounds getBounds() {
        return markerContent.getBounds();
    }

    public boolean isVisible() {
        return markerContent.acceptsZoom(zoom) && isFilterChecked() && markerContent.acceptsLevel(level);
    }

    public boolean isFilterChecked() {
        return filterItem.getChecked();
    }

    public void bind(MapApi googleMap) {
        final ApiMarkerOptions apiMarkerOptions = markerContent.createMarkerOptions();
        if (apiMarkerOptions != null) {
            final ApiMarker marker = googleMap.addMarker(apiMarkerOptions);
            if (marker != null) {
                apiMarker = marker;
                apiMarker.setTag(this);
                updateVisibility();
            }
        }
    }

    public void unbind() {
        if (apiMarker != null) {
            apiMarker.remove();
            apiMarker = null;
        }
    }

}
