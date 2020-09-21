/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.model.parking

import androidx.annotation.StringRes
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.ui.Status

enum class ParkingStatus(
    @field:StringRes val label: Int,
    val status: Status = Status.POSITIVE
) {
    ALWAYS_OPEN(R.string.parking_occupancy_24_7),
    AVAILABILITY_VERY_LOW(R.string.parking_occupancy_very_low),
    AVAILABILITY_LOW(R.string.parking_occupancy_low),
    AVAILABILITY_MEDIUM(R.string.parking_occupancy_medium),
    AVAILABILITY_HIGH(R.string.parking_occupancy_high),
    CLOSED(R.string.parking_occupancy_closed, Status.NEGATIVE),
    UNKNOWN(R.string.empty, Status.NONE);

    companion object {
        private val VALUES =
            values()

        @JvmStatic
        operator fun get(
            parkingFacility: ParkingFacility?
        ) = when {
            parkingFacility?.isOutOfService == true -> CLOSED
            parkingFacility?.liveCapacity != null -> parkingFacility.liveCapacity.parkingStatus
            else -> UNKNOWN
        }
    }

}