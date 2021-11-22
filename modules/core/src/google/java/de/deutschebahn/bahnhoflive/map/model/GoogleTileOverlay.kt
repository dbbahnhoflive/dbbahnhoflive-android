package de.deutschebahn.bahnhoflive.map.model

import com.google.android.gms.maps.model.TileOverlay

class GoogleTileOverlay(private val tileOverlay: TileOverlay) :
    ApiTileOverlay {
    override fun clearTileCache() {
        tileOverlay.clearTileCache()
    }

    override fun remove() {
        tileOverlay.remove()
    }
}