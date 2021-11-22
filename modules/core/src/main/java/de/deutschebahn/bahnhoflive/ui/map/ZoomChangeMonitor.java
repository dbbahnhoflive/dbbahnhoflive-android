/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map;

import de.deutschebahn.bahnhoflive.map.MapApi;

public class ZoomChangeMonitor implements MapApi.OnCameraIdleListener {

    public interface Listener {
        void onZoomChanged(float zoom);
    }

    private final MapApi mapApi;
    private final Listener listener;

    private float zoom;

    public ZoomChangeMonitor(MapApi mapApi, Listener listener) {
        this.mapApi = mapApi;

        zoom = mapApi.getCameraZoom();
        this.listener = listener;
    }

    @Override
    public void onCameraIdle() {
        final float cameraZoom = mapApi.getCameraZoom();
        if (cameraZoom != zoom) {
            zoom = cameraZoom;

            listener.onZoomChanged(zoom);
        }
    }
}
