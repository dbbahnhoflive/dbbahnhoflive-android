/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.backend.db.ris

import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.google.gson.Gson
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.ris.locker.model.EquipmentLockers
import de.deutschebahn.bahnhoflive.backend.db.ris.locker.model.Locker
import de.deutschebahn.bahnhoflive.backend.parse

class RISStationsStationEquipmentsRequest(
    val stadaId: String,
    restListener: VolleyRestListener<List<Locker>>,
    dbAuthorizationTool: DbAuthorizationTool
) :
    RISStationsRequest<List<Locker>>(
        "station-equipments/locker/by-key?key=$stadaId&keyType=STATION_ID",
        dbAuthorizationTool,
        restListener
    ) {

    override fun parseNetworkResponse(response: NetworkResponse): Response<List<Locker>>? {
        super.parseNetworkResponse(response)

        return parse(response) {

            Gson().fromJson(
                response.data.decodeToString(),
                EquipmentLockers::class.java
            )?.lockerList?.flatMap {
                it?.lockers ?: emptyList()
            }?.filterNotNull() ?: throw Exception("Lockerlist empty")


        }
    }

}