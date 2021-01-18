/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository.occupancy.model

class HourlyOccupancy(
    val dayOfWeek: Int,
    val hourOfDay: Int,
    var average: Int? = null,
    var current: Int? = null,
    var level: Int? = null,
    val statusText: CharSequence? = null
)