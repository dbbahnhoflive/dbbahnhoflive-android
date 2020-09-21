/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository.localtransport

import android.location.Location
import de.deutschebahn.bahnhoflive.backend.BaseRestListener
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.hafas.Filter
import de.deutschebahn.bahnhoflive.backend.hafas.HafasDepartures
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasDetail
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasEvent
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation
import de.deutschebahn.bahnhoflive.repository.fail

open class LocalTransportRepository {
    open fun queryNearbyStations(
        latitude: Double,
        longitude: Double,
        localTransportFilter: Filter<HafasStation>,
        listener: VolleyRestListener<List<HafasStation>>,
        origin: String,
        radius: Int
    ) {
        listener.fail()
    }

    open fun queryTimetable(
        hafasStation: HafasStation,
        origin: String,
        listener: BaseRestListener<HafasDepartures>,
        hours: Int,
        filterStrictly: Boolean,
        force: Boolean
    ) {
        listener.fail()
    }

    open fun queryStations(
        query: String,
        location: Location?,
        filter: Filter<HafasStation>,
        listener: VolleyRestListener<List<HafasStation>>,
        origin: String
    ) {
        listener.fail()
    }

    open fun queryTimetableDetails(
        hafasEvent: HafasEvent,
        listener: VolleyRestListener<HafasDetail>,
        origin: String
    ) {
        listener.fail()
    }
}