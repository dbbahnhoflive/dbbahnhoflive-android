/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map.content;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;

import de.deutschebahn.bahnhoflive.BaseApplication;

public class BackgroundLayer {
    private static final int TILE_SIZE = 512;

    private TileOverlay mTileOverlay;
    private TileProvider mTileProvider;
    private TileOverlayOptions mTileOverlayOptions;


    private TileProvider getTileProvider() {
        if (mTileProvider == null) {
            mTileProvider = BaseApplication.get().getRepositories().getMapRepository().createGroundTileProvider(TILE_SIZE, TILE_SIZE);
        }
        return mTileProvider;
    }

    private TileOverlayOptions getOverlayOptions() {
        if (mTileOverlayOptions == null) {
            mTileOverlayOptions = new TileOverlayOptions()
                    .tileProvider(getTileProvider())
                    .zIndex(1);
        }
        return mTileOverlayOptions;
    }

    public void attach(GoogleMap map) {
        if (mTileOverlay == null) {
            mTileOverlay = map.addTileOverlay(getOverlayOptions());
        }
    }

    public void detach() {
        if (mTileOverlay != null) {
            mTileOverlay.clearTileCache();
            mTileOverlay.remove();
            mTileOverlay = null;
        }
    }

    public void reset() {
        this.detach();
        mTileProvider = null;
        mTileOverlayOptions = null;
    }
}
