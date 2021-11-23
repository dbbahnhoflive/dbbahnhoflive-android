package de.deutschebahn.bahnhoflive.map.model

interface AppTileProvider {
    fun getTile(x: Int, y: Int, zoom: Int): AppTile?
}
