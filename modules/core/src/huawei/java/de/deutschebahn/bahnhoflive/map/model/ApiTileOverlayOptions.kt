package de.deutschebahn.bahnhoflive.map.model

import com.huawei.hms.maps.model.Tile
import com.huawei.hms.maps.model.TileOverlayOptions
import com.huawei.hms.maps.model.TileProvider

class ApiTileOverlayOptions {

    val tileOverlayOptions = TileOverlayOptions()

    fun tileProvider(appTileProvider: AppTileProvider): ApiTileOverlayOptions = this.also {
        tileOverlayOptions.tileProvider { x, y, zoom ->
            appTileProvider.getTile(x, y, zoom)?.toTile() ?: TileProvider.NO_TILE
        }
    }

    fun zIndex(zIndex: Float): ApiTileOverlayOptions = this.apply {
        tileOverlayOptions.zIndex(zIndex)
    }

    private fun AppTile.toTile() = Tile(width, height, data)
}