/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository.elevator

import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus
import de.deutschebahn.bahnhoflive.repository.fail

open class ElevatorStatusRepository {
    open fun queryStationElevatorStatuses(
        stationId: String,
        listener: VolleyRestListener<List<FacilityStatus>>
    ) {
        listener.fail()
    }

    open fun queryElevatorStatus(
        equipmentNumber: String,
        listener: VolleyRestListener<FacilityStatus>
    ) {
        listener.fail()
    }

}
