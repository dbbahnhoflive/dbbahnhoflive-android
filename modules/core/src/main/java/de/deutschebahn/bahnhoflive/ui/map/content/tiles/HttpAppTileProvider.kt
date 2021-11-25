package de.deutschebahn.bahnhoflive.ui.map.content.tiles

import android.util.Log
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

    override fun getTile(x: Int, y: Int, zoom: Int): AppTile? =
            getTileUrl(x, y, zoom)?.let { url ->
                try {
                    AppTile(
                        width,
                        height,
                        openConnection(url).getInputStream().buffered().use { stream ->
                            stream.readBytes()
                        }).also {
                        Log.d(HttpAppTileProvider::class.java.simpleName, "Created tile $url")
                    }
                } catch (e: Exception) {
                    Log.i(
                        HttpAppTileProvider::class.java.simpleName, "Error loading tile $url", e
                    )
                    null
                }
            }
}