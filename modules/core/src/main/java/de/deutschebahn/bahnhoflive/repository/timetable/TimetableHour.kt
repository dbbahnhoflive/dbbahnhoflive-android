/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository.timetable

import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo

class TimetableHour(
    val hour: Long,
    val evaId: String,
    val trainInfos: List<TrainInfo>
)