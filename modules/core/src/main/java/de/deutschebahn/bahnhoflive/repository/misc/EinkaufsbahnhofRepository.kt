/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository.misc

import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.einkaufsbahnhof.model.StationList
import de.deutschebahn.bahnhoflive.backend.einkaufsbahnhof.model.StationResponse
import de.deutschebahn.bahnhoflive.repository.fail

open class EinkaufsbahnhofRepository {
    open fun queryStations(cache: Boolean, listener: VolleyRestListener<StationList?>) {
        listener.fail()
    }

    open fun queryStation(
        id: String,
        cache: Boolean,
        listener: VolleyRestListener<StationResponse>
    ) {
        listener.fail()
    }

}
