package de.deutschebahn.bahnhoflive.repository.wagonorder

import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.RisAdminWagonOrders
import de.deutschebahn.bahnhoflive.repository.fail

open class RisTransportsAdminRepository {

    open fun queryAdminWagonOrders(
        listener: VolleyRestListener<RisAdminWagonOrders>
    ) {
        listener.fail()
    }
}

