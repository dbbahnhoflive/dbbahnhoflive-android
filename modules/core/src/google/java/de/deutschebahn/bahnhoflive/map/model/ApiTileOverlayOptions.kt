package de.deutschebahn.bahnhoflive.map.model

import com.google.android.gms.maps.model.Tile
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.android.gms.maps.model.TileProvider

class ApiTileOverlayOptions {
    companion object {
        val NO_TILE: AppTile
            get() = TileProvider.NO_TILE.run {
                AppTile(width, height, data)
            }
    }

    val tileOverlayOptions = TileOverlayOptions()

    fun tileProvider(appTileProvider: AppTileProvider): ApiTileOverlayOptions = this.also {
        tileOverlayOptions.tileProvider { x, y, zoom ->
            appTileProvider.getTile(x, y, zoom).toTile()
        }
    }

    fun zIndex(zIndex: Float): ApiTileOverlayOptions = this.apply {
        tileOverlayOptions.zIndex(zIndex)
    }

    private fun AppTile.toTile() = Tile(width, height, data)
}