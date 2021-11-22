package de.deutschebahn.bahnhoflive.ui.map.content.tiles

import de.deutschebahn.bahnhoflive.map.model.AppTile
import de.deutschebahn.bahnhoflive.map.model.AppTileProvider
import java.net.URL
import java.net.URLConnection

abstract class HttpAppTileProvider(
    val width: Int,
    val height: Int
) : AppTileProvider {

    protected open fun getTileUrl(x: Int, y: Int, zoom: Int): URL? = null

    protected open fun openConnection(url: URL): URLConnection = url.openConnection()

    override fun getTile(x: Int, y: Int, zoom: Int): AppTile =
        try {
            getTileUrl(x, y, zoom)?.let { url ->
                AppTile(
                    width,
                    height,
                    openConnection(url).getInputStream().buffered().use { stream ->
                        stream.readBytes()
                    })
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } ?: AppTileProvider.NO_TILE
}