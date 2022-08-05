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
