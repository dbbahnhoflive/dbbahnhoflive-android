/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository.occupancy.model

class Occupancy(
    val dailyOccupancies: List<DailyOccupancy>
) {

    val max = dailyOccupancies.mapNotNull { it.max }.maxOfOrNull { it }

    val mostRecent by lazy {
        dailyOccupancies.mapNotNull { it.mostRecent }.firstOrNull()
            ?: dailyOccupancies.lastOrNull()?.hourlyOccupancies?.lastOrNull()
    }
}