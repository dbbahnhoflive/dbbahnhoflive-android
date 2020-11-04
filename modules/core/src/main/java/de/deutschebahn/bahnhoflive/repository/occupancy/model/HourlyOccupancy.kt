/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository.occupancy.model

class HourlyOccupancy(
    val dayOfWeek: Int,
    val hourOfDay: Int,
    val average: Int?,
    val current: Int?,
    val level: Int?,
    val statusText: CharSequence?
) {
}