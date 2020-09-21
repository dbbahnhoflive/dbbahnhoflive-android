/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.timetable

import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent

data class FilterSummary(
    val track: String? = null,
    val trainCategory: String? = null,
    val trainEvent: TrainEvent = TrainEvent.DEPARTURE,
    val matchCount: Int,
    val endTime: Long,
    val isMayLoadMore: Boolean = false
)