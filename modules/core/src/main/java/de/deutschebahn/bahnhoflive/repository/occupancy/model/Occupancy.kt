/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository.occupancy.model

import java.util.*

class Occupancy(
    val max: Int,
    val dailyOccupancies: List<DailyOccupancy>
) {

    fun getCurrentHourlyOccupancy() = Calendar.getInstance(Locale.GERMANY).let { calendar ->
        calendar.add(Calendar.HOUR_OF_DAY, -1)
        dailyOccupancies[(calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7]
            .hourlyOccupancies.getOrNull(calendar.get(Calendar.HOUR_OF_DAY))
    }

}