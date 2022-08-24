/*
 * SPDX-FileCopyrightText: 2022 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.db.ris

import com.google.gson.Gson
import de.deutschebahn.bahnhoflive.backend.db.ris.locker.model.EquipmentLockers
import de.deutschebahn.bahnhoflive.backend.db.ris.locker.model.Locker

class RISStationsStationEquipmentsResponseParser {


    fun parse(jsonString: String): List<Locker> {

        return Gson().fromJson(
            jsonString,
            EquipmentLockers::class.java
        )?.lockerList?.flatMap {
            it?.lockers ?: emptyList()
        }?.filterNotNull() ?: throw Exception("Unexpected empty List")

    }
}