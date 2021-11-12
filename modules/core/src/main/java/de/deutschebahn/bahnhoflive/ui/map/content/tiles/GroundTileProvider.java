/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map.content.tiles;

import com.huawei.hms.maps.model.UrlTileProvider;

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
