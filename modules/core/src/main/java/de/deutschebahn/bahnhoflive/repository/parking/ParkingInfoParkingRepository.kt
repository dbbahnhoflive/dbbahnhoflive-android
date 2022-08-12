/*
 * SPDX-FileCopyrightText: 2022 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository.parking

import de.deutschebahn.bahnhoflive.backend.RestHelper
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.parkinginformation.ParkingCapacityRequest
import de.deutschebahn.bahnhoflive.backend.db.parkinginformation.ParkingFacilitiesRequest
import de.deutschebahn.bahnhoflive.model.parking.LiveCapacity
import de.deutschebahn.bahnhoflive.model.parking.ParkingFacility

class ParkingInfoParkingRepository(
    private val restHelper: RestHelper,
    private val dbAuthorizationTool: DbAuthorizationTool
) : ParkingRepository() {
    override fun queryFacilities(
        stationId: String,
        listener: VolleyRestListener<List<ParkingFacility>>
    ) {
        restHelper.add(
            ParkingFacilitiesRequest(
                stationId,
                listener,
                dbAuthorizationTool
            )
        )
    }

    override fun queryCapacity(
        parkingFacility: ParkingFacility,
        listener: VolleyRestListener<LiveCapacity>
    ) {
        restHelper.add(
            ParkingCapacityRequest(
                parkingFacility,
                listener,
                dbAuthorizationTool
            )
        )
    }
}
