/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository.map

import com.google.android.gms.maps.model.TileProvider
import de.deutschebahn.bahnhoflive.backend.BaseRestListener
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.local.model.RrtPoint
import de.deutschebahn.bahnhoflive.backend.rimap.model.RimapPOI
import de.deutschebahn.bahnhoflive.backend.rimap.model.RimapStation
import de.deutschebahn.bahnhoflive.backend.rimap.model.StationFeatureCollection
import de.deutschebahn.bahnhoflive.repository.Station
import de.deutschebahn.bahnhoflive.repository.fail
import de.deutschebahn.bahnhoflive.ui.map.content.tiles.GroundTileProvider
import de.deutschebahn.bahnhoflive.ui.map.content.tiles.IndoorTileProvider
import de.deutschebahn.bahnhoflive.util.Cancellable

open class MapRepository {

    /**
     * This is actually a fallback belonging to the [de.deutschebahn.bahnhoflive.repository.station.StationRepository].
     * Should be cleaned up one day...
     */
    open fun queryStationInfo(
        id: String,
        listener: VolleyRestListener<StationFeatureCollection>,
        useCache: Boolean,
        evaId: String? = null
    ): Cancellable? = listener.fail()

    open fun queryPois(
        station: Station,
        listener: VolleyRestListener<List<RimapPOI>>,
        cache: Boolean
    ) {
        listener.fail()
    }

    open fun createIndoorTileProvider(width: Int, height: Int, zoneId: Int?): IndoorTileProvider =
        IndoorTileProvider(width, height)

    open fun createGroundTileProvider(width: Int, height: Int): TileProvider =
        GroundTileProvider("", width, height)

    open fun queryLevels(
        id: String,
        listener: BaseRestListener<RimapStation?>,
        useCache: Boolean,
        evaId: String?
    ): Cancellable? = listener.fail()

    open fun queryRailReplacement(
        id: String,
        force: Boolean,
        listener: VolleyRestListener<RrtRequestResult>
    ): Cancellable? = listener.fail()

    open fun queryStationPlatformLevels(
        station: Station,
        cache: Boolean,
        listener: VolleyRestListener<List<RimapPOI>>
    ): Cancellable? = listener.fail()
}

typealias RrtRequestResult = List<RrtPoint>
