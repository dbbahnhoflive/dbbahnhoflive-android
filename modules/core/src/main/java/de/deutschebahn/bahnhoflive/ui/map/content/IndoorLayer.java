package de.deutschebahn.bahnhoflive.ui.map.content;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;

import de.deutschebahn.bahnhoflive.BaseApplication;
import de.deutschebahn.bahnhoflive.backend.rimap.model.RimapStationInfo;
import de.deutschebahn.bahnhoflive.ui.map.content.tiles.IndoorTileProvider;

public class IndoorLayer {
    private static final int TILE_SIZE = 512;

    private TileOverlay tileOverlay;
    private IndoorTileProvider tileProvider;
    private TileOverlayOptions tileOverlayOptions;

    private IndoorTileProvider getTileProvider() {
        if (tileProvider == null) {
            tileProvider = BaseApplication.get().getRepositories().getMapRepository().createIndoorTileProvider(TILE_SIZE, TILE_SIZE);
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

    public void attach(GoogleMap map) {
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

    public void setLevel(int level, GoogleMap googleMap) {
        getTileProvider().setIndoorLevel(RimapStationInfo.levelToCode(level));
        detach();
        attach(googleMap);
    }
}
