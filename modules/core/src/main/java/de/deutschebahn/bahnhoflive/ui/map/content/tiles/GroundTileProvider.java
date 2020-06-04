package de.deutschebahn.bahnhoflive.ui.map.content.tiles;

import com.google.android.gms.maps.model.UrlTileProvider;

import java.net.MalformedURLException;
import java.net.URL;

public class GroundTileProvider extends UrlTileProvider {

    private String mUrl;

    public GroundTileProvider(String url, int tileWidth, int tileHeight) {
        super(tileWidth, tileHeight);
        mUrl = url;
    }

    @Override
    public URL getTileUrl(int x, int y, int zoom) {
        try {
            return new URL(String.format(mUrl, zoom, x, y));
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }
}
