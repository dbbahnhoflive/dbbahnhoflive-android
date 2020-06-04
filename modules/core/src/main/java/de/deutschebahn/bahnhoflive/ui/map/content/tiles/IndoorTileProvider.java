package de.deutschebahn.bahnhoflive.ui.map.content.tiles;

import com.google.android.gms.maps.model.UrlTileProvider;

import java.net.URL;

public class IndoorTileProvider extends UrlTileProvider {
    protected static final double MAP_SIZE = 20037508.34789244 * 2;
    protected static final double TILE_ORIGIN_X = -20037508.34789244;
    protected static final double TILE_ORIGIN_Y = 20037508.34789244;

    protected int tileWidth;
    protected int tileHeight;
    private String mIndoorLevel = "L0";

    public IndoorTileProvider(int tileWidth, int tileHeight) {
        super(tileWidth, tileHeight);
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
    }

    protected String getIndoorLevel() {
        if (this.mIndoorLevel == null) {
            return "L0";
        }
        return mIndoorLevel.toUpperCase();
    }

    public void setIndoorLevel(String v) {
        this.mIndoorLevel = v;
    }

    @Override
    public URL getTileUrl(int i, int i1, int i2) {
        return null;
    }
}
