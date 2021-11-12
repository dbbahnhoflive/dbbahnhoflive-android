/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map.content;

import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.model.TileOverlay;
import com.huawei.hms.maps.model.TileOverlayOptions;

import org.jetbrains.annotations.Nullable;

import de.deutschebahn.bahnhoflive.BaseApplication;
import de.deutschebahn.bahnhoflive.ui.map.content.tiles.IndoorTileProvider;

public class IndoorLayer {
    private static final int TILE_SIZE = 256;

    @Nullable
    private final Integer zoneId;

    private TileOverlay tileOverlay;
    private IndoorTileProvider tileProvider;
    private TileOverlayOptions tileOverlayOptions;

    public IndoorLayer(@androidx.annotation.Nullable Integer zoneId) {
        this.zoneId = zoneId;
    }

    private IndoorTileProvider getTileProvider() {
        if (tileProvider == null) {
            tileProvider = BaseApplication.get().getRepositories().getMapRepository().createIndoorTileProvider(TILE_SIZE, TILE_SIZE, zoneId);
        }
        return tileProvider;
    }

    private TileOverlayOptions getOverlayOptions() {
        if (tileOverlayOptions == null) {
            tileOverlayOptions = new TileOverlayOptions()
                    .tileProvider(getTileProvider())
                    .zIndex(100);
        }
        return tileOverlayOptions;
    }

    public void attach(HuaweiMap map) {
        if (tileOverlay == null) {
            tileOverlay = map.addTileOverlay(getOverlayOptions());
        }
    }

    private void detach() {
        if (tileOverlay != null) {
            tileOverlay.clearTileCache();
            tileOverlay.remove();
            tileOverlay = null;
        }
    }

    public void reset() {
        detach();
        tileProvider = null;
        tileOverlayOptions = null;
    }

    public void setLevel(int level, HuaweiMap googleMap) {
        getTileProvider().setLevel(level);
        detach();
        attach(googleMap);
    }
}
