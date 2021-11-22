/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map.content;

import de.deutschebahn.bahnhoflive.BaseApplication;
import de.deutschebahn.bahnhoflive.map.MapApi;
import de.deutschebahn.bahnhoflive.map.model.ApiTileOverlay;
import de.deutschebahn.bahnhoflive.map.model.ApiTileOverlayOptions;
import de.deutschebahn.bahnhoflive.map.model.AppTileProvider;

public class BackgroundLayer {
    private static final int TILE_SIZE = 512;

    private ApiTileOverlay mApiTileOverlay;
    private AppTileProvider mTileProvider;
    private ApiTileOverlayOptions mApiTileOverlayOptions;


    private AppTileProvider getTileProvider() {
        if (mTileProvider == null) {
            mTileProvider = BaseApplication.get().getRepositories().getMapRepository().createGroundTileProvider(TILE_SIZE, TILE_SIZE);
        }
        return mTileProvider;
    }

    private ApiTileOverlayOptions getOverlayOptions() {
        if (mApiTileOverlayOptions == null) {
            mApiTileOverlayOptions = new ApiTileOverlayOptions()
                    .tileProvider(getTileProvider())
                    .zIndex(1);
        }
        return mApiTileOverlayOptions;
    }

    public void attach(MapApi map) {
        if (mApiTileOverlay == null) {
            mApiTileOverlay = map.addTileOverlay(getOverlayOptions());
        }
    }

    public void detach() {
        if (mApiTileOverlay != null) {
            mApiTileOverlay.clearTileCache();
            mApiTileOverlay.remove();
            mApiTileOverlay = null;
        }
    }

    public void reset() {
        this.detach();
        mTileProvider = null;
        mApiTileOverlayOptions = null;
    }
}
