/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map.content;

import org.jetbrains.annotations.Nullable;

import de.deutschebahn.bahnhoflive.BaseApplication;
import de.deutschebahn.bahnhoflive.map.MapApi;
import de.deutschebahn.bahnhoflive.map.model.ApiTileOverlay;
import de.deutschebahn.bahnhoflive.map.model.ApiTileOverlayOptions;
import de.deutschebahn.bahnhoflive.ui.map.content.tiles.IndoorAppTileProvider;

public class IndoorLayer {
    private static final int TILE_SIZE = 256;

    @Nullable
    private final Integer zoneId;

    private ApiTileOverlay apiTileOverlay;
    private IndoorAppTileProvider tileProvider;
    private ApiTileOverlayOptions apiTileOverlayOptions;

    public IndoorLayer(@androidx.annotation.Nullable Integer zoneId) {
        this.zoneId = zoneId;
    }

    private IndoorAppTileProvider getTileProvider() {
        if (tileProvider == null) {
            tileProvider = BaseApplication.get().getRepositories().getMapRepository().createIndoorTileProvider(TILE_SIZE, TILE_SIZE, zoneId);
        }
        return tileProvider;
    }

    private ApiTileOverlayOptions getOverlayOptions() {
        if (apiTileOverlayOptions == null) {
            apiTileOverlayOptions = new ApiTileOverlayOptions()
                    .tileProvider(getTileProvider())
                    .zIndex(100);
        }
        return apiTileOverlayOptions;
    }

    public void attach(MapApi map) {
        if (apiTileOverlay == null) {
            apiTileOverlay = map.addTileOverlay(getOverlayOptions());
        }
    }

    private void detach() {
        if (apiTileOverlay != null) {
            apiTileOverlay.clearTileCache();
            apiTileOverlay.remove();
            apiTileOverlay = null;
        }
    }

    public void reset() {
        detach();
        tileProvider = null;
        apiTileOverlayOptions = null;
    }

    public void setLevel(int level, MapApi googleMap) {
        getTileProvider().setLevel(level);
        detach();
        attach(googleMap);
    }
}
