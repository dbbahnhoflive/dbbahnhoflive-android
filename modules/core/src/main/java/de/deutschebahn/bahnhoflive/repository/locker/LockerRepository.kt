/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.repository.locker

import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.ris.locker.model.Locker
import de.deutschebahn.bahnhoflive.repository.fail

open class LockerRepository {

    open fun queryLocker(
        stationId: String,
        listener: VolleyRestListener<List<Locker>>
    ) {
        listener.fail()
    }

}
