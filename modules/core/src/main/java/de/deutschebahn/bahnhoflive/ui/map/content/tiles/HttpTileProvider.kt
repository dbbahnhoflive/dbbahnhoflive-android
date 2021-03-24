package de.deutschebahn.bahnhoflive.ui.map.content.tiles

import com.google.android.gms.maps.model.Tile
import com.google.android.gms.maps.model.TileProvider
import java.net.URL

abstract class HttpTileProvider(
    val width: Int,
    val height: Int
) : TileProvider {

    open fun getTileUrl(x: Int, y: Int, zoom: Int): URL? = null

    open fun openConnection(url: URL) = url.openConnection()

    override fun getTile(x: Int, y: Int, zoom: Int): Tile =
        try {
            getTileUrl(x, y, zoom)?.let { url ->
                Tile(width, height, openConnection(url).getInputStream().buffered().use { stream ->
                    stream.readBytes()
                })
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } ?: TileProvider.NO_TILE
}