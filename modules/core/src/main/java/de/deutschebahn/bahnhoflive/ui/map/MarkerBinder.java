package de.deutschebahn.bahnhoflive.ui.map;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import de.deutschebahn.bahnhoflive.ui.map.content.rimap.Filter;

public class MarkerBinder {

    @NonNull
    private final MarkerContent markerContent;

    private Marker marker;

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

    public static MarkerBinder of(Marker marker) {
        return (MarkerBinder) marker.getTag();
    }

    @NonNull
    public MarkerContent getMarkerContent() {
        return markerContent;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
        updateMarker();
        markerContent.onHighlighted(highlighted);
    }

    private void updateMarker() {
        if (marker != null) {
            marker.setIcon(markerContent.getBitmapDescriptorFactory().createBitmapDescriptor(highlighted));
            updateVisibility();
        }
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;

        if (marker != null && markerContent.acceptsZoom(zoom) != marker.isVisible()) {
            updateVisibility();
        }
    }

    public void updateVisibility() {
        if (marker != null) {
            marker.setVisible(isVisible());
        }
    }

    public void setLevel(int level) {
        this.level = level;

        if (markerContent.acceptsLevel(level) != marker.isVisible()) {
            updateVisibility();
        }
    }

    public LatLngBounds getBounds() {
        return markerContent.getBounds();
    }

    public boolean isVisible() {
        return markerContent.acceptsZoom(zoom) && isFilterChecked() && markerContent.acceptsLevel(level);
    }

    public boolean isFilterChecked() {
        return filterItem.getChecked();
    }

    public void bind(GoogleMap googleMap) {
        final MarkerOptions markerOptions = markerContent.createMarkerOptions();
        if (markerOptions != null) {
            marker = googleMap.addMarker(markerOptions);
            marker.setTag(this);
            updateVisibility();
        }
    }

    public void unbind() {
        if (marker != null) {
            marker.remove();
            marker = null;
        }
    }

}
