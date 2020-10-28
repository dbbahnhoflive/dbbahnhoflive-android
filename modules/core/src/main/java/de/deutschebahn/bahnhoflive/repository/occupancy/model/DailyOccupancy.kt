/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository.occupancy.model

import kotlin.math.max

class DailyOccupancy(
    val maxAverage: Int?,
    val maxCurrent: Int?,
    val hourlyOccupancies: List<HourlyOccupancy>
) {

    val max = when {
        maxCurrent == null -> maxAverage
        maxAverage == null -> maxCurrent
        else -> max(maxAverage, maxCurrent)
    }

    val mostRecent by lazy {
        hourlyOccupancies
            .takeIf { it.any { it.current == null } }
            ?.lastOrNull { it.current != null }
    }
}