/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository.occupancy.model

class DailyOccupancy(
    val dayOfWeek: Int,
    val hourlyOccupancies: List<HourlyOccupancy?>
)