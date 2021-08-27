/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository.timetable

import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.ris.model.DepartureMatches
import de.deutschebahn.bahnhoflive.repository.fail

open class TimetableRepository {
    open fun createTimetableCollector(): TimetableCollector = TimetableCollector()

    open fun queryJourneysByRelation(
        listener: VolleyRestListener<DepartureMatches>,
        number: String,
        category: String,
        line: String?
    ) {
        listener.fail()
    }
}