/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository.timetable

import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent
import de.deutschebahn.bahnhoflive.repository.fail
import de.deutschebahn.bahnhoflive.ui.timetable.journey.JourneyStop

open class TimetableRepository {
    open fun createTimetableCollector(): TimetableCollector = TimetableCollector()

    open fun queryJourneys(
        evaIds: EvaIds,
        scheduledTime: Long,
        trainEvent: TrainEvent,
        number: String?,
        category: String?,
        line: String?,
        listener: VolleyRestListener<List<JourneyStop>>
    ) {
        listener.fail()
    }
}