package de.deutschebahn.bahnhoflive.repository.locker

import de.deutschebahn.bahnhoflive.backend.RestHelper
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.ris.RISStationsStationEquipmentsRequest
import de.deutschebahn.bahnhoflive.backend.db.ris.locker.model.Locker

class LockerInfoLockerRepository(
    private val restHelper: RestHelper,
    private val dbAuthorizationTool: DbAuthorizationTool
) : LockerRepository() {

    override fun queryLocker(
        stationId: String,
        listener: VolleyRestListener<List<Locker>>
    ) {
        restHelper.add(
            RISStationsStationEquipmentsRequest(
                stationId,
                listener,
                dbAuthorizationTool
            )
        )
    }

}