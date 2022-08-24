/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository.station

import android.location.Location
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.ris.model.LocalServices
import de.deutschebahn.bahnhoflive.backend.db.ris.model.Platform
import de.deutschebahn.bahnhoflive.backend.db.ris.model.RISStation
import de.deutschebahn.bahnhoflive.backend.db.ris.model.StopPlace
import de.deutschebahn.bahnhoflive.repository.fail
import de.deutschebahn.bahnhoflive.util.Cancellable
import de.deutschebahn.bahnhoflive.util.volley.VolleyRequestCancellable

open class StationRepository {
    open fun queryStations(
        listener: VolleyRestListener<List<StopPlace>?>,
        query: String? = null,
        location: Location? = null,
        force: Boolean = false,
        limit: Int = 25,
        radius: Int = 2000,
        mixedResults: Boolean,
        collapseNeighbours: Boolean,
        pullUpFirstDbStation: Boolean
    ): Cancellable? {
        listener.fail()
        return null
    }

    open fun queryAccessibilityDetails(
        listener: VolleyRestListener<List<Platform>>,
        stadaId: String,
        force: Boolean
    ): VolleyRequestCancellable<List<Platform>>? {
        listener.fail()
        return null
    }

    open fun queryLocalServices(
        listener: VolleyRestListener<LocalServices>,
        stadaId: String,
        force: Boolean,
        currentPosition: Location?
    ): Cancellable? {
        listener.fail()
        return null
    }

    open fun queryStation(
        listener: VolleyRestListener<RISStation>,
        stadaId: String,
        force: Boolean,
        currentPosition: Location?
    ): Cancellable? {
        listener.fail()
        return null
    }
}
