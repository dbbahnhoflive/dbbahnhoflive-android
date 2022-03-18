/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository.occupancy.model

import de.deutschebahn.bahnhoflive.ui.station.occupancy.OccupancyViewBinder

class HourlyOccupancy(
    val dayOfWeek: Int,
    val hourOfDay: Int,
    var average: Int? = null,
    var current: Int? = null,
    var level: OccupancyViewBinder.Level? = null,
    val statusText: CharSequence? = null
) {
    fun String?.toLevel() = this?.run {
        when (this) {
            "LESS_THAN_NORMAL" -> OccupancyViewBinder.Level.LOW
            "NORMAL" -> OccupancyViewBinder.Level.AVERAGE
            "MORE_THAN_NORMAL" -> OccupancyViewBinder.Level.HIGH
            else -> null
        }
    }
}