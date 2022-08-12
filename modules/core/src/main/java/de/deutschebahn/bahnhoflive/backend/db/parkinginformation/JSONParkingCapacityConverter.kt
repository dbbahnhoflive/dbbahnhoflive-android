/*
 * SPDX-FileCopyrightText: 2022 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.db.parkinginformation

import de.deutschebahn.bahnhoflive.backend.db.parkinginformation.ParkingFacilityConstants.ROOT_ARRAY
import de.deutschebahn.bahnhoflive.model.parking.LiveCapacity
import de.deutschebahn.bahnhoflive.model.parking.ParkingFacility
import de.deutschebahn.bahnhoflive.model.parking.ParkingStatus
import de.deutschebahn.bahnhoflive.util.json.JsonArrayObjectIterator
import de.deutschebahn.bahnhoflive.util.json.int
import org.json.JSONObject

class JSONParkingCapacityConverter(val parkingFacility: ParkingFacility) {

    fun parse(jsonObject: JSONObject): LiveCapacity? =
        jsonObject.optJSONArray(ROOT_ARRAY)?.let { rootArray ->
            Sequence { JsonArrayObjectIterator(rootArray) }.mapNotNull { capacityJSONObject ->
                capacityJSONObject?.takeIf { it.optString(ParkingFacilityConstants.Capacity.TYPE) == ParkingFacilityConstants.Capacity.Type.PARKING }
                    ?.int(ParkingFacilityConstants.Capacity.TOTAL)?.let { total ->
                        LiveCapacity(
                            parkingFacility.id,
                            when {
                                total > 50 -> ParkingStatus.AVAILABILITY_HIGH
                                total > 30 -> ParkingStatus.AVAILABILITY_MEDIUM
                                total > 10 -> ParkingStatus.AVAILABILITY_LOW
                                else -> ParkingStatus.AVAILABILITY_VERY_LOW
                            }
                        )
                    }
            }.firstOrNull()
        }

}

