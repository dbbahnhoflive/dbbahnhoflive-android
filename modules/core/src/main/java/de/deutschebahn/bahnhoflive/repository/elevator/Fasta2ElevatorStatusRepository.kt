/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository.elevator

import de.deutschebahn.bahnhoflive.backend.RestHelper
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.fasta2.FacilityEquipmentStatusRequest
import de.deutschebahn.bahnhoflive.backend.db.fasta2.FacilityStatusRequest
import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus

class Fasta2ElevatorStatusRepository(
    val restHelper: RestHelper,
    private val authorizationTool: DbAuthorizationTool
) : ElevatorStatusRepository() {

    override fun queryStationElevatorStatuses(
        stationId: String,
        listener: VolleyRestListener<List<FacilityStatus>>
    ) {
        restHelper.add(FacilityStatusRequest(stationId, authorizationTool, listener))
    }

    override fun queryElevatorStatus(
        equipmentNumber: String,
        listener: VolleyRestListener<FacilityStatus>
    ) {
        restHelper.add(FacilityEquipmentStatusRequest(equipmentNumber, authorizationTool, listener))
    }
}