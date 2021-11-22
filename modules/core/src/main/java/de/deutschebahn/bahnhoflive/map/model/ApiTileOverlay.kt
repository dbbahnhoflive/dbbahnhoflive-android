package de.deutschebahn.bahnhoflive.map.model

interface ApiTileOverlay {
    fun clearTileCache()

    fun remove()
}