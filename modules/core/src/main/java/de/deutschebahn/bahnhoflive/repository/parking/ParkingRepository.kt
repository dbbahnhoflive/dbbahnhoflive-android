/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository.parking

import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.model.parking.LiveCapacity
import de.deutschebahn.bahnhoflive.model.parking.ParkingFacility
import de.deutschebahn.bahnhoflive.repository.fail

open class ParkingRepository {

    open fun queryFacilities(
        stationId: String,
        listener: VolleyRestListener<List<ParkingFacility>>
    ) {
        listener.fail()
    }

    open fun queryCapacity(
        parkingFacility: ParkingFacility,
        listener: VolleyRestListener<LiveCapacity>
    ) {
        listener.fail()
    }
}
