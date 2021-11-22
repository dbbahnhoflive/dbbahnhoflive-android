/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.map.content.tiles

open class IndoorAppTileProvider(
    protected val tileWidth: Int,
    protected val tileHeight: Int
) :
    HttpAppTileProvider(
        tileWidth, tileHeight
    ) {

    open var level: Int = 0

    companion object {
        const val MAP_SIZE = 20037508.34789244 * 2
        const val TILE_ORIGIN_X = -20037508.34789244
        const val TILE_ORIGIN_Y = 20037508.34789244
    }
}