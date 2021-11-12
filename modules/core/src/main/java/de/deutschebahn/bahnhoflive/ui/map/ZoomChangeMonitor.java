/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map;

import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.model.CameraPosition;

class ZoomChangeMonitor implements HuaweiMap.OnCameraIdleListener {

    public interface Listener {
        void onZoomChanged(float zoom);
    }

    private final HuaweiMap googleMap;
    private final Listener listener;

    private float zoom;

    public ZoomChangeMonitor(HuaweiMap googleMap, Listener listener) {
        this.googleMap = googleMap;

        zoom = googleMap.getCameraPosition().zoom;
        this.listener = listener;
    }

    @Override
    public void onCameraIdle() {
        final CameraPosition cameraPosition = googleMap.getCameraPosition();
        if (cameraPosition.zoom != zoom) {
            zoom = cameraPosition.zoom;

            listener.onZoomChanged(zoom);
        }
    }
}
