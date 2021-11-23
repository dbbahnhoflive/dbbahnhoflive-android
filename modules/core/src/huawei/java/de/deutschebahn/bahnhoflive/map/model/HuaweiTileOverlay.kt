package de.deutschebahn.bahnhoflive.map.model

import com.huawei.hms.maps.model.TileOverlay

class HuaweiTileOverlay(private val tileOverlay: TileOverlay) :
    ApiTileOverlay {

    override fun clearTileCache() {
        tileOverlay.clearTileCache()
    }

    override fun remove() {
        tileOverlay.remove()
    }

}