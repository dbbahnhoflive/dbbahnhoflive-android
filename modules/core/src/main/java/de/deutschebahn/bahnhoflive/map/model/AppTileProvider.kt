package de.deutschebahn.bahnhoflive.map.model

interface AppTileProvider {
    companion object {
        val NO_TILE get() = ApiTileOverlayOptions.NO_TILE
    }

    fun getTile(x: Int, y: Int, zoom: Int): AppTile
}
